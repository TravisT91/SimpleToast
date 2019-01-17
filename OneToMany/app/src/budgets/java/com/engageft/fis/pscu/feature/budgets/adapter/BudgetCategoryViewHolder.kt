package com.engageft.fis.pscu.feature.budgets.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.TrackingPanel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.budgets.model.BudgetModel

/**
 * BudgetCategoryViewHolder
 * <p>
 * ViewHolder subclass to show a budget category row in budgets list.
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val trackingPanel: TrackingPanel = itemView.findViewById(R.id.row_budget_tracking_panel)

    fun bindTo(budgetModel: BudgetModel) {

    }
}