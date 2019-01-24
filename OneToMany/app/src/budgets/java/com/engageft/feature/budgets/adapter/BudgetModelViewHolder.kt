package com.engageft.feature.budgets.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.TrackingPanel
import com.engageft.feature.budgets.model.BudgetModel
import com.engageft.fis.pscu.R

/**
 * BudgetModelViewHolder
 * <p>
 * ViewHolder subclass to show a budget category row in budgets list.
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetModelViewHolder(itemView: View, listener: BudgetModelSection.BudgetModelSectionListener) : RecyclerView.ViewHolder(itemView) {

    private val trackingPanel: TrackingPanel = itemView.findViewById(R.id.row_budget_tracking_panel)
    private var budgetModel: BudgetModel? = null

    init {
        trackingPanel.setOnClickListener {
            budgetModel?.apply {
                listener.onBudgetCategorySelected(categoryName)
            }
        }
    }

    fun bindTo(budgetModel: BudgetModel) {
        this.budgetModel = budgetModel

        budgetModel.apply {
            trackingPanel.setTitleText(title)
            trackingPanel.setRightSubTitle(spent)
            trackingPanel.setRightSubtitleColor(spentColor)
            trackingPanel.setProgress(progress)
            trackingPanel.setProgressColor(progressColor)
            trackingPanel.setIndicatorPosition(fractionTimePeriodPassed)
        }
    }
}