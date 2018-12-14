package com.engageft.fis.pscu.feature

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.event.TransactionsListEvent
import com.engageft.engagekit.repository.transaction.TransactionRepository
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.engagekit.rest.request.CardLockUnlockRequest
import com.engageft.engagekit.tools.TransactionsFilter
import com.engageft.engagekit.utils.AlertUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.ob.domain.lookup.TransactionStatus
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.TransactionInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import java.math.BigDecimal

/**
 *  DashboardViewModel
 *  </p>
 *  Supports DashboardExpandableView, DashboardFragment, and AuthenticatedActivity
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DashboardViewModel : BaseEngageViewModel() {
    val productCardViewModelDelegate = ProductCardViewDelegate(this)

    var expirationDateFormatString = "%1\$d/%2\$d" // provide a sensible default, and allow to be overridden

    // Balances
    var spendingBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var spendingBalanceStateObservable: MutableLiveData<DashboardBalanceState> = MutableLiveData()

    var savingsBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var savingsBalanceStateObservable: MutableLiveData<DashboardBalanceState> = MutableLiveData()

    // Transactions
    var allTransactionsObservable: MutableLiveData<List<TransactionInfo>> = MutableLiveData()
    var retrievingTransactionsFinishedObservable: MutableLiveData<Boolean> = MutableLiveData()

    // Room transactions
    val transactionsReadyObservable = MutableLiveData<Boolean>()
    lateinit var transactionsListObservable: LiveData<PagedList<Transaction>>

    var notificationsCountObservable: MutableLiveData<Int> = MutableLiveData()

    var transactionsTabPosition = TRANSACTIONS_TAB_POSITION_ALL

    val navigationObservable = MutableLiveData<DashboardNavigationEvent>()

    val animationObservable: MutableLiveData<DashboardAnimationEvent> = MutableLiveData()

    private lateinit var debitCardInfo: DebitCardInfo
    private var transactionsInitialized = false
    private var isRefreshing = false

    // ProductCardView
    fun initCardView() {
        productCardViewModelDelegate.updateCardView()
    }

    // Balances
    fun initBalancesAndNotifications() {
        spendingBalanceStateObservable.value = DashboardBalanceState.LOADING
        // only change savings state if already set. Otherwise it is currently hidden in UI, so don't show loading indicator
        if (savingsBalanceStateObservable.value == DashboardBalanceState.AVAILABLE) {
            savingsBalanceStateObservable.value = DashboardBalanceState.LOADING
        }
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                debitCardInfo?.apply {
                                    try {
                                        spendingBalanceObservable.value = BigDecimal(currentBalance)
                                        spendingBalanceStateObservable.value = DashboardBalanceState.AVAILABLE
                                    } catch (e: Throwable) {
                                        spendingBalanceObservable.value = BigDecimal.ZERO
                                        spendingBalanceStateObservable.value = DashboardBalanceState.ERROR
                                    }
                                } ?: run {
                                    // error getting debit card info
                                    spendingBalanceObservable.value = BigDecimal.ZERO
                                    spendingBalanceStateObservable.value = DashboardBalanceState.ERROR
                                }

                                try { // TODO(kurt): hide savings entirely if no goals/savings (criteria for hiding TBD -- see FOTM-230)
                                    val goalAmount = BigDecimal(response.goalsContributed)
                                    val savingsAmount = BigDecimal(response.savingsContributed)
                                    savingsBalanceObservable.value = goalAmount.plus(savingsAmount)
                                    savingsBalanceStateObservable.value = DashboardBalanceState.AVAILABLE
                                } catch (e: Throwable) {
                                    savingsBalanceObservable.value = BigDecimal.ZERO
                                    savingsBalanceStateObservable.value = DashboardBalanceState.ERROR
                                }

                                updateNotifications(response)
                            } else {
                                spendingBalanceObservable.value = BigDecimal.ZERO
                                spendingBalanceStateObservable.value = DashboardBalanceState.ERROR
                                savingsBalanceObservable.value = BigDecimal.ZERO
                                savingsBalanceStateObservable.value = DashboardBalanceState.ERROR
                                handleUnexpectedErrorResponse(response)
                            }
                        })
                        { e ->
                            spendingBalanceObservable.value = BigDecimal.ZERO
                            spendingBalanceStateObservable.value = DashboardBalanceState.ERROR
                            savingsBalanceObservable.value = BigDecimal.ZERO
                            savingsBalanceStateObservable.value = DashboardBalanceState.ERROR
                            handleThrowable(e)
                        }
        )
    }

    // Transactions
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
                                    debitCardInfo.let {
                                        transactionsListObservable = TransactionRepository.pagedTransactions(debitCardInfo.debitCardId)
                                        transactionsReadyObservable.postValue(true)
                                        //TransactionRepository.refreshTransactions(debitCardInfo.debitCardId)
                                    }
                                } else {
                                    handleUnexpectedErrorResponse(response)
                                }
                            })
                            { e ->
                                handleThrowable(e)
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

    private fun handleTransactionsListEvent(event: TransactionsListEvent) {
        if (isRefreshing) {
            isRefreshing = false
            (allTransactionsObservable.value as MutableList).clear()
        }
        if (event.isSuccessful) {
            val newTransactions = EngageService.getInstance().storageManager.transactionsStore.getTransactionsWithFilter(debitCardInfo.debitCardId, TransactionsFilter(event.year, event.month).addStatus(TransactionStatus.PENDING))
            // add new transactions to existing list, or create a new list if no existing list
            val transactionsList = allTransactionsObservable.value?.toMutableList() ?: run {
                mutableListOf<TransactionInfo>()
            }
            transactionsList.addAll(newTransactions)

            // TODO(kurt): only update observable once all in-flight requests have completed. Otherwise, we are doing
            // a lot of extra work to update recyclerview many times, once for each request completion.
            allTransactionsObservable.value = transactionsList
        }
    }

    // These are called by DashboardFragment in response to user presses in DashboardExpandableView.
    // May not be needed depending on how alerts and search are implemented. May not need DashboardFragment's
    // parent activity to observe this viewModel's navigationObservable, but leaving here just in case.
    fun showAlerts() {
        navigationObservable.value = DashboardNavigationEvent.ALERTS
    }

    fun showTransactionSearch() {
        navigationObservable.value = DashboardNavigationEvent.TRANSACTION_SEARCH
    }

    // DashboardExpandableView animation-related functions
    fun expandImmediate() {
        animationObservable.value = DashboardAnimationEvent.EXPAND_IMMEDIATE
    }

    fun expandStart() {
        animationObservable.value = DashboardAnimationEvent.EXPAND_START
    }

    fun expandEnd() {
        animationObservable.value = DashboardAnimationEvent.EXPAND_END
    }

    fun collapseStart() {
        animationObservable.value = DashboardAnimationEvent.COLLAPSE_START
    }

    fun collapseEnd() {
        animationObservable.value = DashboardAnimationEvent.COLLAPSE_END
    }

    private fun datesAreSameMonthAndYear(date1: DateTime?, date2: DateTime?): Boolean {
        return date1 != null && date2 != null && date1.year == date2.year && date1.monthOfYear == date2.monthOfYear
    }

    companion object {
        const val MAX_MONTHS_TO_LOAD = 6

        const val TRANSACTIONS_TAB_POSITION_ALL = 0
        const val TRANSACTIONS_TAB_POSITION_DEPOSITS = 1
    }

    fun updateCardLockStatus(lock: Boolean){
        engageApi().postLockCard(
                CardLockUnlockRequest(
                        EngageService.getInstance().storageManager.loginResponse.token,
                        EngageService.getInstance().storageManager.currentCard.debitCardId,
                        lock).fieldMap)
                .subscribeWithProgressAndDefaultErrorHandling<BasicResponse>(this, {
                    EngageService.getInstance().clearLoginAndDashboardResponses()
                    productCardViewModelDelegate.updateCardView() })
    }
}

enum class DashboardAnimationEvent {
    EXPAND_IMMEDIATE, EXPAND_START, EXPAND_END, COLLAPSE_START, COLLAPSE_END
}

enum class DashboardNavigationEvent {
    ALERTS, TRANSACTION_SEARCH
}

enum class DashboardBalanceState {
    LOADING, AVAILABLE, HIDDEN, ERROR
}