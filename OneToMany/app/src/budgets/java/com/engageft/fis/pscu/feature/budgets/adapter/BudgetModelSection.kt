package com.engageft.fis.pscu.feature.budgets.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.budgets.extension.isCategoryNameOtherSpending
import com.engageft.fis.pscu.feature.budgets.model.BudgetModel
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import java.util.*

/**
 * BudgetModelSection
 * <p>
 * StatelessSection for displaying a list of BudgetModels
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetModelSection(val context: Context, val budgetModels: List<BudgetModel>, val isTotalSection: Boolean)
    : StatelessSection(SectionParameters.builder().itemResourceId(
        if (isTotalSection) R.layout.row_budget_tracking_panel_parent else R.layout.row_budget_tracking_panel_child
).build()) {
    override fun getContentItemsTotal(): Int {
        return budgetModels.size
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is BudgetModelViewHolder) {
            val budgetModel = budgetModels[position]
            if (isTotalSection) {
                budgetModel.title = context.getString(R.string.budget_total_spent_title)
            } else {
                var title = EngageService.getInstance().storageManager.getBudgetCategoryDescription(budgetModel.categoryName, Locale.getDefault().language)
                if (title.isEmpty() && budgetModel.categoryName != null && isCategoryNameOtherSpending(budgetModel.categoryName)) {
                    title = context.getString(R.string.budget_category_name_other_spending)
                }
                budgetModel.title = title
            }
            holder.bindTo(budgetModels[position], context)
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return BudgetModelViewHolder(view)
    }
}