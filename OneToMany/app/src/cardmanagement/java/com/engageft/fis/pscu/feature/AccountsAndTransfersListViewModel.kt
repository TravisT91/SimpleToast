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
import com.ob.ws.dom.FundingAccountsResponse
import com.engageft.engagekit.rest.request.FundingAccountsRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import org.joda.time.DateTime
import com.ob.ws.dom.AchLoadsResponse
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import utilGen1.ScheduledLoadUtils
import java.util.*


class AccountsAndTransfersListViewModel: BaseEngageViewModel() {

    private companion object {
        private const val HISTORICAL_LOADS_LIST_MAX_COUNT = 5
    }

    private var achAccountInfoList = mutableListOf<AchAccountInfo>()
    private var historicalAccountInfoList = mutableListOf<AchAccountInfo>()
    private var scheduledLoadList = mutableListOf<ScheduledLoad>()
    private var historicalLoadList = mutableListOf<AchLoadInfo>()

    enum class BankAccountStatus {
        NO_BANK_ACCOUNT,
        UNVERIFIED_BANK_ACCOUNT,
        VERIFIED_BANK_ACCOUNT
    }

    enum class Transfers {
        SCHEDULED_TRANSFERS,
        HISTORICAL_TRANSFERS
    }

//    enum class Transfers {
//        SCHEDULED_TRANSFERS,
//        HISTORICAL_TRANSFERS
//    }
    // todo: would make more sense to display Pair<BankAccountStatus, mutableListOf<AchAccountInfo>()>
    var bankAccountStatusObservable = MutableLiveData<BankAccountStatus>()

    init {
        progressOverlayShownObservable.value = true

        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response is LoginResponse) {
                        achAccountInfoList = response.achAccountList
                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        getAccountsList(currentCard)
//                        initBankAccountStatus()
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )    }

    private fun getScheduledLoads(currentCard: DebitCardInfo, getHistoricalLoadsOnSuccess: Boolean) {
        //todo show/hide progress
        compositeDisposable.add(
                EngageService.getInstance().getScheduledLoadsResponseObservable(EngageService.getInstance().authManager.authToken, currentCard, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response.javaClass == ScheduledLoadsResponse::class.java) {
                                val scheduledLoadsResponse = response as ScheduledLoadsResponse
                                scheduledLoadList = ScheduledLoadUtils.getScheduledLoads(scheduledLoadsResponse).toMutableList()
                                if (getHistoricalLoadsOnSuccess) {
                                    getAccountsList(currentCard)
                                } else {
//                                    hideProgressOverlay()
//                                    setupRecyclerView()
                                }
                            } else {
//                                hideProgressOverlay()
//                                handleErrorResponse(response)
//                                setupRecyclerView()
                            }
                        }, { e ->
//                            hideProgressOverlay()
                            handleThrowable(e)
//                            setupRecyclerView()
                        })
        )
    }

    /**
     * Attempts to retrieve a list of all accounts used in past transfers, even those no longer configured as active in the app,
     * so that the list of historical transfers can accurately display the bank name involved in each one. If this step fails,
     * the list is still displayed but in each case the bank involved will be listed as "Unknown Account".
     *
     * @param currentCard the current card
     */
    //todo
    private fun getAccountsList(currentCard: DebitCardInfo) {
        // todo show/hide progress
        val accountsRequest = FundingAccountsRequest(EngageService.getInstance().authManager.authToken)
        compositeDisposable.add(engageApi().postListFundingAccounts(accountsRequest.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            historicalAccountInfoList = if (response.isSuccess && response is FundingAccountsResponse ) {
                                response.achAccountList.toMutableList()
                            } else {
                                // set to empty list
                                mutableListOf()
                            }
//                            getHistoricalLoads(currentCard)
                        }, { e ->
                            // set to empty list
                            historicalAccountInfoList = mutableListOf()
//                            getHistoricalLoads(currentCard)
                        })
        )
    }

    private fun getHistoricalLoads(currentCard: DebitCardInfo) {
        progressOverlayShownObservable.value = true
        val cardRequest = CardRequest(EngageService.getInstance().authManager.authToken, currentCard.debitCardId)
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postListHistoricalLoads(cardRequest.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess && response.javaClass == AchLoadsResponse::class.java) {
                                val loads = (response as AchLoadsResponse).loads
                                // sort by date
                                loads.sortWith(Comparator { one, two ->
                                    val date1 = BackendDateTimeUtils.parseDateTimeFromIso8601String(one.isoLoadDate)
                                    val date2 = BackendDateTimeUtils.parseDateTimeFromIso8601String(two.isoLoadDate)
                                    date2!!.compareTo(date1)
                                })
                                historicalLoadList = loads.subList(0, Math.min(loads.size, HISTORICAL_LOADS_LIST_MAX_COUNT))
//                                setupRecyclerView()
                            } else {
                                handleUnexpectedErrorResponse(response)
//                                setupRecyclerView()
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
//                            setupRecyclerView()
                        })
        )
    }

    private fun initBankAccountStatus() {
        if (achAccountInfoList.isNotEmpty()) {
            var verified = false
            for (achAccountInfo in achAccountInfoList) {
                if (achAccountInfo.achAccountStatus == AchAccountStatus.VERIFIED) {
                    bankAccountStatusObservable.value = BankAccountStatus.VERIFIED_BANK_ACCOUNT
                    verified = true
                    break
                }
            }
            if (!verified) {
                bankAccountStatusObservable.value = BankAccountStatus.UNVERIFIED_BANK_ACCOUNT
            }
        } else {
            bankAccountStatusObservable.value = BankAccountStatus.NO_BANK_ACCOUNT
        }
    }

}