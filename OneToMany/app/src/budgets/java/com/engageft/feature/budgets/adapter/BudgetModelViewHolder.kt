package com.engageft.feature.budgets.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.TrackingPanel
import com.engageft.fis.pscu.R
import com.engageft.feature.budgets.model.BudgetModel
import utilGen1.StringUtils
import java.math.BigDecimal

/**
 * BudgetModelViewHolder
 * <p>
 * ViewHolder subclass to show a budget category row in budgets list.
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val trackingPanel: TrackingPanel = itemView.findViewById(R.id.row_budget_tracking_panel)

    fun bindTo(budgetModel: BudgetModel, context: Context) {
        budgetModel.title?.let {
            trackingPanel.setTitleText(it)
        }
        trackingPanel.setRightSubTitle(
                String.format(
                        context.getString(R.string.budget_spent_of_amount_format),
                        StringUtils.formatCurrencyString(budgetModel.spentAmount.toFloat()),
                        StringUtils.formatCurrencyString(budgetModel.budgetAmount.toFloat())
                )
        )
        if (budgetModel.budgetAmount.compareTo(BigDecimal.ZERO) != 0) {
            trackingPanel.setProgress(budgetModel.spentAmount.divide(budgetModel.budgetAmount).toFloat())
        } else {
            trackingPanel.setProgress(0.0F)
        }
        trackingPanel.setIndicatorPosition(budgetModel.fractionTimePeriodPassed)
    }
}