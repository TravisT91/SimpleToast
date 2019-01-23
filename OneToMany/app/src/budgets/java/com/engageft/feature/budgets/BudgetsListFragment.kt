package com.engageft.feature.budgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentBudgetsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.feature.budgets.adapter.BudgetModelSection
import com.engageft.feature.budgets.model.BudgetModel
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
class BudgetsListFragment : BaseEngagePageFragment() {

    private lateinit var viewModel: BudgetsListViewModel
    private lateinit var binding: FragmentBudgetsListBinding
    private var budgetsListAdapter = SectionedRecyclerViewAdapter()

    private val budgetsObserver = Observer<Pair<BudgetModel, List<BudgetModel>>> { updateBudgetsList(it)}

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(BudgetsListViewModel::class.java)
        return viewModel
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        viewModel.budgetsObservable.observe(viewLifecycleOwner, budgetsObserver)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBudgetsListBinding.inflate(inflater,container,false)
        binding.apply {
            budgetsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = budgetsListAdapter
            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.budgetsObservable.observe(viewLifecycleOwner, budgetsObserver)
        viewModel.init()
    }

    private fun updateBudgetsList(totalAndCategoryBudgetModels: Pair<BudgetModel, List<BudgetModel>>) {
        val totalBudgetModel = totalAndCategoryBudgetModels.first
        val categoryBudgetModels = totalAndCategoryBudgetModels.second

        budgetsListAdapter.removeAllSections()
        budgetsListAdapter.addSection(
                BudgetModelSection(context!!, listOf(totalBudgetModel), isTotalSection = true)
        )

        budgetsListAdapter.addSection(LabelSection.newInstanceGroupTitle(getString(R.string.budget_categories_header)))

        budgetsListAdapter.addSection(
                BudgetModelSection(context!!, categoryBudgetModels, isTotalSection = false)
        )
        budgetsListAdapter.notifyDataSetChanged()
    }
}