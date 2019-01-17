package com.engageft.fis.pscu.feature.budgets

import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.feature.BaseEngagePageFragment

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

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(BudgetsListViewModel::class.java)
        return viewModel
    }
}