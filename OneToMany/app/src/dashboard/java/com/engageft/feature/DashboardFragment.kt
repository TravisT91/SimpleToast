package com.engageft.feature

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentDashboardBinding
import com.google.android.material.tabs.TabLayout
import com.ob.ws.dom.utility.TransactionInfo
import eightbitlab.com.blurview.BlurView
import java.math.BigDecimal

/**
 * DashboardFragment
 * <p>
 * UI Fragment for the Dashboard (Overview).
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DashboardFragment : LotusFullScreenFragment() {
//    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding

    private lateinit var overviewViewModel: OverviewViewModel
    private lateinit var cardViewViewModel: CardViewViewModel

    private val cardInfoModelObserver = Observer<ProductCardModel> { updateForCardInfo(it!!) }
    private val cardStateObserver = Observer<CardViewViewModel.CardState> { updateForCardState(it!!) }
    private val cardErrorObserver = Observer<Any> { error ->
        if (error != null && error is String) showErrorDialog(error)
    }

    private val spendingBalanceObserver = Observer<BigDecimal> { updateSpendingBalance(it) }
    private val spendingBalanceStateObserver = Observer<OverviewBalanceState> { updateSpendingBalanceState(it!!) }

    private val savingsBalanceObserver = Observer<BigDecimal> { updateSavingsBalance(it) }
    private val savingsBalanceStateObserver = Observer<OverviewBalanceState> { updateSavingsBalanceState(it!!) }

    private val allTransactionsObserver = Observer<List<TransactionInfo>> { updateTransactions(it)}
    private val retrievingTransactionsFinishedObserver = Observer<Boolean> { retrievingTransactionsFinished(it)}

    private val notificationsObserver = Observer<Int> { activity?.invalidateOptionsMenu() }

    private var listener: OverviewFragmentListener? = null

    private var toolbarShadowAnimationScrollRange: Int = 0
    private var toolbarShadowAnimationScrollRangeFloat: Float = 0F

    private lateinit var transactionsAdapter: TransactionsAdapter

    private var scrollDisabled = false

    // views, which can't be accessed using Kotlin Android plugin because the imports created for that are flavor-specific
    private lateinit var rootLayout: ViewGroup
    lateinit var overviewView: OverviewView
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var overviewNestedScrollView: NestedScrollView
    private lateinit var toolbarShadowView: View
    private lateinit var spendingBalanceLayout: ViewGroup
    private lateinit var spendingBalanceAmount: TextView
    private lateinit var savingsBalanceLayout: ViewGroup
    private lateinit var savingsBalanceAmount: TextView
    private lateinit var transactionsTabLayout: TabLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var blurView: BlurView
    // for displaying a message view, like CARE-399 waiting for card activation message
    lateinit var messageContainer: ViewGroup

    override fun createViewModel(): BaseViewModel? {
//        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_dashboard, container, false)
        return binding.root
    }

    private fun updateForCardInfo(cardInfo: ProductCardModel) {
        overviewView.cardView.updateWithCardInfoModel(cardInfo)

        overviewView.overviewLockUnlockCardLabel.text = getString(
                if (cardInfo.cardLocked) R.string.OVERVIEW_UNLOCK_MY_CARD else R.string.OVERVIEW_LOCK_MY_CARD
        )
    }

    private fun updateForCardState(cardState: CardViewViewModel.CardState) {
        when (cardState) {
            CardViewViewModel.CardState.LOADING -> {
                showProgressOverlay()
                overviewView.overviewShowHideCardDetailsLabel.text = getString(R.string.OVERVIEW_LOADING_CARD_DETAILS)
            }
            CardViewViewModel.CardState.DETAILS_HIDDEN -> {
                hideProgressOverlay()
                overviewView.overviewShowHideCardDetailsLabel.text = getString(R.string.OVERVIEW_SHOW_CARD_DETAILS)
            }
            CardViewViewModel.CardState.DETAILS_SHOWN -> {
                hideProgressOverlay()
                overviewView.overviewShowHideCardDetailsLabel.text = getString(R.string.OVERVIEW_HIDE_CARD_DETAILS)
            }
            CardViewViewModel.CardState.ERROR -> {
                hideProgressOverlay()
                overviewView.overviewShowHideCardDetailsLabel.text = getString(
                        if (cardViewViewModel.isShowingCardDetails()) R.string.OVERVIEW_HIDE_CARD_DETAILS else R.string.OVERVIEW_SHOW_CARD_DETAILS
                )
                showErrorDialog(getString(R.string.OVERVIEW_CARD_ERROR_DIALOG_MESSAGE))

            }
        }

        // don't show screen image in task switcher if showing card details
        preventScreenCapture(cardViewViewModel.isShowingCardDetails())
    }

    fun updateSpendingBalance(spendingBalance: BigDecimal?) {
        spendingBalance?.let {
            spendingBalanceAmount.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(spendingBalance.toFloat(), 0.5f, true)
        } ?: run {
            // TODO(kurt) what do show when no value?
            spendingBalanceAmount.text = "..."
        }
    }

    fun updateSpendingBalanceState(spendingBalanceState: OverviewBalanceState) {
        when (spendingBalanceState) {
            OverviewBalanceState.LOADING -> spendingBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_LOADING)
            OverviewBalanceState.ERROR -> spendingBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_ERROR)
            // don't change for other states
        }
    }

    fun updateSavingsBalance(savingsBalance: BigDecimal?) {
        savingsBalance?.let {
            savingsBalanceAmount.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(savingsBalance.toFloat(), 0.5f, true)
            savingsBalanceLayout.visibility = View.VISIBLE
        } ?: run {
            savingsBalanceLayout.visibility = View.GONE
        }
    }

    fun updateSavingsBalanceState(savingsBalanceState: OverviewBalanceState) {
        when (savingsBalanceState) {
            OverviewBalanceState.LOADING -> {
                savingsBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_LOADING)
                savingsBalanceLayout.visibility = View.VISIBLE
            }
            OverviewBalanceState.HIDDEN -> savingsBalanceLayout.visibility = View.INVISIBLE
            OverviewBalanceState.AVAILABLE -> savingsBalanceLayout.visibility = View.VISIBLE
            OverviewBalanceState.ERROR -> savingsBalanceAmount.text = getString(R.string.OVERVIEW_BALANCE_ERROR)
        }
    }

    fun retrievingTransactionsFinished(transactionsComplete: Boolean?) {
        transactionsComplete?.let {
            if (it) {
                transactionsAdapter.notifyRetrievingTransactionsFinished()
            }
        }
    }

    fun updateTransactions(transactionsList: List<TransactionInfo>?) {
        transactionsList?.let {
            transactionsAdapter.updateTransactionsList(it)
        }
        // swipeToRefresh showProgressOverlay in viewModel, remove here
        hideProgressOverlay()
    }

    fun showMessageContainerWithView(messageView: View) {
        messageContainer.addView(messageView)
        messageContainer.visibility = View.VISIBLE
        overviewView.showExpandCollapseButton(false)
    }

    fun hideMessageContainer() {
        messageContainer.visibility = View.INVISIBLE
        messageContainer.removeAllViews()
        overviewView.showExpandCollapseButton(true)
    }

    private fun preventScreenCapture(prevent: Boolean) {
        if (prevent) {
            activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        else {
            activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // for use when restoring OverviewView in onSaveInstanceState
    private fun showObscuringOverlayImmediate() {
        scrollDisabled = true
        transactionsAdapter.transactionSelectionEnabled = false
        blurView.visibility = View.VISIBLE
        blurView.alpha = 1F
    }

    private fun showObscuringOverlay(show: Boolean) {
        // animate alpha of obscuring overlay over transactions -- fade in as OverviewView expands
        val alphaEnd: Float

        if (show) {
            alphaEnd = 1F

            // if the main scrollView is not at the top, scroll to top now. Otherwise expanded OverviewView
            // may be partially offscreen.
            if (overviewNestedScrollView.scrollY != 0) {
                overviewNestedScrollView.scrollTo(0, 0)
            }
            // Don't let user scroll or click transactions when OverviewView is expanded
            scrollDisabled = true
            transactionsAdapter.transactionSelectionEnabled = false
        } else {
            alphaEnd = 0F
        }

        val alphaAnimator = ObjectAnimator.ofFloat(blurView,"alpha", alphaEnd)
        alphaAnimator.duration = overviewView.animationDurationMs
        blurView.visibility = View.VISIBLE
        alphaAnimator.start()
    }
}