package com.engageft.feature.budgets.recyclerview

import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.EngageService
import com.engageft.feature.budgets.BudgetConstants
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import utilGen1.StringUtils
import java.math.BigDecimal
import java.util.*

/**
 * BudgetItemSection
 * <p>
 * StatelessSection for displaying a list of BudgetItems
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetItemSection(private val budgetItems: List<BudgetItem>,
                        isTotalSection: Boolean,
                        private val totalSpentTitle: String,
                        private val otherSpendingTitle: String,
                        private val spentNormalFormat: String,
                        private val spentOverFormat: String,
                        @ColorInt private val spentColorNormal: Int,
                        @ColorInt private val spentColorOverBudget: Int,
                        @ColorInt private val progressColorNormal: Int,
                        @ColorInt private val progressColorHighSpendingTrend: Int,
                        @ColorInt private val progressColorOverBudget: Int,
                        val listener: BudgetItemSectionListener)
    : StatelessSection(
        SectionParameters.builder().itemResourceId(
                if (isTotalSection) R.layout.row_budget_tracking_panel_parent else R.layout.row_budget_tracking_panel_child).build()) {

    override fun getContentItemsTotal(): Int {
        return budgetItems.size
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val myBudgetItem = budgetItems[position]
        (holder as? BudgetItemViewHolder)?.apply {
            budgetItem = myBudgetItem
            trackingPanel.apply {
                setTitleText(getTitleFromCategoryName(myBudgetItem.categoryName))
                setRightSubTitle(spentString(myBudgetItem.spentAmount, myBudgetItem.budgetAmount, myBudgetItem.budgetStatus))
                setRightSubtitleColor(spentColor(myBudgetItem.budgetStatus))
                setProgress(myBudgetItem.progress)
                setProgressColor(progressColor(myBudgetItem.budgetStatus))
                setIndicatorPosition(myBudgetItem.fractionTimePeriodPassed)
            }
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return BudgetItemViewHolder(view, listener)
    }

    private fun getTitleFromCategoryName(categoryName: String): String {
        return when (categoryName) {
            BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING -> totalSpentTitle
            BudgetConstants.CATEGORY_NAME_BE_OTHER_SPENDING -> otherSpendingTitle
            else -> EngageService.getInstance().storageManager.getBudgetCategoryDescription(categoryName, Locale.getDefault().language)
        }
    }

    private fun spentString(spentAmount: BigDecimal, budgetAmount: BigDecimal, budgetStatus: BudgetConstants.BudgetStatus): String {
        return if (budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            String.format(spentOverFormat, StringUtils.formatCurrencyString(spentAmount.minus(budgetAmount).toFloat()))
        } else {
            String.format(spentNormalFormat, StringUtils.formatCurrencyString(spentAmount.toFloat()), StringUtils.formatCurrencyString(budgetAmount.toFloat()))
        }
    }

    @ColorInt
    private fun spentColor(budgetStatus: BudgetConstants.BudgetStatus): Int {
        return if (budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            spentColorOverBudget
        } else {
            spentColorNormal
        }
    }

    @ColorInt
    private fun progressColor(budgetStatus: BudgetConstants.BudgetStatus): Int {
        return when (budgetStatus) {
            BudgetConstants.BudgetStatus.NORMAL -> progressColorNormal
            BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND -> progressColorHighSpendingTrend
            BudgetConstants.BudgetStatus.OVER_BUDGET -> progressColorOverBudget
        }
    }

    interface BudgetItemSectionListener {
        fun onBudgetCategorySelected(categoryName: String)
    }
}