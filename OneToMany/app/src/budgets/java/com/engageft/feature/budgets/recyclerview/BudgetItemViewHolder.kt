package com.engageft.feature.budgets.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.databinding.RowBudgetTrackingPanelBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * BudgetItemViewHolder
 * <p>
 * ViewHolder subclass to show a budget category row in budgets list.
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetItemViewHolder(val binding: RowBudgetTrackingPanelBinding, listener: BudgetsListAdapter.BudgetListAdapterListener) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.rowBudgetTrackingPanel.setOnClickListener {
            binding.budgetItem?.apply {
                listener.onBudgetCategorySelected(categoryName)
            }
        }

        binding.palette = Palette
    }

    fun bind(budgetItem: BudgetItem) {
        binding.budgetItem = budgetItem
        binding.executePendingBindings()
    }
}