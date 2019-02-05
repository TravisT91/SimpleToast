package com.engageft.feature.budgets.model

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.engageft.engagekit.EngageService
import com.engageft.feature.budgets.BudgetConstants
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import utilGen1.StringUtils
import java.util.Locale

/**
 * BudgetItemDisplayHelper
 * <p>
 * Functions to help format budget item rows.
 * <p>
 * Created by kurteous on 2/1/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetItemDisplayHelper(context: Context) {

    private val totalSpentTitle = context.getString(R.string.budget_category_title_total_spent)
    private val otherSpendingTitle = context.getString(R.string.budget_category_title_other_spending)
    private val spentNormalFormat = context.getString(R.string.budget_spent_of_amount_format)
    private val spentOverFormat = context.getString(R.string.budget_spent_over_format)
    @ColorInt
    private val spentColorNormal = ContextCompat.getColor(context, R.color.budget_spent_text_normal)
    @ColorInt
    private val spentColorOverBudget = Palette.errorColor
    @ColorInt
    private val progressColorNormal = Palette.secondaryColor
    @ColorInt
    private val progressColorHighSpendingTrend = Palette.warningColor
    @ColorInt
    private val progressColorOverBudget = Palette.errorColor
    private val progressBarHeightTotal = context.resources.getDimensionPixelSize(R.dimen.trackingPanelProgressBarHeightParentGrandparent)
    private val progressBarHeightCategory = context.resources.getDimensionPixelSize(R.dimen.trackingPanelProgressBarHeightChild)

    fun title(budgetItem: BudgetItem): String {
        return when (budgetItem.categoryName) {
            BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING -> totalSpentTitle
            BudgetConstants.CATEGORY_NAME_BE_OTHER_SPENDING -> otherSpendingTitle
            else -> EngageService.getInstance().storageManager.getBudgetCategoryDescription(budgetItem.categoryName, Locale.getDefault().language)
        }
    }

    fun spentString(budgetItem: BudgetItem): String {
        return if (budgetItem.budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            String.format(spentOverFormat, StringUtils.formatCurrencyString(budgetItem.spentAmount.minus(budgetItem.budgetAmount).toFloat()))
        } else {
            String.format(spentNormalFormat, StringUtils.formatCurrencyString(budgetItem.spentAmount.toFloat()), StringUtils.formatCurrencyString(budgetItem.budgetAmount.toFloat()))
        }
    }

    @ColorInt
    fun spentColor(budgetItem: BudgetItem): Int {
        return if (budgetItem.budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            spentColorOverBudget
        } else {
            spentColorNormal
        }
    }

    @ColorInt
    fun progressColor(budgetItem: BudgetItem): Int {
        return when (budgetItem.budgetStatus) {
            BudgetConstants.BudgetStatus.NORMAL -> progressColorNormal
            BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND -> progressColorHighSpendingTrend
            BudgetConstants.BudgetStatus.OVER_BUDGET -> progressColorOverBudget
        }
    }

    fun progressBarHeight(budgetItem: BudgetItem): Int {
        return if (budgetItem.categoryName == BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING) progressBarHeightTotal else progressBarHeightCategory
    }

    fun showIndicatorText(budgetItem: BudgetItem): Boolean {
        return budgetItem.categoryName == BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING
    }
}