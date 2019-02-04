package com.engageft.fis.pscu.feature

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.engagekit.repository.util.NetworkState
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentDashboardBinding
import com.engageft.fis.pscu.feature.adapter.DashboardTransactionsAdapter
import com.engageft.fis.pscu.feature.authentication.AuthenticationDialogFragment
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.search.SearchDialogFragment
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionListener
import com.engageft.fis.pscu.feature.utils.CardStatusUtils
import com.ob.domain.lookup.DebitCardStatus
import com.ob.domain.lookup.branding.BrandingCard
import eightbitlab.com.blurview.RenderScriptBlur
import utilGen1.StringUtils
import java.math.BigDecimal

/**
 * DashboardFragment
 * <p>
 * UI Fragment for the Dashboard.
 * </p>
 * Created by Kurt Mueller on 4/17/18.
 * Ported to gen2 by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DashboardFragment : BaseEngagePageFragment(),
        DashboardExpandableView.DashboardExpandableViewListener,
        DashboardTransactionsAdapter.DashboardTransactionsAdapterListener,
        TransactionListener {

    private lateinit var binding: FragmentDashboardBinding

    private lateinit var dashboardViewModel: DashboardViewModel

    private val cardModelObserver = Observer<ProductCardModel> { updateForCardModel(it!!) }
    private val cardStateObserver = Observer<ProductCardViewCardState> { updateForCardState(it!!) }

    private val spendingBalanceObserver = Observer<BigDecimal> { updateSpendingBalance(it) }
    private val spendingBalanceStateObserver = Observer<DashboardBalanceState> { updateSpendingBalanceState(it!!) }

    private val savingsBalanceObserver = Observer<BigDecimal> { updateSavingsBalance(it) }
    private val savingsBalanceStateObserver = Observer<DashboardBalanceState> { updateSavingsBalanceState(it!!) }

    private val transactionsObserver = Observer<PagedList<Transaction>> {
        pagedList -> transactionsAdapter.submitList(pagedList)
    }
    private val networkStateObserver = Observer<NetworkState> { transactionsAdapter.setNetworkState(it) }

    private val notificationsObserver = Observer<Int> { activity?.invalidateOptionsMenu() }

    private val animationObserver = Observer<DashboardAnimationEvent> { updateAnimation(it!!) }

    private val brandingCardObserver = Observer<BrandingCard> { updateBrandingCard(it) }
    private val cardOptionsObserver = Observer<List<ExpandableViewListItem>> { updateExpandableListItems(it) }

    private var toolbarShadowAnimationScrollRange: Int = 0
    private var toolbarShadowAnimationScrollRangeFloat: Float = 0F

    private lateinit var transactionsAdapter: DashboardTransactionsAdapter

    private var scrollDisabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        transactionsAdapter = DashboardTransactionsAdapter(dashboardViewModel.compositeDisposable, this, this, dashboardViewModel::clearAndRefreshAllTransactions)
    }

    override fun createViewModel(): BaseViewModel? {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        return dashboardViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_dashboard, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarShadowAnimationScrollRange = context!!.resources.getDimensionPixelSize(R.dimen.dashboard_toolbar_shadow_animation_scroll_range)
        toolbarShadowAnimationScrollRangeFloat = toolbarShadowAnimationScrollRange.toFloat()

        binding.dashboardExpandableView.listener = this

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionsAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // This is a potential UI slowdown, but there's no other way I can find to know precisely how
                    // far the recyclerview has scrolled. This was recommended here, by a member of the Android team:
                    // https://stackoverflow.com/questions/29581782/how-to-get-the-scrollposition-in-the-recyclerview-layoutmanager
                    val scrollY = recyclerView.computeVerticalScrollOffset()
                    when {
                        scrollY == 0 -> binding.toolbarShadowView.visibility = View.INVISIBLE
                        scrollY <= toolbarShadowAnimationScrollRange -> {
                            binding.toolbarShadowView.alpha = scrollY / toolbarShadowAnimationScrollRangeFloat
                            binding.toolbarShadowView.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.toolbarShadowView.alpha = 1F
                            binding.toolbarShadowView.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }

        // allows disabling scrollview
        binding.transactionsRecyclerView.setOnTouchListener { _, _ ->
            scrollDisabled
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            dashboardViewModel.clearAndRefreshBalancesAndNotifications()
            if (binding.transactionsRecyclerView.itemAnimator == null) {
                binding.transactionsRecyclerView.itemAnimator = DefaultItemAnimator()
            }
            dashboardViewModel.clearAndRefreshAllTransactions()
            // viewModel will trigger showing regular activity indicator. Don't show swipe refresh indicator too.
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // lot of work to get a float from dimens!
        val outValue = TypedValue()
        resources.getValue(R.dimen.blur_radius, outValue, true)
        val blurRadius = outValue.float
        binding.blurView.setupWith(binding.rootLayout)
                .setBlurAlgorithm(RenderScriptBlur(context))
                .setBlurRadius(blurRadius)

        initViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        menu?.clear()

        inflater?.inflate(R.menu.menu_dashboard_fragment, menu)
//        val notificationsItem = menu?.findItem(R.id.action_notifications)
//        notificationsItem?.apply {
//            val icon = this.icon as LayerDrawable
//            var notificationsCount = 0
//            viewModel.notificationsCountObservable.value?.let {
//                notificationsCount = it
//            }
//            BadgeUtils.setBadgeCount(activity, icon, notificationsCount)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When overviewView is expanded, toolbar alpha is 0F but options menu items are still clickable.
        // Ignore clicks in this case.
        if (!binding.dashboardExpandableView.showingActions) {
            val id = item.itemId

            if (id == R.id.action_notifications) {
                //listener?.onAlertsNotificationsOptionsItem()
                return true
            } else if (id == R.id.action_search_transactions) {
                SearchDialogFragment().show(activity!!.supportFragmentManager, "Search")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateForCardModel(productCardModel: ProductCardModel) {
        productCardModel.cardStatusText = CardStatusUtils.cardStatusStringForProductCardModelCardStatus(context!!, productCardModel.cardStatus).toString()

        // update ProductCardView in recyclerview, initially visible
        transactionsAdapter.productCardModel = productCardModel

        // update ProductCardView in expandable overlay, initially invisible
        binding.dashboardExpandableView.cardView.updateWithProductCardModel(productCardModel)

        dashboardViewModel.refreshExpandableListItems()
    }

    private fun updateForCardState(cardState: ProductCardViewCardState) {
        dashboardViewModel.refreshExpandableListItems()
        when (cardState) {
            ProductCardViewCardState.LOADING -> {
                // Do nothing
            }
            ProductCardViewCardState.DETAILS_HIDDEN -> {
                // Do nothing
            }
            ProductCardViewCardState.DETAILS_SHOWN -> {
                // Do nothing
            }
            ProductCardViewCardState.ERROR -> {
                fragmentDelegate.showDialog(
                        InformationDialogFragment.newLotusInstance(
                                message = getString(R.string.OVERVIEW_CARD_ERROR_DIALOG_MESSAGE),
                                buttonPositiveText = getString(R.string.dialog_information_ok_button)
                        )
                )
            }
        }

        // don't show screen image in task switcher if showing card details
        preventScreenCapture(dashboardViewModel.productCardViewModelDelegate.isShowingCardDetails())
    }

    private fun updateSpendingBalance(spendingBalance: BigDecimal?) {
        spendingBalance?.let {
            transactionsAdapter.spendingBalanceAmount = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(spendingBalance.toFloat(), 0.5f, true)
        } ?: run {
            // TODO(kurt) what do show when no value?
            transactionsAdapter.spendingBalanceAmount = "..."
        }
    }

    private fun updateSpendingBalanceState(spendingBalanceState: DashboardBalanceState) {
        when (spendingBalanceState) {
            DashboardBalanceState.LOADING -> transactionsAdapter.spendingBalanceAmount = getString(R.string.OVERVIEW_BALANCE_LOADING)
            DashboardBalanceState.ERROR -> transactionsAdapter.spendingBalanceAmount = getString(R.string.OVERVIEW_BALANCE_ERROR)
            // don't change for other states
            else -> {}
        }
    }

    private fun updateSavingsBalance(savingsBalance: BigDecimal?) {
        savingsBalance?.let {
            transactionsAdapter.savingsBalanceAmount = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(savingsBalance.toFloat(), 0.5f, true)
            transactionsAdapter.setSavingsBalanceVisibility(visible = true)
        } ?: run {
            transactionsAdapter.setSavingsBalanceVisibility(visible = false)
        }
    }

    private fun updateSavingsBalanceState(savingsBalanceState: DashboardBalanceState) {
        when (savingsBalanceState) {
            DashboardBalanceState.LOADING -> {
                transactionsAdapter.savingsBalanceAmount = getString(R.string.OVERVIEW_BALANCE_LOADING)
                transactionsAdapter.setSavingsBalanceVisibility(visible = true)
            }
            DashboardBalanceState.HIDDEN -> {
                transactionsAdapter.setSavingsBalanceVisibility(visible = false)
            }
            DashboardBalanceState.AVAILABLE -> {
                transactionsAdapter.setSavingsBalanceVisibility(visible = true)
            }
            DashboardBalanceState.ERROR -> transactionsAdapter.savingsBalanceAmount = getString(R.string.OVERVIEW_BALANCE_ERROR)
        }
    }

    private fun updateAnimation(animationEvent: DashboardAnimationEvent) {
        when (animationEvent) {
            DashboardAnimationEvent.EXPAND_IMMEDIATE -> {
                //navView.visibility = View.GONE // for bottom nav
                toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)
            }
            DashboardAnimationEvent.EXPAND_START -> {
                //navView.visibility = View.GONE // for bottom nav
                toolbarController.animateToolbarVisibility(ToolbarVisibilityState.INVISIBLE, animationDurationMs = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong())
            }
            DashboardAnimationEvent.EXPAND_END -> {
                // intentionally left blank
            }
            DashboardAnimationEvent.COLLAPSE_START -> {
                //navView.visibility = View.VISIBLE // for bottom nav
                toolbarController.animateToolbarVisibility(ToolbarVisibilityState.VISIBLE, animationDurationMs = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong())
            }
            DashboardAnimationEvent.COLLAPSE_END -> {
                // intentionally left blank
            }
        }
    }

    private fun updateBrandingCard(brandingCard: BrandingCard?) {
        brandingCard?.let {
            binding.dashboardExpandableView.cardView.applyBranding(it,dashboardViewModel.compositeDisposable){ e ->
                Toast.makeText(context, "Failed to retrieve card image", Toast.LENGTH_SHORT).show()
                Log.e("BRANDING_INFO_FAIL", e.message)
                //TODO(ttkachuk) right now it is not clear on how we should handle failure to retrieve the card image
                //tracked in FOTM-497
            }
        }
    }

    fun updateExpandableListItems(items : List<ExpandableViewListItem>) {
        binding.dashboardExpandableView.setExpandableListItems(items)
    }

    fun showMessageContainerWithView(messageView: View) {
        binding.messageContainer.addView(messageView)
        binding.messageContainer.visibility = View.VISIBLE
        binding.dashboardExpandableView.showExpandCollapseButton(false)
    }

    fun hideMessageContainer() {
        binding.messageContainer.visibility = View.INVISIBLE
        binding.messageContainer.removeAllViews()
        binding.dashboardExpandableView.showExpandCollapseButton(true)
    }

    // If the dashboardExpandableView is expanded when the device back button is pressed, collapseImmediate it.
    fun handleBackPressed(): Boolean {
        return if (binding.dashboardExpandableView.showingActions) {
            binding.dashboardExpandableView.showActions(show = false)
            true
        } else {
            false
        }
    }

    private fun preventScreenCapture(prevent: Boolean) {
        if (prevent) {
            activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        else {
            activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // for use when restoring DashboardExpandableView in onSaveInstanceState
    private fun showObscuringOverlayImmediate() {
        scrollDisabled = true
        transactionsAdapter.transactionSelectionEnabled = false
        binding.blurView.visibility = View.VISIBLE
        binding.blurView.alpha = 1F
    }

    private fun showObscuringOverlay(show: Boolean) {
        // animate alpha of obscuring overlay over transactions -- fade in as DashboardExpandableView expands
        val alphaEnd: Float

        if (show) {
            alphaEnd = 1F

            // Don't let user scroll or click transactions when DashboardExpandableView is expanded
            scrollDisabled = true
            transactionsAdapter.transactionSelectionEnabled = false
        } else {
            alphaEnd = 0F
        }

        val alphaAnimator = ObjectAnimator.ofFloat(binding.blurView,"alpha", alphaEnd)
        alphaAnimator.duration = binding.dashboardExpandableView.animationDurationMs
        binding.blurView.visibility = View.VISIBLE
        alphaAnimator.start()
    }

    private fun initViewModel() {
        dashboardViewModel = ViewModelProviders.of(activity!!).get(DashboardViewModel::class.java)
        dashboardViewModel.expirationDateFormatString = getString(R.string.MONTH_YEAR_FORMAT)
        dashboardViewModel.productCardViewModelDelegate.cardInfoModelObservable.observe(this, cardModelObserver)
        dashboardViewModel.productCardViewModelDelegate.cardStateObservable.observe(this, cardStateObserver)
        dashboardViewModel.expandableOptionsItems.observe(this, cardOptionsObserver)
        dashboardViewModel.initCardView()

        dashboardViewModel.spendingBalanceObservable.observe(this, spendingBalanceObserver)
        dashboardViewModel.spendingBalanceStateObservable.observe(this, spendingBalanceStateObserver)

        dashboardViewModel.savingsBalanceObservable.observe(this, savingsBalanceObserver)
        dashboardViewModel.savingsBalanceStateObservable.observe(this, savingsBalanceStateObserver)

        dashboardViewModel.networkState.observe(this, networkStateObserver)
        dashboardViewModel.transactions.observe(this, transactionsObserver)

        // make sure correct tab is showing, after return from TransactionDetailFragment, in particular
        when (dashboardViewModel.transactionsTabPosition) {
            DashboardViewModel.TRANSACTIONS_TAB_POSITION_ALL -> {
                transactionsAdapter.selectedDashboardHeaderTabIndex = DashboardViewModel.TRANSACTIONS_TAB_POSITION_ALL
            }
            DashboardViewModel.TRANSACTIONS_TAB_POSITION_DEPOSITS -> {
                transactionsAdapter.selectedDashboardHeaderTabIndex = DashboardViewModel.TRANSACTIONS_TAB_POSITION_DEPOSITS
            }
        }

        dashboardViewModel.clearAndRefreshAllTransactions()

        dashboardViewModel.notificationsCountObservable.observe(this, notificationsObserver)

        dashboardViewModel.animationObservable.observe(this, animationObserver)

        dashboardViewModel.brandingCardObservable.observe(this, brandingCardObserver)

        dashboardViewModel.navigationObservable.observe(this, Observer {dashboardNavigationEvent ->
            when (dashboardNavigationEvent) {
                DashboardNavigationEvent.ALERTS -> {
                    Toast.makeText(context!!, "TODO: Navigate to Alerts", Toast.LENGTH_LONG).show()
                }
                DashboardNavigationEvent.TRANSACTION_SEARCH -> {
                    Toast.makeText(context!!, "TODO: Navigate to Transaction Search", Toast.LENGTH_LONG).show()
                }
                DashboardNavigationEvent.CARD_TRACKER -> {
                    Toast.makeText(context!!, "TODO: Navigate to Card Tracker", Toast.LENGTH_LONG).show()
                }
                DashboardNavigationEvent.SHOW_ONBOARDING_SPLASH -> {
                    Toast.makeText(context!!, "TODO: Navigate to Onboarding Splash", Toast.LENGTH_LONG).show()
                }
                DashboardNavigationEvent.SHOW_POST_30_DAYS_SPLASH -> {
                    Toast.makeText(context!!, "TODO: Navigate to Post 30 Days Splash", Toast.LENGTH_LONG).show()
                }
            }
        })

        dashboardViewModel.refreshBalancesAndNotifications()
    }

    private fun expand(animate: Boolean) {
        if (animate) {
            // if the recyclerview is not at the top, scroll to top now. Otherwise card views in
            // recyclerview and invisible overlay will not be aligned when overlay becomes visible.
            binding.transactionsRecyclerView.scrollToPosition(0)
            // also hide toolbar shadow
            binding.toolbarShadowView.visibility = View.INVISIBLE

            binding.dashboardExpandableView.visibility = View.VISIBLE
            binding.dashboardExpandableView.showActions(true)
        } else {
            binding.dashboardExpandableView.showActionsImmediate()
            binding.dashboardExpandableView.visibility = View.VISIBLE
        }
    }

    private fun collapseImmediate() {
            binding.dashboardExpandableView.showActions(false)
            // hide view after collapseImmediate handled in onCollapseEnd()
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.runGateKeeper()
    }

    override fun onExpandImmediate() {
        showObscuringOverlayImmediate()
        dashboardViewModel.expandImmediate()

        binding.blurView.setOnClickListener {
            collapseImmediate()
        }
    }

    override fun onExpandStart() {
        showObscuringOverlay(true)
        dashboardViewModel.expandStart()

        binding.blurView.setOnClickListener {
            collapseImmediate()
        }
    }

    override fun onExpandEnd() {
        dashboardViewModel.expandEnd()
    }

    override fun onCollapseStart() {
        dashboardViewModel.productCardViewModelDelegate.hideCardDetails()
        showObscuringOverlay(false)
        dashboardViewModel.collapseStart()

        binding.blurView.setOnClickListener(null)
    }

    override fun onCollapseEnd() {
        binding.dashboardExpandableView.visibility = View.INVISIBLE
        binding.blurView.visibility = View.INVISIBLE
        scrollDisabled = false
        transactionsAdapter.transactionSelectionEnabled = true
        dashboardViewModel.collapseEnd()
    }

    override fun onShowCardDetails() {
        if (!dashboardViewModel.productCardViewModelDelegate.isShowingCardDetails()) {
            val authDialogFragment = AuthenticationDialogFragment.newInstance(
                    getString(R.string.OVERVIEW_SHOW_CARD_DETAILS_AUTHENTICATION_MESSAGE)
            ) {
                dashboardViewModel.productCardViewModelDelegate.showCardDetails()
            }

            authDialogFragment.show(childFragmentManager, AuthenticationDialogFragment.TAG)
        }
    }

    override fun onHideCardDetails() {
        if (dashboardViewModel.productCardViewModelDelegate.isShowingCardDetails()) {
            dashboardViewModel.productCardViewModelDelegate.hideCardDetails()
        }
    }

    override fun onMoveMoney() {
        binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_moveMoneyFragment)
    }

    override fun onLockCard() {
        binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_cardLockUnlockFragment)
    }

    override fun onUnlockCard() {
        binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_cardLockUnlockFragment)
    }

    override fun onChangePin() {
        val authDialogFragment = AuthenticationDialogFragment.newInstance(
                getString(R.string.OVERVIEW_CHANGE_CARD_PIN_AUTHENTICATION_MESSAGE)
        ) { binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_cardPinFragment) }

        authDialogFragment.show(childFragmentManager, AuthenticationDialogFragment.TAG)
    }

    override fun onReplaceCard() {
        if (EngageService.getInstance().storageManager.currentCard.status == DebitCardStatus.ACTIVE) {
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_replaceCardFragment)
        } else {
            val bundle = Bundle().apply {
                putSerializable(CardFeatureNotAvailableFragment.KEY_UNAVAILABLE_FEATURE, CardFeatureNotAvailableFragment.UnavailableFeatureType.REPLACE)
            }
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_featureNotAvailable, bundle)
        }
    }

    override fun onReportCardLostStolen() {
        if(EngageService.getInstance().storageManager.currentCard.status == DebitCardStatus.ACTIVE ||
                EngageService.getInstance().storageManager.currentCard.status == DebitCardStatus.REPLACEMENT_ORDERED) {
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_reportLostStolenCardFragment)
        }
        else {
            val bundle = Bundle().apply {
                putSerializable(CardFeatureNotAvailableFragment.KEY_UNAVAILABLE_FEATURE, CardFeatureNotAvailableFragment.UnavailableFeatureType.LOST_STOLEN)
            }
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_featureNotAvailable, bundle)
        }
    }

    override fun onCancelCard() {
        if(EngageService.getInstance().storageManager.currentCard.status == DebitCardStatus.ACTIVE) {
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_cancelCardFragment)
        }
        else {
            val bundle = Bundle().apply {
                putSerializable(CardFeatureNotAvailableFragment.KEY_UNAVAILABLE_FEATURE, CardFeatureNotAvailableFragment.UnavailableFeatureType.CANCEL)
            }
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_featureNotAvailable, bundle)
        }
    }

    // DashboardTransactionsAdapter.DashboardTransactionsAdapterListener
    override fun onExpandClicked() {
        expand(true)
    }

    // DashboardTransactionsAdapter.DashboardTransactionsAdapterListener
    override fun onSpendingBalanceClicked() {
        if (!binding.dashboardExpandableView.showingActions) {
            Toast.makeText(activity, "Spending balance selected", Toast.LENGTH_SHORT).show()
        }
    }

    // DashboardTransactionsAdapter.DashboardTransactionsAdapterListener
    override fun onSavingsBalanceClicked() {
        if (!binding.dashboardExpandableView.showingActions) {
            binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_goalsListFragment)
        }
    }

    // DashboardTransactionsAdapter.DashboardTransactionsAdapterListener
    override fun onAllActivityClicked() {
        binding.transactionsRecyclerView.itemAnimator = null
        dashboardViewModel.showAllActivity()
    }

    // DashboardTransactionsAdapter.DashboardTransactionsAdapterListener
    override fun onDepositsClicked() {
        binding.transactionsRecyclerView.itemAnimator = null
        dashboardViewModel.showDeposits()
    }

    // TransactionsPagedAdapter.OnTransactionsAdapterListener
    override fun onTransactionSelected(transaction: Transaction) {
        // TODO(kurt): Pass transactionInfo to TransactionDetailFragment through navigation bundle (see onMoveMoney(), above)
        Toast.makeText(activity, "Transaction selected: " + transaction.store, Toast.LENGTH_SHORT).show()
    }
}