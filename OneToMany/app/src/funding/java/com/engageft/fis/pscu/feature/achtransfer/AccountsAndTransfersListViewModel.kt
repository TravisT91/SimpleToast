package com.engageft.fis.pscu.feature.achtransfer

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.domain.lookup.AchAccountStatus
import com.ob.ws.dom.AchLoadsResponse
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.ScheduledLoadsResponse
import com.ob.ws.dom.utility.AchAccountInfo
import com.ob.ws.dom.utility.AchLoadInfo
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

    enum class CreateTransferButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    val createTransferButtonStateObservable = MutableLiveData<CreateTransferButtonState>()
    val accountsAndTransfersListObservable = MutableLiveData<List<AccountsAndTransferListItem>>().apply {
        listOf<List<AccountsAndTransferListItem>>()
    }

    // We keep 3 lists in memory to rebuild the observable any time one of the many API calls completes.
    var loginResponseListSetion = ArrayList<AccountsAndTransferListItem>()
    var scheduledLoadListSection = ArrayList<AccountsAndTransferListItem>()
    var historicalLoadListSection = ArrayList<AccountsAndTransferListItem>()
    var createTransferListSection = ArrayList<AccountsAndTransferListItem>()

    var achBankAccountId = 0L
    val achBankAccountsListObservable = MutableLiveData<List<AchAccountInfo>>()
    val isAchEnabledObservable = MutableLiveData<Boolean>()
    val isAllowAddAchAccountObservable = MutableLiveData<Boolean>()
    val achScheduledLoadListObservable = MutableLiveData<List<ScheduledLoad>>()
    val achHistoricalLoadListObservable = MutableLiveData<List<AchLoadInfo>>()

    private var loginResponse: LoginResponse? = null

    init {
        createTransferButtonStateObservable.value = CreateTransferButtonState.GONE
    }

    private fun concatenateObservable() {
        accountsAndTransfersListObservable.value = ArrayList<AccountsAndTransferListItem>().apply {
            addAll(loginResponseListSetion)
            addAll(scheduledLoadListSection)
            addAll(historicalLoadListSection)
            addAll(createTransferListSection)
        }
    }

    fun refreshViews() {
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        loginResponse = response

                        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(response)!!
                        val isAchEnabled = accountInfo.accountPermissionsInfo.isFundingAchEnabled
                        val creditDebitEnabled = accountInfo.accountPermissionsInfo.isFundingDebitCardEnabled

                        var showCardLoadHeader = true
                        var showCreateTransfer = false

                        val achSection = ArrayList<AccountsAndTransferListItem>()
                        if (isAchEnabled) {
                            achSection.add(AccountsAndTransferListItem.HeaderItem.BankAccountHeaderItem)
                            var hasOneUnverifiedAccount = false
                            for (account in response.achAccountList) {
                                val item = when (account.achAccountStatus) {
                                    AchAccountStatus.VERIFIED -> {
                                        showCardLoadHeader = false
                                        showCreateTransfer = true
                                        AccountsAndTransferListItem.BankAccountItem(account.bankName, account.accountLastDigits, true, account.achAccountId)
                                    }
                                    AchAccountStatus.UNVERIFIED -> {
                                        hasOneUnverifiedAccount = true
                                        AccountsAndTransferListItem.BankAccountItem(account.bankName, account.accountLastDigits, false, account.achAccountId)
                                    }
                                    AchAccountStatus.REMOVED -> {
                                        // Don't show these.
                                        null
                                    }
                                    AchAccountStatus.FAILED_VERIFICATION -> {
                                        // Assuming just show this as UNVERIFIED??
                                        hasOneUnverifiedAccount = true
                                        AccountsAndTransferListItem.BankAccountItem(account.bankName, account.accountLastDigits, false, account.achAccountId)
                                    }
                                }
                                item?.let {
                                    achSection.add(it)
                                }
                            }
                            if (accountInfo.accountPermissionsInfo.isFundingAddAchAllowable) {
                                achSection.add(AccountsAndTransferListItem.AddItem.AddBankAccountItem)
                            }
                            if (hasOneUnverifiedAccount) {
                                achSection.add(AccountsAndTransferListItem.VerifyBankAccountFooterItem)
                            }
                        }

                        val creditDebitSection = ArrayList<AccountsAndTransferListItem>()
                        if (creditDebitEnabled) {
                            creditDebitSection.add(AccountsAndTransferListItem.HeaderItem.CreditDebitHeaderItem)
                            for (account in response.ccAccountList) {
                                // TODO(jhutchins): Do we care about isActive or expiration dates to filter these out of the list?
                                showCardLoadHeader = false
                                showCreateTransfer = true
                                creditDebitSection.add(AccountsAndTransferListItem.CreditDebitCardItem(account.lastDigits))
                            }
                            if (accountInfo.accountPermissionsInfo.isFundingAddDebitCardAllowable) {
                                creditDebitSection.add(AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem)
                            }
                        }

                        loginResponseListSetion = ArrayList<AccountsAndTransferListItem>().apply {
                            if (showCardLoadHeader) {
                                add(AccountsAndTransferListItem.CardLoadHeaderItem)
                            }
                            addAll(achSection)
                            addAll(creditDebitSection)
                        }
                        createTransferListSection = ArrayList<AccountsAndTransferListItem>().apply {
                            if (showCreateTransfer) {
                                createTransferButtonStateObservable.value = CreateTransferButtonState.VISIBLE_ENABLED
                                add(AccountsAndTransferListItem.CreateTransferItem)
                            } else {
                                createTransferButtonStateObservable.value = CreateTransferButtonState.GONE
                            }
                        }

                        concatenateObservable()

                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        getScheduledLoads(currentCard)
                        getHistoricalLoads(currentCard)
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }

    private fun getScheduledLoads(currentCard: DebitCardInfo) {
        // TODO(jhutchins): FOTM-1002 update for merging historical loads and scheduled loads.
        compositeDisposable.add(
                EngageService.getInstance().getScheduledLoadsResponseObservable(currentCard, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is ScheduledLoadsResponse) {
                                val scheduledLoads = ArrayList<AccountsAndTransferListItem>()
                                val scheduledLoadsList = ScheduledLoadUtils.getScheduledLoads(response)
                                if (scheduledLoadsList.isNotEmpty()) {
                                    scheduledLoads.add(AccountsAndTransferListItem.HeaderItem.ScheduledLoadHeader)
                                }
                                for (scheduledLoad in scheduledLoadsList) {
                                    val nextRunDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(scheduledLoad.isoNextRunDate)
                                    when (scheduledLoad.typeString) {
                                        ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> {
                                            scheduledLoads.add(AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem(
                                                    nextRunDate, "TODO", true, scheduledLoad.amount, scheduledLoad.scheduledLoadId))
                                        }
                                        ScheduledLoad.SCHED_LOAD_TYPE_ALT_WEEKLY -> {
                                            scheduledLoads.add(AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.WeeklyItem(
                                                    nextRunDate, "TODO", true, scheduledLoad.amount, scheduledLoad.scheduledLoadId))
                                        }
                                        ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> {
                                            scheduledLoads.add(AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.AltWeeklyItem(
                                                    nextRunDate, "TODO", true, scheduledLoad.amount, scheduledLoad.scheduledLoadId))
                                        }
                                        else -> {
                                            handleUnexpectedErrorResponse(BasicResponse(false,
                                                    "Unexpected scheduled load type: ${scheduledLoad.typeString}"))
                                        }
                                    }
                                }
                                scheduledLoadListSection = scheduledLoads
                                concatenateObservable()
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            handleThrowable(e)
                        })
        )
    }

    private fun getHistoricalLoads(currentCard: DebitCardInfo) {
        // TODO(jhutchins): FOTM-1002 update for merging historical loads and scheduled loads.
        val cardRequest = CardRequest(currentCard.debitCardId)
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postListHistoricalLoads(cardRequest.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is AchLoadsResponse) {
                                val loads = response.loads
                                // sort by date
                                loads.sortWith(Comparator { one, two ->
                                    val date1 = BackendDateTimeUtils.parseDateTimeFromIso8601String(one.isoLoadDate)
                                    val date2 = BackendDateTimeUtils.parseDateTimeFromIso8601String(two.isoLoadDate)
                                    date2!!.compareTo(date1)
                                })
                                val historicalLoadList = loads.subList(0, Math.min(loads.size, HISTORICAL_LOADS_LIST_MAX_COUNT))
                                val historicalLoads = ArrayList<AccountsAndTransferListItem>()
                                if (historicalLoadList.isNotEmpty()) {
                                    historicalLoads.add(AccountsAndTransferListItem.HeaderItem.RecentActivityHeaderItem)
                                }
                                for (load in historicalLoadList) {
                                    historicalLoads.add(AccountsAndTransferListItem.TransferItem.RecentActivityItem(
                                            BackendDateTimeUtils.parseDateTimeFromIso8601String(load.isoLoadDate), "TODO", true, load.amount))
                                }
                                historicalLoadListSection = historicalLoads
                                concatenateObservable()
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            handleThrowable(e)
                        })
        )
    }

//    private fun initBankAccountStatusAndList(loginResponse: LoginResponse) {
//        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
//        accountInfo?.let { account ->
//            if (account.accountPermissionsInfo.isFundingAchEnabled) {
//
//                if (loginResponse.achAccountList.isNotEmpty()) {
//                    loginResponse.achAccountList.find { achAccount ->
//                        achAccount.achAccountStatus == AchAccountStatus.VERIFIED
//                    }?.let { achAccountInfo ->
//                        achBankAccountId = achAccountInfo.achAccountId
//                        achBankAccountsListObservable.value = listOf(achAccountInfo)
//                        createTransferButtonStateObservable.value = CreateTransferButtonState.CREATE_TRANSFER
//                    }
//
//                    loginResponse.achAccountList.find { achAccount ->
//                        achAccount.achAccountStatus == AchAccountStatus.UNVERIFIED
//                    }?.let { achAccountInfo ->
//                        achBankAccountId = achAccountInfo.achAccountId
//                        achBankAccountsListObservable.value = listOf(achAccountInfo)
//                        createTransferButtonStateObservable.value = CreateTransferButtonState.VERIFY_BANK
//                    }
//                } else {
//                    createTransferButtonStateObservable.value = CreateTransferButtonState.HIDE
//
//                    //TODO(aHashimi): Adding of multiple ACH bank accounts is not supported yet [on Frontend at least]
//                    // calling this here makes sense for now.
//                    isAllowAddAchAccountObservable.value = account.accountPermissionsInfo.isFundingAddAchAllowable
//                }
//            } else {
//                isAchEnabledObservable.value = false
//                createTransferButtonStateObservable.value = CreateTransferButtonState.HIDE
//            }
//        }
//    }
}