package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentDashboardBinding
import com.google.android.material.tabs.TabLayout
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

    private val cardInfoModelObserver = Observer<CardInfoModel> { updateForCardInfo(it!!) }
    private val cardStateObserver = Observer<CardState> { updateForCardState(it!!) }
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
}