package com.engageft.fis.pscu.feature.achtransfer

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
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import utilGen1.ScheduledLoadUtils
/**
 * AccountsAndTransfersListViewModel
 * </p>
 * ViewModel that provides ACH Bank Accounts, scheduled and past transfers lists.
 * </p>
 * Created by Atia Hashimi 12/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountsAndTransfersListViewModel: BaseEngageViewModel() {

    private companion object {
        private const val HISTORICAL_LOADS_LIST_MAX_COUNT = 5
    }

    enum class AchButtonState {
        HIDE,
        CREATE_TRANSFER,
        VERIFY_BANK
    }

    var achBankAccountId = 0L
    val achBankAccountsListObservable = MutableLiveData<List<AchAccountInfo>>()
    val achButtonStateObservable = MutableLiveData<AchButtonState>()
    val isAchEnabledObservable = MutableLiveData<Boolean>()
    val isAllowAddAchAccountObservable = MutableLiveData<Boolean>()
    val achScheduledLoadListObservable = MutableLiveData<List<ScheduledLoad>>()
    val achHistoricalLoadListObservable = MutableLiveData<List<AchLoadInfo>>()

    private var loginResponse: LoginResponse? = null
    private var shouldHideProgressOverlay = false

    init {
        achButtonStateObservable.value = AchButtonState.HIDE

        initBankAccountsListAndTransfersList()
    }

    fun refreshViews() {
        // for this main screen to show the updated to item correctly.
        if (this.loginResponse != EngageService.getInstance().storageManager.loginResponse) {
            initBankAccountsListAndTransfersList()
        }
    }

    fun isBankVerified(): Boolean {
        return achButtonStateObservable.value == AchButtonState.CREATE_TRANSFER
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

                        // the order of invoking the following methods don't matter

                        initBankAccountStatusAndList(response)

                        val currentCard = LoginResponseUtils.getCurrentCard(response)
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
                EngageService.getInstance().getScheduledLoadsResponseObservable(currentCard, false)
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
        val cardRequest = CardRequest(currentCard.debitCardId)
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

    private fun initBankAccountStatusAndList(loginResponse: LoginResponse) {
        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        accountInfo?.let { account ->
            if (account.accountPermissionsInfo.isFundingAchEnabled) {

                if (loginResponse.achAccountList.isNotEmpty()) {
                    loginResponse.achAccountList.find { achAccount ->
                        achAccount.achAccountStatus == AchAccountStatus.VERIFIED
                    }?.let { achAccountInfo ->
                        achBankAccountId = achAccountInfo.achAccountId
                        achBankAccountsListObservable.value = listOf(achAccountInfo)
                        achButtonStateObservable.value = AchButtonState.CREATE_TRANSFER
                    }

                    loginResponse.achAccountList.find { achAccount ->
                        achAccount.achAccountStatus == AchAccountStatus.UNVERIFIED
                    }?.let { achAccountInfo ->
                        achBankAccountId = achAccountInfo.achAccountId
                        achBankAccountsListObservable.value = listOf(achAccountInfo)
                        achButtonStateObservable.value = AchButtonState.VERIFY_BANK
                    }
                } else {
                    achButtonStateObservable.value = AchButtonState.HIDE

                    //TODO(aHashimi): Adding of multiple ACH bank accounts is not supported yet [on Frontend at least]
                    // calling this here makes sense for now.
                    isAllowAddAchAccountObservable.value = account.accountPermissionsInfo.isFundingAddAchAllowable
                }
            } else {
                isAchEnabledObservable.value = false
                achButtonStateObservable.value = AchButtonState.HIDE
            }
        }
    }

    private fun shouldHideProgressOverlay(apiCallDone: Boolean) {
        if (shouldHideProgressOverlay && apiCallDone) {
            progressOverlayShownObservable.value = false
        }
        shouldHideProgressOverlay = true
    }
}