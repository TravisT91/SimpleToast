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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.repository.transaction.TransactionRepository
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentDashboardBinding
import com.engageft.fis.pscu.feature.adapter.DashboardTransactionsAdapter
import com.engageft.fis.pscu.feature.authentication.AuthenticationDialogFragment
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionListener
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionsSimpleAdapter
import com.engageft.fis.pscu.feature.utils.cardStatusStringRes
import com.ob.domain.lookup.DebitCardStatus
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
class DashboardFragment : BaseEngageFullscreenFragment(),
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

    private val searchObserver = Observer<List<Transaction>> {
        transactionList -> displaySearchResults(transactionList)
    }

    private val notificationsObserver = Observer<Int> { activity?.invalidateOptionsMenu() }

    private val animationObserver = Observer<DashboardAnimationEvent> { updateAnimation(it!!) }

    private var toolbarShadowAnimationScrollRange: Int = 0
    private var toolbarShadowAnimationScrollRangeFloat: Float = 0F

    private lateinit var transactionsAdapter: DashboardTransactionsAdapter
    private val searchAdapter: TransactionsSimpleAdapter by lazy {
        binding.searchRecyclerView.adapter = TransactionsSimpleAdapter(this)
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.searchRecyclerView.adapter as TransactionsSimpleAdapter
    }

    private var scrollDisabled = false

//    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
//        override fun onClick(): Boolean {
//            return if (changePasswordViewModel.hasUnsavedChanges()) {
//                showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
//                true
//            } else {
//                false
//            }
//        }
//    }

    override fun createViewModel(): BaseViewModel? {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        return dashboardViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_dashboard, container, false)
        //TODO(ttkachuk) right now card types are no specified by the backend, but we will select the BrandingCard that matches debitCardInfo.cardType when the backend is updated
        //tracked in FOTM-498
        BrandingInfoRepo.cards?.get(0)?.let {
            binding.dashboardExpandableView.cardView.applyBranding(it,dashboardViewModel.compositeDisposable){ e ->
                Toast.makeText(context, "Failed to retrieve card image", Toast.LENGTH_SHORT).show()
                Log.e("BRANDING_INFO_FAIL", e.message)
                //TODO(ttkachuk) right now it is not clear on how we should handle failure to retrieve the card image
                //tracked in FOTM-497
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarShadowAnimationScrollRange = context!!.resources.getDimensionPixelSize(R.dimen.dashboard_toolbar_shadow_animation_scroll_range)
        toolbarShadowAnimationScrollRangeFloat = toolbarShadowAnimationScrollRange.toFloat()

        binding.dashboardExpandableView.listener = this

        transactionsAdapter = DashboardTransactionsAdapter(dashboardViewModel.compositeDisposable, this, this)
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
            dashboardViewModel.refreshBalancesAndNotifications()
            if (binding.transactionsRecyclerView.itemAnimator == null) {
                binding.transactionsRecyclerView.itemAnimator = DefaultItemAnimator()
            }
            loadTransactions(listOf(TransactionRepository.TransactionRepoType.ALL_ACTIVITY, TransactionRepository.TransactionRepoType.DEPOSITS))
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

        val item = menu?.findItem(R.id.action_search_transactions)
        val searchView = item?.actionView as? SearchView
        searchView?.apply {
            queryHint = getString(R.string.OPTIONS_MENU_SEARCHVIEW_PLACEHOLDER)

            (findViewById(R.id.search_close_btn) as? ImageView)?.setOnClickListener {
                // clear current results
                displaySearchResults(listOf())

                if (query.isNullOrBlank()) {
                    onActionViewCollapsed()
                    endSearch()
                } else {
                    setQuery("", false)
                }
            }

            setOnSearchClickListener {
                prepareToSearch()
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    clearFocus() // required to prevent onQueryTextSubmit being called a second time, onKeyUp (first is onKeyDown)
                    query?.let {
                        val trimmedQuery = it.trim()
                        if (trimmedQuery.length < TRANSACTION_SEARCH_MINIMUM_CHARS) {
                            Toast.makeText(context, getString(R.string.TRANSACTIONS_SEARCH_MESSAGE_MINIMUM_CHARS), Toast.LENGTH_LONG).show()
                        } else {
                            // clear current results
                            displaySearchResults(listOf())
                            dashboardViewModel.searchTransactions(it.trim())
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // intentionally left blank
                    return true
                }

            })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        val item = menu?.findItem(R.id.action_search_transactions)
        val searchView = item?.actionView as? SearchView
        searchView?.apply {
            // TODO: set typeface of textview, etc.
            /* from gen1:
            // Next color the search textview and set typeface
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.themeDefaultTextDark));
        textView.setHintTextColor(ContextCompat.getColor(getContext(), R.color.themeNavigationTitle));
             */
            (findViewById(androidx.appcompat.R.id.search_src_text) as? TextView)?.apply {
                typeface = Palette.font_regular
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When overviewView is expanded, toolbar alpha is 0F but options menu items are still clickable.
        // Ignore clicks in this case.
        if (!binding.dashboardExpandableView.showingActions) {
            val id = item.itemId

            if (id == R.id.action_notifications) {
                //listener?.onAlertsNotificationsOptionsItem()
                return true
            }
            // search is handled with SearchView
        }

        return super.onOptionsItemSelected(item)
    }

    private fun prepareToSearch() {
        binding.searchRecyclerView.visibility = View.VISIBLE
        binding.swipeRefreshLayout.visibility = View.INVISIBLE
    }

    private fun endSearch(): Boolean {
        binding.searchRecyclerView.visibility = View.INVISIBLE
        binding.swipeRefreshLayout.visibility = View.VISIBLE
        return true
    }

    private fun updateForCardModel(productCardModel: ProductCardModel) {
        productCardModel.cardStatusText = getString(productCardModel.cardStatus.cardStatusStringRes())

        // update ProductCardView in recyclerview, initially visible
        transactionsAdapter.productCardModel = productCardModel

        // update ProductCardView in expandable overlay, initially invisible
        binding.dashboardExpandableView.cardView.updateWithProductCardModel(productCardModel)

        binding.dashboardExpandableView.overviewLockUnlockCardIcon.setImageDrawable(
                ContextCompat.getDrawable(context!!, if (productCardModel.cardLocked) R.drawable.ic_dashboard_card_unlock else R.drawable.ic_dashboard_card_lock)
        )
        binding.dashboardExpandableView.overviewLockUnlockCardLabel.text = getString(
                if (productCardModel.cardLocked) R.string.OVERVIEW_UNLOCK_MY_CARD else R.string.OVERVIEW_LOCK_MY_CARD
        )
    }

    private fun updateForCardState(cardState: ProductCardViewCardState) {
        when (cardState) {
            ProductCardViewCardState.LOADING -> {
                binding.dashboardExpandableView.overviewShowHideCardDetailsLabel.text = getString(R.string.OVERVIEW_LOADING_CARD_DETAILS)
            }
            ProductCardViewCardState.DETAILS_HIDDEN -> {
                binding.dashboardExpandableView.overviewShowHideCardDetailsIcon.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_dashboard_card_details_show))
                binding.dashboardExpandableView.overviewShowHideCardDetailsLabel.text = getString(R.string.OVERVIEW_SHOW_CARD_DETAILS)
            }
            ProductCardViewCardState.DETAILS_SHOWN -> {
                binding.dashboardExpandableView.overviewShowHideCardDetailsIcon.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_dashboard_card_details_hide))
                binding.dashboardExpandableView.overviewShowHideCardDetailsLabel.text = getString(R.string.OVERVIEW_HIDE_CARD_DETAILS)
            }
            ProductCardViewCardState.ERROR -> {
                binding.dashboardExpandableView.overviewShowHideCardDetailsLabel.text = getString(
                        if (dashboardViewModel.productCardViewModelDelegate.isShowingCardDetails()) R.string.OVERVIEW_HIDE_CARD_DETAILS else R.string.OVERVIEW_SHOW_CARD_DETAILS
                )

                showDialog(
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

    // If the dashboardExpandableView is expanded when the device back button is pressed, collapse it.
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
        dashboardViewModel.initCardView()

        dashboardViewModel.spendingBalanceObservable.observe(this, spendingBalanceObserver)
        dashboardViewModel.spendingBalanceStateObservable.observe(this, spendingBalanceStateObserver)

        dashboardViewModel.savingsBalanceObservable.observe(this, savingsBalanceObserver)
        dashboardViewModel.savingsBalanceStateObservable.observe(this, savingsBalanceStateObserver)

        dashboardViewModel.networkState.observe(this, Observer { transactionsAdapter.setNetworkState(it) })
        dashboardViewModel.transactions.observe(this, transactionsObserver)

        dashboardViewModel.searchTransactions.observe(this, searchObserver)

        // make sure correct tab is showing, after return from TransactionDetailFragment, in particular
        when (dashboardViewModel.transactionsTabPosition) {
            DashboardViewModel.TRANSACTIONS_TAB_POSITION_ALL -> {
                transactionsAdapter.selectedDashboardHeaderTabIndex = DashboardViewModel.TRANSACTIONS_TAB_POSITION_ALL
            }
            DashboardViewModel.TRANSACTIONS_TAB_POSITION_DEPOSITS -> {
                transactionsAdapter.selectedDashboardHeaderTabIndex = DashboardViewModel.TRANSACTIONS_TAB_POSITION_DEPOSITS
            }
        }

        loadTransactions(listOf(TransactionRepository.TransactionRepoType.ALL_ACTIVITY, TransactionRepository.TransactionRepoType.DEPOSITS))

        dashboardViewModel.notificationsCountObservable.observe(this, notificationsObserver)

        dashboardViewModel.animationObservable.observe(this, animationObserver)

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

        dashboardViewModel.initBalancesAndNotifications()
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

    private fun collapse(animate: Boolean) {
        if (animate) {
            binding.dashboardExpandableView.showActions(false)
            // hide view after collapse handled in onCollapseEnd()
        } else {
            // TODO(kurt): there is no function to collapseImmediate()
        }
    }

    private fun displaySearchResults(transactions: List<Transaction>) {
        searchAdapter.updateTransactions(transactions)
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.runGateKeeper()
    }

    override fun onExpandImmediate() {
        showObscuringOverlayImmediate()
        dashboardViewModel.expandImmediate()

        binding.blurView.setOnClickListener {
            //binding.dashboardExpandableView.showActions(false)
            collapse(true)
        }
    }

    override fun onExpandStart() {
        showObscuringOverlay(true)
        dashboardViewModel.expandStart()

        binding.blurView.setOnClickListener {
            //binding.dashboardExpandableView.showActions(false)
            collapse(true)
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

    override fun onShowHideCardDetails() {
        if (dashboardViewModel.productCardViewModelDelegate.isShowingCardDetails()) {
            dashboardViewModel.productCardViewModelDelegate.hideCardDetails()
        } else {
            val authDialogFragment = AuthenticationDialogFragment.newInstance(
                    getString(R.string.OVERVIEW_SHOW_CARD_DETAILS_AUTHENTICATION_MESSAGE)
            ) { dashboardViewModel.productCardViewModelDelegate.showCardDetails() }

            authDialogFragment.show(childFragmentManager, AuthenticationDialogFragment.TAG)
        }
    }

    override fun onMoveMoney() {
        binding.root.findNavController().navigate(R.id.action_dashboard_fragment_to_moveMoneyFragment)
    }

    override fun onLockUnlockCard() {
        val lock: Boolean? = when(EngageService.getInstance().storageManager.currentCard.status){
            DebitCardStatus.ACTIVE -> true
            DebitCardStatus.LOCKED_USER -> false
            else -> null
        }
        lock?.let{ dashboardViewModel.updateCardLockStatus(it)
        } ?: run {
            Toast.makeText(
                    context,
                    getString(R.string.FEATURE_NOT_AVAILABLE_HEADER),
                    Toast.LENGTH_SHORT).show() }
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
            Toast.makeText(activity, "Set aside balance selected", Toast.LENGTH_SHORT).show()
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

    private fun loadTransactions(repoTypes: List<TransactionRepository.TransactionRepoType>) {
        dashboardViewModel.clearTransactions(repoTypes) { dashboardViewModel.initTransactions(repoTypes) }
    }

    // TransactionsPagedAdapter.OnTransactionsAdapterListener
    override fun onTransactionSelected(transaction: Transaction) {
        // TODO(kurt): Pass transactionInfo to TransactionDetailFragment through navigation bundle (see onMoveMoney(), above)
        Toast.makeText(activity, "Transaction selected: " + transaction.store, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TRANSACTION_SEARCH_MINIMUM_CHARS = 2
    }
}