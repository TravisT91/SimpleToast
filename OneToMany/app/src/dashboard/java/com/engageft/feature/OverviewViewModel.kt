package com.engageft.feature

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.event.TransactionsListEvent
import com.engageft.engagekit.tools.TransactionsFilter
import com.engageft.engagekit.utils.AlertUtils
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.domain.lookup.TransactionStatus
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.TransactionInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Months
import java.math.BigDecimal

/**
 *  OverviewViewModel
 *  </p>
 *  Supports OverviewView, OverviewFragment, and OverviewView
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class OverviewViewModel : BaseViewModel() {
    var spendingBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var spendingBalanceStateObservable: MutableLiveData<OverviewBalanceState> = MutableLiveData()

    var savingsBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var savingsBalanceStateObservable: MutableLiveData<OverviewBalanceState> = MutableLiveData()

    var allTransactionsObservable: MutableLiveData<List<TransactionInfo>> = MutableLiveData()
    var retrievingTransactionsFinishedObservable: MutableLiveData<Boolean> = MutableLiveData()

    var notificationsCountObservable: MutableLiveData<Int> = MutableLiveData()

    var transactionsTabPosition = TRANSACTIONS_TAB_POSITION_ALL

    private val compositeDisposable = CompositeDisposable()

    val dialogInfoObservable: MutableLiveData<DashboardDialogInfo> = MutableLiveData()
    val navigationObservable = MutableLiveData<OverviewNavigationEvent>()

    private lateinit var debitCardInfo: DebitCardInfo
    private var transactionsInitialized = false
    private var isRefreshing = false

    fun initBalancesAndNotifications() {
        spendingBalanceStateObservable.value = OverviewBalanceState.LOADING
        // only change savings state if already set. Otherwise it is currently hidden in UI, so don't show loading indicator
        if (savingsBalanceStateObservable.value == OverviewBalanceState.AVAILABLE) {
            savingsBalanceStateObservable.value = OverviewBalanceState.LOADING
        }
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()) // TODO: run on background thread, then postValues?
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                debitCardInfo?.apply {
                                    try {
                                        spendingBalanceObservable.value = BigDecimal(currentBalance)
                                        spendingBalanceStateObservable.value = OverviewBalanceState.AVAILABLE
                                    } catch (e: Throwable) {
                                        spendingBalanceObservable.value = BigDecimal.ZERO
                                        spendingBalanceStateObservable.value = OverviewBalanceState.ERROR
                                    }
                                } ?: run {
                                    // error getting debit card info
                                    spendingBalanceObservable.value = BigDecimal.ZERO
                                    spendingBalanceStateObservable.value = OverviewBalanceState.ERROR
                                }

                                try {
                                    val goalAmount = BigDecimal(response.goalsContributed)
                                    val savingsAmount = BigDecimal(response.savingsContributed)
                                    savingsBalanceObservable.value = goalAmount.plus(savingsAmount)
                                    savingsBalanceStateObservable.value = OverviewBalanceState.AVAILABLE
                                } catch (e: Throwable) {
                                    savingsBalanceObservable.value = BigDecimal.ZERO
                                    savingsBalanceStateObservable.value = OverviewBalanceState.ERROR
                                }

                                updateNotifications(response)
                            } else {
                                // TODO(jhutchins): Proper error handling.
                                dialogInfoObservable.value = DashboardDialogInfo()
                                spendingBalanceObservable.value = BigDecimal.ZERO
                                spendingBalanceStateObservable.value = OverviewBalanceState.ERROR
                                savingsBalanceObservable.value = BigDecimal.ZERO
                                savingsBalanceStateObservable.value = OverviewBalanceState.ERROR

                            }
                        })
                        { e ->
                            // TODO(jhutchins): Proper error handling.
                            dialogInfoObservable.value = DashboardDialogInfo()
                            spendingBalanceObservable.value = BigDecimal.ZERO
                            spendingBalanceStateObservable.value = OverviewBalanceState.ERROR
                            savingsBalanceObservable.value = BigDecimal.ZERO
                            savingsBalanceStateObservable.value = OverviewBalanceState.ERROR
                        }
        )
    }

    fun initTransactions(useCached: Boolean = true) {
        if (!transactionsInitialized) {
            transactionsInitialized = true
            compositeDisposable.add(
                    EngageService.getInstance().loginResponseAsObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                if (response.isSuccess && response is LoginResponse) {
                                    debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                    if (debitCardInfo != null) {
                                        // get 6 months of transactions (or less, if card request date less than 6 months ago)
                                        val numberMonthsToLoad = getCountOfMonthsToLoadAndReload()
                                        if (numberMonthsToLoad > 0) {
                                            loadMostRecentMonthlyTransactions(numberMonthsToLoad, useCached)
                                        } else {
                                            // this could happen if using a malformed test account with isoRequestDate in the future
                                            progressOverlayShownObservable.value = false
                                        }
                                    } else {
                                        // TODO No debit card in login response

                                        // TODO(jhutchins): Proper error handling.
                                        dialogInfoObservable.value = DashboardDialogInfo()
                                    }
                                } else {

                                    // TODO(jhutchins): Proper error handling.
                                    dialogInfoObservable.value = DashboardDialogInfo()
                                }
                            })
                            { e ->
                                // TODO(jhutchins): Proper error handling.
                                dialogInfoObservable.value = DashboardDialogInfo()
                            }
            )
        }
    }

    private fun updateNotifications(loginResponse: LoginResponse) {
        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        if (accountInfo != null) {
            val currentMessages = AlertUtils.getAlertMessagesForDisplay(accountInfo.accountId, true, false)
            currentMessages?.let {
                var notificationsCount = 0
                for (alertMessage in currentMessages) {
                    if (!alertMessage.deleted && !alertMessage.viewed) {
                        ++notificationsCount
                    }
                }
                notificationsCountObservable.value = notificationsCount
            } ?: run {
                notificationsCountObservable.value = 0
            }
        }

    }

    fun refreshBalancesAndNotifications() {
        progressOverlayShownObservable.value = true
        EngageService.getInstance().clearLoginAndDashboardResponses()
        initBalancesAndNotifications()
    }

    fun refreshTransactions() {
        if (!isRefreshing) {
            isRefreshing = true
            transactionsInitialized = false
            initTransactions(false)
        }
    }

    private fun loadMostRecentMonthlyTransactions(monthsToLoad: Int, useCached: Boolean) {
        // showProgressOverlay for transactions has been replaced with placeholder views

        val now = DateTime.now()

        val populateFilter = TransactionsFilter()
        populateFilter.filterYear = now.year
        populateFilter.filterMonth = now.monthOfYear
        var transactionsMonthsFetchedCounter = -1

        for (i in 0..monthsToLoad) {
            compositeDisposable.add(
                    EngageService.getInstance().transactionsListObservable(
                            populateFilter.filterYear, populateFilter.filterMonth, debitCardInfo.debitCardId, useCached)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe( {
                                this.handleTransactionsListEvent(it)
                                // we need this counter to notify TransactionsAdapter that fetching of transactions data is completed
                                // This is for now until loading of data with one call is done.
                                transactionsMonthsFetchedCounter++
                                if (transactionsMonthsFetchedCounter == monthsToLoad) {
                                    retrievingTransactionsFinishedObservable.value = true
                                }
                            }) { e ->
                                // TODO(jhutchins): Proper error handling.
                                dialogInfoObservable.value = DashboardDialogInfo()
                            }
            )
            populateFilter.filterYear = if (populateFilter.filterMonth > 1) populateFilter.filterYear else populateFilter.filterYear - 1
            populateFilter.filterMonth = if (populateFilter.filterMonth > 1) populateFilter.filterMonth - 1 else 12
        }
    }

    private fun handleTransactionsListEvent(event: TransactionsListEvent) {
        if (isRefreshing) {
            isRefreshing = false
            (allTransactionsObservable.value as MutableList).clear()
        }
        if (event.isSuccessful) {
            val newTransactions = EngageService.getInstance().storageManager.transactionsStore.getTransactionsWithFilter(debitCardInfo.debitCardId, TransactionsFilter(event.year, event.month).addStatus(TransactionStatus.PENDING))
            // add new transactions to existing list, or create a new list if no existing list
            var transactionsList = allTransactionsObservable.value?.let {
                it.toMutableList()
            } ?: run {
                mutableListOf<TransactionInfo>()
            }
            transactionsList.addAll(newTransactions)

            // TODO(kurt): only update observable once all in-flight requests have completed. Otherwise, we are doing
            // a lot of extra work to update recyclerview many times, once for each request completion.
            allTransactionsObservable.value = transactionsList
        }
    }

    private fun getCountOfMonthsToLoadAndReload(): Int {
        var count = MAX_MONTHS_TO_LOAD
        if (!TextUtils.isEmpty(debitCardInfo.isoRequestDate)) {
            BackendDateTimeUtils.parseDateTimeFromIso8601String(debitCardInfo.isoRequestDate).let { requestDate ->
                val now = DateTime.now()
                count = if (datesAreSameMonthAndYear(requestDate, now)) {
                    1
                } else {
                    // add 2 because monthsBetween is exclusive. If activation is January and now is February, monthsBetween is 0.
                    Math.min(Months.monthsBetween(requestDate, DateTime.now()).months + 2, count)
                }
            }
        } else {
            count = 1
        }

        // If for some reason debitCardInfo.isoRequestDate is months/years in the future, like for malformed
        // test accounts, set to a sensible value rather than a negative number of months.
        if (count < 0) count = 0

        return count
    }

    // These are called by OverviewFragment in response to user presses in OverviewView
    fun showDirectDepositInfo() {
        navigationObservable.value = OverviewNavigationEvent.SHOW_DIRECT_DEPOSIT_INFO
    }

    fun showAddMoney() {
        navigationObservable.value = OverviewNavigationEvent.SHOW_ADD_MONEY
    }

    fun showLockUnlockCard() {
        navigationObservable.value = OverviewNavigationEvent.SHOW_LOCK_UNLOCK_CARD
    }

    fun showChangePin() {
        navigationObservable.value = OverviewNavigationEvent.SHOW_CHANGE_PIN
    }

    fun datesAreSameMonthAndYear(date1: DateTime?, date2: DateTime?): Boolean {
        return date1 != null && date2 != null && date1.year == date2.year && date1.monthOfYear == date2.monthOfYear
    }

    companion object {
        const val MAX_MONTHS_TO_LOAD = 6

        const val TRANSACTIONS_TAB_POSITION_ALL = 0
        const val TRANSACTIONS_TAB_POSITION_DEPOSITS = 1
    }
}

enum class OverviewNavigationEvent {
    SHOW_DIRECT_DEPOSIT_INFO, SHOW_ADD_MONEY, SHOW_LOCK_UNLOCK_CARD, SHOW_CHANGE_PIN
}

enum class OverviewBalanceState {
    LOADING, AVAILABLE, HIDDEN, ERROR
}

class DashboardDialogInfo(title: String? = null,
                       message: String? = null,
                       tag: String? = null,
                       val dialogType: DialogType = DialogType.GENERIC_ERROR) : DialogInfo(title, message, tag) {
    enum class DialogType {
        GENERIC_ERROR,
        SERVER_ERROR
    }
}