package com.engageft.fis.pscu.feature

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.event.TransactionsListEvent
import com.engageft.engagekit.tools.TransactionsFilter
import com.engageft.engagekit.utils.AlertUtils
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.DebitCardInfoUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.domain.lookup.TransactionStatus
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.SecureCardInfoResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.TransactionInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Months
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
    // ProductCardView
    var cardInfoModelObservable: MutableLiveData<ProductCardModel> = MutableLiveData()
    var cardStateObservable: MutableLiveData<CardState> = MutableLiveData()

    var expirationDateFormatString = "%1\$d/%2\$d" // provide a sensible default, and allow to be overridden

    // Balances
    var spendingBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var spendingBalanceStateObservable: MutableLiveData<DashboardBalanceState> = MutableLiveData()

    var savingsBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var savingsBalanceStateObservable: MutableLiveData<DashboardBalanceState> = MutableLiveData()

    // Transactions
    var allTransactionsObservable: MutableLiveData<List<TransactionInfo>> = MutableLiveData()
    var retrievingTransactionsFinishedObservable: MutableLiveData<Boolean> = MutableLiveData()

    var notificationsCountObservable: MutableLiveData<Int> = MutableLiveData()

    var transactionsTabPosition = TRANSACTIONS_TAB_POSITION_ALL

    private val compositeDisposable = CompositeDisposable()

    val navigationObservable = MutableLiveData<DashboardNavigationEvent>()

    val animationObservable: MutableLiveData<DashboardAnimationEvent> = MutableLiveData()

    private lateinit var debitCardInfo: DebitCardInfo
    private var transactionsInitialized = false
    private var isRefreshing = false

    // ProductCardView
    fun isShowingCardDetails(): Boolean {
        return cardStateObservable.value == CardState.DETAILS_SHOWN
    }

    fun isLocked(): Boolean {
        return cardInfoModelObservable.value?.cardLocked ?: false
    }

    fun initCardView() {
        updateCardView()
    }

    private fun updateCardView(secureCardInfoResponse: SecureCardInfoResponse? = null) {
        cardStateObservable.postValue(CardState.LOADING)
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                var productCardModel = ProductCardModel()
                                productCardModel.cardholderName = LoginResponseUtils.getUserFullname(response)
                                debitCardInfo?.let { cardInfo ->
                                    productCardModel.cardStatus = getCardModelStatusFromDebitCardInfo(cardInfo)
                                    productCardModel.cardStatusOkay = DebitCardInfoUtils.displayCardStatusAsOkay(cardInfo)
                                    productCardModel.cardLocked = DebitCardInfoUtils.isLocked(cardInfo)
                                    productCardModel.cardPendingActivation = DebitCardInfoUtils.isPendingActivation(cardInfo)
                                    secureCardInfoResponse?.let { response ->
                                        productCardModel.cardCvv = response.cvv
                                        BackendDateTimeUtils.parseDateTimeFromIso8601String(response.expiration)?.let { expirationDate ->
                                            productCardModel.cardExpirationMonthYear = String.format(expirationDateFormatString, expirationDate.monthOfYear, expirationDate.year)
                                        }
                                        productCardModel.cardNumberFull = response.pan
                                        cardStateObservable.postValue(CardState.DETAILS_SHOWN)
                                    } ?: run {
                                        productCardModel.cardNumberPartial = cardInfo.lastFour
                                        cardStateObservable.postValue(CardState.DETAILS_HIDDEN)
                                    }
                                    cardInfoModelObservable.postValue(productCardModel)
                                }
                            } else {
                                cardStateObservable.postValue(CardState.ERROR)
                            }
                        }) {
                            cardStateObservable.postValue(CardState.ERROR)
                        }

        )
    }

    fun showCardDetails() {
        val debitCardInfo = LoginResponseUtils.getCurrentCard(EngageService.getInstance().storageManager.loginResponse)
        if (debitCardInfo != null) {
            cardStateObservable.value = CardState.LOADING
            val observable = if (DebitCardInfoUtils.hasVirtualCard(debitCardInfo))
                EngageService.getInstance().debitVirtualCardInfoObservable(debitCardInfo)
            else
                EngageService.getInstance().debitSecureCardInfoObservable(debitCardInfo)

            compositeDisposable.add(observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        if (response.isSuccess && response is SecureCardInfoResponse) {
                            updateCardView(response)
                        } else {
                            cardStateObservable.value = CardState.DETAILS_HIDDEN
                            handleUnexpectedErrorResponse(response)
                        }
                    }) { e ->
                        cardStateObservable.value = CardState.DETAILS_HIDDEN
                        handleThrowable(e)
                    }
            )
        }
    }

    fun hideCardDetails() {
        updateCardView(null)
    }

    enum class CardState {
        LOADING,
        DETAILS_HIDDEN,
        DETAILS_SHOWN,
        ERROR
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
                                    // get 6 months of transactions (or less, if card request date less than 6 months ago)
                                    val numberMonthsToLoad = getCountOfMonthsToLoadAndReload()
                                    if (numberMonthsToLoad > 0) {
                                        loadMostRecentMonthlyTransactions(numberMonthsToLoad, useCached)
                                    } else {
                                        // this could happen if using a malformed test account with isoRequestDate in the future
                                        progressOverlayShownObservable.value = false
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
                                handleThrowable(e)
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
            val transactionsList = allTransactionsObservable.value?.toMutableList() ?: run {
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

    private fun getCardModelStatusFromDebitCardInfo(debitCardInfo: DebitCardInfo): ProductCardModelCardStatus {
        return if (DebitCardInfoUtils.hasVirtualCard(debitCardInfo) && EngageService.getInstance().engageConfig.virtualCardEnabled)
            ProductCardModelCardStatus.CARD_STATUS_VIRTUAL
        else if (DebitCardInfoUtils.isLocked(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_LOCKED
        else if (DebitCardInfoUtils.isPendingActivation(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_PENDING
        else if (DebitCardInfoUtils.isLostStolen(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_REPLACED
        else if (DebitCardInfoUtils.isCancelled(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_CANCELED
        else if (DebitCardInfoUtils.isSuspended(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_SUSPENDED
        else if (DebitCardInfoUtils.isFraudStatus(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_CLOSED
        else if (DebitCardInfoUtils.isOrdered(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_ORDERED
        else
            ProductCardModelCardStatus.CARD_STATUS_ACTIVE
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