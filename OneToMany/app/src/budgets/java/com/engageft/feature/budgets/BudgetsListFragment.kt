package com.engageft.feature.budgets

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.feature.budgets.recyclerview.BudgetItem
import com.engageft.feature.budgets.recyclerview.BudgetItemSection
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentBudgetsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.recyclerview.section.LabelSection
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 * BudgetsListFragment
 * <p>
 * Fragment to display a list of budgets
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetsListFragment : BaseEngagePageFragment(), BudgetItemSection.BudgetItemSectionListener {
    private lateinit var viewModel: BudgetsListViewModel
    private lateinit var binding: FragmentBudgetsListBinding
    private var budgetsListAdapter = SectionedRecyclerViewAdapter()

    // Without a slight delay, updating the recyclerview causes the close drawer animation to stutter.
    // The delay gives the drawer time to close before updateBudgetLists() is called on the main thread.
    // When I wrote this initially with the ViewModel doing all calculations/processing on a background thread
    // with injected strings, colors, etc. this delay was unnecessary. There was no stuttering.
    private val budgetsObserver = Observer<Pair<BudgetItem, List<BudgetItem>>> { Handler().postDelayed({ updateBudgetsList(it) }, RECYCLERVIEW_UPDATE_DELAY_MS)}

    private lateinit var totalSpentTitle: String
    private lateinit var otherSpendingTitle: String
    private lateinit var spentNormalFormat: String
    private lateinit var spentOverFormat: String
    @ColorInt
    private var spentColorNormal: Int = 0
    @ColorInt
    private var spentColorOverBudget: Int = 0
    @ColorInt
    private var progressColorNormal: Int = 0
    @ColorInt
    private var progressColorHighSpendingTrend: Int = 0
    @ColorInt
    private var progressColorOverBudget: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        totalSpentTitle = getString(R.string.budget_category_title_total_spent)
        otherSpendingTitle = getString(R.string.budget_category_title_other_spending)
        spentNormalFormat = getString(R.string.budget_spent_of_amount_format)
        spentOverFormat = getString(R.string.budget_spent_over_format)
        spentColorNormal = getColor(context!!, R.color.budget_spent_text_normal)
        spentColorOverBudget = getColor(context!!, R.color.budget_spent_text_over)
        progressColorNormal = getColor(context!!, R.color.budget_bar_normal)
        progressColorHighSpendingTrend = getColor(context!!, R.color.budget_bar_warning)
        progressColorOverBudget = getColor(context!!, R.color.budget_bar_over)
    }

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(BudgetsListViewModel::class.java)
        return viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBudgetsListBinding.inflate(inflater,container,false)
        binding.budgetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetsListAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.budgetsObservable.observe(viewLifecycleOwner, budgetsObserver)
        viewModel.refresh()
    }

    private fun updateBudgetsList(totalAndCategoryBudgetItems: Pair<BudgetItem, List<BudgetItem>>) {
        val totalBudgetItem = totalAndCategoryBudgetItems.first
        val categoryBudgetItems = totalAndCategoryBudgetItems.second

        budgetsListAdapter.removeAllSections()
        budgetsListAdapter.addSection(
                BudgetItemSection(budgetItems = listOf(totalBudgetItem),
                        isTotalSection = true,
                        totalSpentTitle = totalSpentTitle,
                        otherSpendingTitle = otherSpendingTitle,
                        spentNormalFormat = spentNormalFormat,
                        spentOverFormat = spentOverFormat,
                        spentColorNormal = spentColorNormal,
                        spentColorOverBudget = spentColorOverBudget,
                        progressColorNormal = progressColorNormal,
                        progressColorHighSpendingTrend = progressColorHighSpendingTrend,
                        progressColorOverBudget = progressColorOverBudget,
                        listener = this)
        )

        budgetsListAdapter.addSection(LabelSection.newInstanceGroupTitle(getString(R.string.budget_categories_header)))

        budgetsListAdapter.addSection(
                BudgetItemSection(budgetItems = categoryBudgetItems,
                        isTotalSection = false,
                        totalSpentTitle = totalSpentTitle,
                        otherSpendingTitle = otherSpendingTitle,
                        spentNormalFormat = spentNormalFormat,
                        spentOverFormat = spentOverFormat,
                        spentColorNormal = spentColorNormal,
                        spentColorOverBudget = spentColorOverBudget,
                        progressColorNormal = progressColorNormal,
                        progressColorHighSpendingTrend = progressColorHighSpendingTrend,
                        progressColorOverBudget = progressColorOverBudget,
                        listener = this)
        )
        budgetsListAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val RECYCLERVIEW_UPDATE_DELAY_MS = 250L
    }

    // BudgetItemSection.BudgetItemSectionListener
    override fun onBudgetCategorySelected(categoryName: String) {
        Toast.makeText(context, "Selected category: $categoryName", Toast.LENGTH_SHORT).show()
    }
}