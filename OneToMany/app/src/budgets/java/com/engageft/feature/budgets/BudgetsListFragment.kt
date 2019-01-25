package com.engageft.feature.budgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentBudgetsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.feature.budgets.recyclerview.BudgetItemSection
import com.engageft.feature.budgets.recyclerview.BudgetItem
import com.engageft.fis.pscu.R
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

    private val budgetsObserver = Observer<Pair<BudgetItem, List<BudgetItem>>> { updateBudgetsList(it)}

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
        viewModel.initViewModel(
                totalSpentTitle = getString(R.string.budget_category_title_total_spent),
                otherSpendingTitle = getString(R.string.budget_category_title_other_spending),
                spentNormalFormat = getString(R.string.budget_spent_of_amount_format),
                spentOverFormat = getString(R.string.budget_spent_over_format),
                spentColorNormal = getColor(context!!, R.color.budget_spent_text_normal),
                spentColorOverBudget = getColor(context!!, R.color.budget_spent_text_over),
                progressColorNormal = getColor(context!!, R.color.budget_bar_normal),
                progressColorHighSpendingTrend = getColor(context!!, R.color.budget_bar_warning),
                progressColorOverBudget = getColor(context!!, R.color.budget_bar_over)
        )
    }

    private fun updateBudgetsList(totalAndCategoryBudgetItems: Pair<BudgetItem, List<BudgetItem>>) {
        val totalBudgetItem = totalAndCategoryBudgetItems.first
        val categoryBudgetItems = totalAndCategoryBudgetItems.second

        budgetsListAdapter.removeAllSections()
        budgetsListAdapter.addSection(
                BudgetItemSection(budgetItems = listOf(totalBudgetItem), isTotalSection = true, listener = this)
        )

        budgetsListAdapter.addSection(LabelSection.newInstanceGroupTitle(getString(R.string.budget_categories_header)))

        budgetsListAdapter.addSection(
                BudgetItemSection(budgetItems = categoryBudgetItems, isTotalSection = false, listener = this)
        )
        budgetsListAdapter.notifyDataSetChanged()
    }

    // BudgetItemSection.BudgetItemSectionListener
    override fun onBudgetCategorySelected(categoryName: String) {
        Toast.makeText(context, "Selected category: ${categoryName}", Toast.LENGTH_SHORT).show()
    }
}