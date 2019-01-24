package com.engageft.feature.budgets.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.TrackingPanel
import com.engageft.fis.pscu.R

/**
 * BudgetItemViewHolder
 * <p>
 * ViewHolder subclass to show a budget category row in budgets list.
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetItemViewHolder(itemView: View, listener: BudgetItemSection.BudgetItemSectionListener) : RecyclerView.ViewHolder(itemView) {

    private val trackingPanel: TrackingPanel = itemView.findViewById(R.id.row_budget_tracking_panel)
    private var budgetItem: BudgetItem? = null

    init {
        trackingPanel.setOnClickListener {
            budgetItem?.apply {
                listener.onBudgetCategorySelected(categoryName)
            }
        }
    }

    fun bindTo(budgetItem: BudgetItem) {
        this.budgetItem = budgetItem

        budgetItem.apply {
            trackingPanel.setTitleText(title)
            trackingPanel.setRightSubTitle(spent)
            trackingPanel.setRightSubtitleColor(spentColor)
            trackingPanel.setProgress(progress)
            trackingPanel.setProgressColor(progressColor)
            trackingPanel.setIndicatorPosition(fractionTimePeriodPassed)
        }
    }
}