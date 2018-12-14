package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.model.ScheduledLoad
import com.ob.domain.lookup.AchAccountStatus
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AchAccountInfo
import com.ob.ws.dom.utility.AchLoadInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.ob.ws.dom.ScheduledLoadsResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.ob.ws.dom.AchLoadsResponse
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import utilGen1.ScheduledLoadUtils

class AccountsAndTransfersListViewModel: BaseEngageViewModel() {

    private companion object {
        private const val HISTORICAL_LOADS_LIST_MAX_COUNT = 5
    }

    enum class BankAccountStatus {
        NO_BANK_ACCOUNT,
        UNVERIFIED_BANK_ACCOUNT,
        VERIFIED_BANK_ACCOUNT
    }

    val achAccountsListAndStatusObservable = MutableLiveData<AchBankAccountListAndStatus>()
    val achScheduledLoadListObservable = MutableLiveData<List<ScheduledLoad>>()
    val achHistoricalLoadListObservable = MutableLiveData<List<AchLoadInfo>>()

    private var loginResponse: LoginResponse? = null
    private var shouldHideProgressOverlay = false

    init {
        initBankAccountsListAndTransfersList()
    }

    fun refreshViews() {
        //TODO(aHashimi): need to create LiveData for observing LoginResponse so we don't have to do this step.
        //TODO(aHashimi): FOTM-65 & FOTM-113 must do clearLoginResponse
        // for this main screen to show the updated to item correctly.
        if (this.loginResponse != EngageService.getInstance().storageManager.loginResponse) {
            initBankAccountsListAndTransfersList()
        }
    }

    private fun initBankAccountsListAndTransfersList() {
        progressOverlayShownObservable.value = true

        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // don't hide progressOverlay just yet
                    if (response is LoginResponse) {
                        loginResponse = response

                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        // the order of invoking these methods don't matter
                        initBankAccountStatusAndList(response.achAccountList)
                        // hide progress when the following two API calls are done.
                        shouldHideProgressOverlay = false
                        getScheduledLoads(currentCard)
                        getHistoricalLoads(currentCard)
                    } else {
                        progressOverlayShownObservable.value = false
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )
    }

    private fun getScheduledLoads(currentCard: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().getScheduledLoadsResponseObservable(EngageService.getInstance().authManager.authToken, currentCard, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            shouldHideProgressOverlay(true)
                            if (response.isSuccess && response is ScheduledLoadsResponse) {
                                achScheduledLoadListObservable.value = ScheduledLoadUtils.getScheduledLoads(response)
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            shouldHideProgressOverlay(true)
                            handleThrowable(e)
                        })
        )
    }

    private fun getHistoricalLoads(currentCard: DebitCardInfo) {
        val cardRequest = CardRequest(EngageService.getInstance().authManager.authToken, currentCard.debitCardId)
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postListHistoricalLoads(cardRequest.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            shouldHideProgressOverlay(true)
                            if (response.isSuccess && response is AchLoadsResponse) {
                                val loads = response.loads
                                // sort by date
                                loads.sortWith(Comparator { one, two ->
                                    val date1 = BackendDateTimeUtils.parseDateTimeFromIso8601String(one.isoLoadDate)
                                    val date2 = BackendDateTimeUtils.parseDateTimeFromIso8601String(two.isoLoadDate)
                                    date2!!.compareTo(date1)
                                })
                                val historicalLoadList = loads.subList(0, Math.min(loads.size, HISTORICAL_LOADS_LIST_MAX_COUNT))
                                achHistoricalLoadListObservable.value = historicalLoadList
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            shouldHideProgressOverlay(true)
                            handleThrowable(e)
                        })
        )
    }

    private fun initBankAccountStatusAndList(achAccountInfoList: List<AchAccountInfo>) {
        if (achAccountInfoList.isNotEmpty()) {

            var verified = false
            for (achAccountInfo in achAccountInfoList) {

                if (achAccountInfo.achAccountStatus == AchAccountStatus.VERIFIED) {

                    achAccountsListAndStatusObservable.value = AchBankAccountListAndStatus(
                            bankStatus = BankAccountStatus.VERIFIED_BANK_ACCOUNT,
                            achAccountInfoList = achAccountInfoList)
                    verified = true
                    break
                }
            }
            if (!verified) {
                achAccountsListAndStatusObservable.value = AchBankAccountListAndStatus(
                        bankStatus = BankAccountStatus.UNVERIFIED_BANK_ACCOUNT,
                        achAccountInfoList = achAccountInfoList)
            }
        } else {
            achAccountsListAndStatusObservable.value = AchBankAccountListAndStatus(
                    bankStatus = BankAccountStatus.NO_BANK_ACCOUNT,
                    achAccountInfoList = achAccountInfoList)
        }
    }

    private fun shouldHideProgressOverlay(apiCallDone: Boolean) {
        if (shouldHideProgressOverlay && apiCallDone) {
            progressOverlayShownObservable.value = false
        }
        shouldHideProgressOverlay = true
    }

    data class AchBankAccountListAndStatus(val bankStatus: BankAccountStatus, val achAccountInfoList: List<AchAccountInfo>)
}