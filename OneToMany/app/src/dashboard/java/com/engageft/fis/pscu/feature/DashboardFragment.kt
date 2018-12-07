package com.engageft.fis.pscu.feature

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentDashboardBinding
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.authentication.AuthenticationDialogFragment
import com.engageft.fis.pscu.feature.utils.cardStatusStringRes
import com.google.android.material.tabs.TabLayout
import com.ob.domain.lookup.DebitCardStatus
import com.ob.ws.dom.utility.TransactionInfo
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
        TransactionsAdapter.OnTransactionsAdapterListener {
    private lateinit var binding: FragmentDashboardBinding

    private lateinit var dashboardViewModel: DashboardViewModel

    private val cardModelObserver = Observer<ProductCardModel> { updateForCardModel(it!!) }
    private val cardStateObserver = Observer<ProductCardViewCardState> { updateForCardState(it!!) }

    private val spendingBalanceObserver = Observer<BigDecimal> { updateSpendingBalance(it) }
    private val spendingBalanceStateObserver = Observer<DashboardBalanceState> { updateSpendingBalanceState(it!!) }

    private val savingsBalanceObserver = Observer<BigDecimal> { updateSavingsBalance(it) }
    private val savingsBalanceStateObserver = Observer<DashboardBalanceState> { updateSavingsBalanceState(it!!) }

    private val allTransactionsObserver = Observer<List<TransactionInfo>> { updateTransactions(it)}
    private val retrievingTransactionsFinishedObserver = Observer<Boolean> { retrievingTransactionsFinished(it)}

    private val notificationsObserver = Observer<Int> { activity?.invalidateOptionsMenu() }

    private val animationObserver = Observer<DashboardAnimationEvent> { updateAnimation(it!!) }

    private var toolbarShadowAnimationScrollRange: Int = 0
    private var toolbarShadowAnimationScrollRangeFloat: Float = 0F

    private lateinit var transactionsAdapter: TransactionsAdapter

    private var scrollDisabled = false

    override fun createViewModel(): BaseViewModel? {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        return dashboardViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        transactionsAdapter = TransactionsAdapter(context!!, this)
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionsAdapter
        }
        // this enables smooth scrolling of the nestedscrollview
        ViewCompat.setNestedScrollingEnabled(binding.transactionsRecyclerView, false)

        // allows disabling scrollview
        binding.dashboardNestedScrollView.setOnTouchListener { _, _ ->
            scrollDisabled
        }

        binding.dashboardNestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
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

        val spendingClickListener = View.OnClickListener {
            if (!binding.dashboardExpandableView.showingActions) {
                Toast.makeText(activity, "Spending balance selected", Toast.LENGTH_SHORT).show()
            }
        }
        binding.spendingBalanceAmount.setOnClickListener(spendingClickListener)
        binding.spendingBalanceLabel.setOnClickListener(spendingClickListener)

        val savingsClickListener = View.OnClickListener {
            if (!binding.dashboardExpandableView.showingActions) {
                Toast.makeText(activity, "Set aside balance selected", Toast.LENGTH_SHORT).show()
            }
        }
        binding.savingsBalanceAmount.setOnClickListener(savingsClickListener)
        binding.savingsBalanceLabel.setOnClickListener(savingsClickListener)

        binding.transactionsTabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // intentionally left blank
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // intentionally left blank
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (binding.transactionsTabLayout.selectedTabPosition) {
                    DashboardViewModel.TRANSACTIONS_TAB_POSITION_ALL -> {
                        transactionsAdapter.showDepositsOnly = false
                    }
                    DashboardViewModel.TRANSACTIONS_TAB_POSITION_DEPOSITS -> {
                        transactionsAdapter.showDepositsOnly = true
                    }
                }
                // update view model with position, so that it can be set correctly when fragment is restored
                dashboardViewModel.transactionsTabPosition = binding.transactionsTabLayout.selectedTabPosition
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            dashboardViewModel.refreshBalancesAndNotifications()
            refreshTransactions()
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

    private fun updateForCardModel(productCardModel: ProductCardModel) {
        productCardModel.cardStatusText = getString(productCardModel.cardStatus.cardStatusStringRes())
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

    fun updateSpendingBalance(spendingBalance: BigDecimal?) {
        spendingBalance?.let {
            binding.spendingBalanceAmount.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(spendingBalance.toFloat(), 0.5f, true)
        } ?: run {
            // TODO(kurt) what do show when no value?
            binding.spendingBalanceAmount.text = "..."
        }
    }

    fun updateSpendingBalanceState(spendingBalanceState: DashboardBalanceState) {
        when (spendingBalanceState) {
            DashboardBalanceState.LOADING -> binding.spendingBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_LOADING)
            DashboardBalanceState.ERROR -> binding.spendingBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_ERROR)
            // don't change for other states
            else -> {}
        }
    }

    fun updateSavingsBalance(savingsBalance: BigDecimal?) {
        savingsBalance?.let {
            binding.savingsBalanceAmount.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(savingsBalance.toFloat(), 0.5f, true)
            binding.savingsBalanceAmount.visibility = View.VISIBLE
            binding.savingsBalanceLabel.visibility = View.VISIBLE
        } ?: run {
            binding.savingsBalanceAmount.visibility = View.GONE
            binding.savingsBalanceLabel.visibility = View.GONE
        }
    }

    fun updateSavingsBalanceState(savingsBalanceState: DashboardBalanceState) {
        when (savingsBalanceState) {
            DashboardBalanceState.LOADING -> {
                binding.savingsBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_LOADING)
                binding.savingsBalanceAmount.visibility = View.VISIBLE
                binding.savingsBalanceLabel.visibility = View.VISIBLE
            }
            DashboardBalanceState.HIDDEN -> {
                binding.savingsBalanceAmount.visibility = View.GONE
                binding.savingsBalanceLabel.visibility = View.GONE
            }
            DashboardBalanceState.AVAILABLE -> {
                binding.savingsBalanceAmount.visibility = View.VISIBLE
                binding.savingsBalanceLabel.visibility = View.VISIBLE
            }
            DashboardBalanceState.ERROR -> binding.savingsBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_ERROR)
        }
    }

    private fun updateAnimation(animationEvent: DashboardAnimationEvent) {
        when (animationEvent) {
            DashboardAnimationEvent.EXPAND_IMMEDIATE -> {
                //navView.visibility = View.GONE // for bottom nav
                (activity as LotusActivity).showToolbar(show = false, animate = false)
            }
            DashboardAnimationEvent.EXPAND_START -> {
                //navView.visibility = View.GONE // for bottom nav
                (activity as LotusActivity).showToolbar(show = false, animate = true, animationDurationMS = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong())
            }
            DashboardAnimationEvent.EXPAND_END -> {
                // intentionally left blank
            }
            DashboardAnimationEvent.COLLAPSE_START -> {
                //navView.visibility = View.VISIBLE // for bottom nav
                (activity as LotusActivity).showToolbar(show = true, animate = true, animationDurationMS = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong())
            }
            DashboardAnimationEvent.COLLAPSE_END -> {
                // intentionally left blank
            }
        }
    }

    private fun retrievingTransactionsFinished(transactionsComplete: Boolean?) {
        transactionsComplete?.let {
            if (it) {
                transactionsAdapter.notifyRetrievingTransactionsFinished()
            }
        }
    }

    private fun updateTransactions(transactionsList: List<TransactionInfo>?) {
        transactionsList?.let {
            transactionsAdapter.updateTransactionsList(it)
        }
        // swipeToRefresh showProgressOverlay in viewModel, remove here
        progressOverlayDelegate.dismissProgressOverlay()
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

    private fun refreshTransactions() {
        transactionsAdapter.notifyRetrievingTransactionsStarted()
        dashboardViewModel.refreshTransactions()
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

            // if the main scrollView is not at the top, scroll to top now. Otherwise expanded DashboardExpandableView
            // may be partially offscreen.
            if (binding.dashboardNestedScrollView.scrollY != 0) {
                binding.dashboardNestedScrollView.scrollTo(0, 0)
            }
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

        dashboardViewModel.allTransactionsObservable.observe(this, allTransactionsObserver)
        dashboardViewModel.retrievingTransactionsFinishedObservable.observe(this, retrievingTransactionsFinishedObserver)

        dashboardViewModel.notificationsCountObservable.observe(this, notificationsObserver)

        dashboardViewModel.animationObservable.observe(this, animationObserver)

        // make sure correct tab is showing, after return from TransactionDetailFragment, in particular
        binding.transactionsTabLayout.getTabAt(dashboardViewModel.transactionsTabPosition)?.select()

        dashboardViewModel.initBalancesAndNotifications()
        dashboardViewModel.initTransactions()
    }

    override fun onExpandImmediate() {
        showObscuringOverlayImmediate()
        dashboardViewModel.expandImmediate()

        binding.blurView.setOnClickListener {
            binding.dashboardExpandableView.showActions(false)
        }
    }

    override fun onExpandStart() {
        showObscuringOverlay(true)
        dashboardViewModel.expandStart()

        binding.blurView.setOnClickListener {
            binding.dashboardExpandableView.showActions(false)
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

    // TransactionsAdapter.OnTransactionsAdapterListener
    override fun onTransactionInfoSelected(transactionInfo: TransactionInfo) {
        // TODO(kurt): Pass transactionInfo to TransactionDetailFragment through navigation bundle (see onMoveMoney(), above)
        Toast.makeText(activity, "Transaction selected: " + transactionInfo.store, Toast.LENGTH_SHORT).show()
    }
}