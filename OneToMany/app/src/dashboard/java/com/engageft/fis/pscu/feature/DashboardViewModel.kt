package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.repository.transaction.TransactionRepository
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.engagekit.repository.util.Listing
import com.engageft.engagekit.utils.AlertUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.engageft.fis.pscu.feature.gatekeeping.DashboardGateKeeper
import com.engageft.fis.pscu.feature.gatekeeping.GateKeeperListener
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.OnboardingGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.PendingCardActivationGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.Post30DaysGatedItem
import com.ob.domain.lookup.TransactionType
import com.ob.domain.lookup.branding.BrandingCard
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

/**
 *  DashboardViewModel
 *  </p>
 *  Supports DashboardExpandableView, DashboardFragment, and AuthenticatedActivity
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DashboardViewModel : BaseEngageViewModel(), GateKeeperListener {
    val productCardViewModelDelegate = ProductCardViewDelegate(this)

    var expirationDateFormatString = "%1\$d/%2\$d" // provide a sensible default, and allow to be overridden

    // Balances
    var spendingBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var spendingBalanceStateObservable: MutableLiveData<DashboardBalanceState> = MutableLiveData()

    var savingsBalanceObservable: MutableLiveData<BigDecimal> = MutableLiveData()
    var savingsBalanceStateObservable: MutableLiveData<DashboardBalanceState> = MutableLiveData()

    // Room transactions
    private var allTransactionsListing: Listing<Transaction>? = null
    private var depositTransactionsListing: Listing<Transaction>? = null
    private val observedTransactionsListing = MutableLiveData<Listing<Transaction>>()
    val transactions = Transformations.switchMap(observedTransactionsListing) { it.pagedList }!!
    val networkState = Transformations.switchMap(observedTransactionsListing) { it.networkState }!!

    var notificationsCountObservable: MutableLiveData<Int> = MutableLiveData()

    var transactionsTabPosition = TRANSACTIONS_TAB_POSITION_ALL

    val navigationObservable = MutableLiveData<DashboardNavigationEvent>()

    val animationObservable: MutableLiveData<DashboardAnimationEvent> = MutableLiveData()

    val brandingCardObservable: MutableLiveData<BrandingCard> = MutableLiveData()

    val expandableOptionsItems = MutableLiveData<List<ExpandableViewListItem>>()

    private val dashboardGateKeeper = DashboardGateKeeper(compositeDisposable, this)

    private lateinit var debitCardInfo: DebitCardInfo

    init {
        expandableOptionsItems.value = ArrayList()
    }

    // ProductCardView
    fun initCardView() {
        productCardViewModelDelegate.updateCardView()
        refreshExpandableListItems()
    }

    fun refreshExpandableListItems() {
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                updateExpandableViewOptions(response)
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        })
                        { e ->
                            handleThrowable(e)
                        }
        )
    }

    // Balances
    fun clearAndRefreshBalancesAndNotifications() {
        progressOverlayShownObservable.value = true
        EngageService.getInstance().clearLoginAndDashboardResponses()
        refreshBalancesAndNotifications()
    }

    fun refreshBalancesAndNotifications() {
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

                                    // Find the BrandingCard that matches the current card type. This could be null
                                    // and null is handled in the view.
                                    brandingCardObservable.value = BrandingInfoRepo.cards?.find { card ->
                                        card.type == cardType
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
    fun clearAndRefreshAllTransactions() {
        val repoTypes = listOf(TransactionRepository.TransactionRepoType.ALL_ACTIVITY, TransactionRepository.TransactionRepoType.DEPOSITS)
        clearTransactions(repoTypes) { refreshTransactions(repoTypes) }
    }

    private fun clearTransactions(repoTypes: List<TransactionRepository.TransactionRepoType>, callBack: (() -> Unit)? = null) {
        compositeDisposable.add(
                Observable.fromCallable { TransactionRepository.clearTransactions(repoTypes) }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .subscribe {
                            if (callBack != null) {
                                callBack()
                            }
                        }
        )
    }

    private fun refreshTransactions(transactionTypes: List<TransactionRepository.TransactionRepoType>) {
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                debitCardInfo.let {
                                    if (transactionTypes.contains(TransactionRepository.TransactionRepoType.ALL_ACTIVITY)) {
                                        allTransactionsListing = TransactionRepository.pagedTransactions(TransactionRepository.TransactionRepoType.ALL_ACTIVITY, debitCardInfo.debitCardId, null)
                                    }
                                    if (transactionTypes.contains(TransactionRepository.TransactionRepoType.DEPOSITS)) {
                                        depositTransactionsListing = TransactionRepository.pagedTransactions(TransactionRepository.TransactionRepoType.DEPOSITS, debitCardInfo.debitCardId, TransactionType.LOAD.name)
                                    }

                                    if (transactionsTabPosition == TRANSACTIONS_TAB_POSITION_ALL) {
                                        showAllActivity(reselect = true)
                                    } else {
                                        showDeposits(reselect = true)
                                    }
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

    fun showAllActivity(reselect: Boolean = false) {
        if (reselect || transactionsTabPosition != TRANSACTIONS_TAB_POSITION_ALL) {
            transactionsTabPosition = TRANSACTIONS_TAB_POSITION_ALL
            observedTransactionsListing.value = allTransactionsListing
        }
    }

    fun showDeposits(reselect: Boolean = false) {
        if (reselect || transactionsTabPosition != TRANSACTIONS_TAB_POSITION_DEPOSITS) {
            transactionsTabPosition = TRANSACTIONS_TAB_POSITION_DEPOSITS
            observedTransactionsListing.value = depositTransactionsListing
        }
    }

    fun runGateKeeper() {
        dashboardGateKeeper.run()
    }

    override fun onGateOpen() {
        // Do nothing, just stay on this screen.
    }

    override fun onGatedItemFailed(item: GatedItem) {
        when (item) {
            is PendingCardActivationGatedItem -> {
                showCardTracker()
            }
            is OnboardingGatedItem -> {
                showOnboarding()
            }
            is Post30DaysGatedItem -> {
                showPost30DaysSplash()
            }
        }
    }

    override fun onItemError(item: GatedItem, e: Throwable?, message: String?) {
        message?.let{
            handleUnexpectedErrorResponse(BasicResponse(false, it))
        }
        e?.let {
            handleThrowable(it)
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

    fun updateExpandableViewOptions(response: LoginResponse?) {
        response?.let { loginResponse ->
            val cardPermissionsInfo = LoginResponseUtils.getCurrentCard(loginResponse).cardPermissionsInfo
            val accountPermissionsInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse).accountPermissionsInfo
            val options = ArrayList<ExpandableViewListItem>()

            // The order of this logic matters as items are displayed in the order they're added to the list.
            if (cardPermissionsInfo.isEnableCardPAN && cardPermissionsInfo.isAllowCardPAN) {
                if (productCardViewModelDelegate.isShowingCardDetails()) {
                    options.add(ExpandableViewListItem.HideCardDetailsItem)
                } else {
                    options.add(ExpandableViewListItem.ShowCardDetailsItem)
                }
            }
            if (accountPermissionsInfo.isFundingEnabled) {
                options.add(ExpandableViewListItem.MoveMoneyItem)
            }
            if (cardPermissionsInfo.isEnableLockCard) {
                val cardLocked = productCardViewModelDelegate.isLocked()
                if (cardLocked) {
                    options.add(ExpandableViewListItem.UnlockCardItem)
                } else {
                    options.add(ExpandableViewListItem.LockCardItem)
                }
            }
            if (cardPermissionsInfo.isEnableChangePIN && cardPermissionsInfo.isAllowChangePIN) {
                options.add(ExpandableViewListItem.ChangeCardPinItem)
            }
            if (cardPermissionsInfo.isEnableReplaceCard) {
                options.add(ExpandableViewListItem.ReplaceCardItem(cardPermissionsInfo.isAllowReplaceCard))
            }
            if (cardPermissionsInfo.isEnableLostStolenCard) {
                options.add(ExpandableViewListItem.ReportLostStolenItem(cardPermissionsInfo.isAllowLostStolenCard))
            }
            if (cardPermissionsInfo.isEnableCancelCard) {
                options.add(ExpandableViewListItem.CancelCardItem(cardPermissionsInfo.isAllowCancelCard))
            }

            // Now we alter the list based on its size. If the size is four or less, we keep the list as-is.
            // If the size is exactly five, we remove the last two items and add them to a MoreOptionsItem.
            // If the size is larger, we insert all items after four to the list.
            if (options.size > 4) {
                if (options.size == 5) {
                    val itemFour = options.removeAt(3)
                    val itemFive = options.removeAt(4)

                    val moreOptionsList = ArrayList<ExpandableViewListItem>().apply {
                        add(itemFour)
                        add(itemFive)
                    }
                    options.add(ExpandableViewListItem.MoreOptionsItem(moreOptionsList))
                } else {
                    val moreOptionsList = ArrayList<ExpandableViewListItem>().apply {
                        while (options.size > 3) {
                            add(options.removeAt(3))
                        }
                    }
                    options.add(ExpandableViewListItem.MoreOptionsItem(moreOptionsList))
                }
            }
            expandableOptionsItems.value = options
        } ?: kotlin.run {
            expandableOptionsItems.value = ArrayList()
        }
    }

    // These are called by DashboardFragment in response to user presses in DashboardExpandableView.
    // May not be needed depending on how alerts and search are implemented. May not need DashboardFragment's
    // parent activity to observe this viewModel's navigationObservable, but leaving here just in case.
    fun showCardTracker() {
        navigationObservable.value = DashboardNavigationEvent.CARD_TRACKER
        navigationObservable.postValue(DashboardNavigationEvent.NONE)
    }

    fun showOnboarding() {
        navigationObservable.value = DashboardNavigationEvent.SHOW_ONBOARDING_SPLASH
        navigationObservable.postValue(DashboardNavigationEvent.NONE)
    }

    fun showPost30DaysSplash() {
        navigationObservable.value = DashboardNavigationEvent.SHOW_POST_30_DAYS_SPLASH
        navigationObservable.postValue(DashboardNavigationEvent.NONE)
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

    companion object {
        const val TRANSACTIONS_TAB_POSITION_ALL = 0
        const val TRANSACTIONS_TAB_POSITION_DEPOSITS = 1
    }
}

enum class DashboardAnimationEvent {
    EXPAND_IMMEDIATE, EXPAND_START, EXPAND_END, COLLAPSE_START, COLLAPSE_END
}

enum class DashboardNavigationEvent {
    NONE, ALERTS, TRANSACTION_SEARCH, SECURITY_QUESTIONS, CARD_TRACKER, SHOW_ONBOARDING_SPLASH, SHOW_POST_30_DAYS_SPLASH
}

enum class DashboardBalanceState {
    LOADING, AVAILABLE, HIDDEN, ERROR
}