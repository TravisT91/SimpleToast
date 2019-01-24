package com.engageft.feature.budgets.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import com.engageft.feature.budgets.model.BudgetModel
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

/**
 * BudgetModelSection
 * <p>
 * StatelessSection for displaying a list of BudgetModels
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetModelSection(private val budgetModels: List<BudgetModel>, val isTotalSection: Boolean, val listener: BudgetModelSectionListener)
    : StatelessSection(
        SectionParameters.builder().itemResourceId(
        if (isTotalSection) R.layout.row_budget_tracking_panel_parent else R.layout.row_budget_tracking_panel_child).build()) {

    override fun getContentItemsTotal(): Int {
        return budgetModels.size
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as? BudgetModelViewHolder)?.apply {
            bindTo(budgetModels[position])
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return BudgetModelViewHolder(view, listener)
    }

    interface BudgetModelSectionListener {
        fun onBudgetCategorySelected(categoryName: String)
    }
}