package com.engageft.fis.pscu.feature.budgets

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.budgets.model.BudgetModel

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

    private val budgetsObserver = Observer<Pair<BudgetModel, List<BudgetModel>>> { updateBudgetsList(it)}

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(BudgetsListViewModel::class.java)
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.budgetsObservable.observe(viewLifecycleOwner, budgetsObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init()
    }

    private fun updateBudgetsList(totalAndCategoryBudgetModels: Pair<BudgetModel, List<BudgetModel>>) {
        val totalBudgetModel = totalAndCategoryBudgetModels.first
        val categoryBudgetModels = totalAndCategoryBudgetModels.second


    }
}