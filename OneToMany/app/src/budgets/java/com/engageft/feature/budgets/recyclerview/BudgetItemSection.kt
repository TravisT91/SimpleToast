package com.engageft.feature.budgets.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

/**
 * BudgetItemSection
 * <p>
 * StatelessSection for displaying a list of BudgetItems
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetItemSection(private val budgetItems: List<BudgetItem>, val isTotalSection: Boolean, val listener: BudgetItemSectionListener)
    : StatelessSection(
        SectionParameters.builder().itemResourceId(
        if (isTotalSection) R.layout.row_budget_tracking_panel_parent else R.layout.row_budget_tracking_panel_child).build()) {

    override fun getContentItemsTotal(): Int {
        return budgetItems.size
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as? BudgetItemViewHolder)?.apply {
            bindTo(budgetItems[position])
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return BudgetItemViewHolder(view, listener)
    }

    interface BudgetItemSectionListener {
        fun onBudgetCategorySelected(categoryName: String)
    }
}