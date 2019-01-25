package com.engageft.feature.budgets.extension

import com.engageft.feature.budgets.BudgetConstants
import com.ob.ws.dom.utility.CategorySpending

/**
 * CategorySpending
 * <p>
 * CategorySpending extension and utility functions
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

fun CategorySpending.isInOtherBudget(shouldShowBudgetSetup: Boolean, isInFirst30Days: Boolean): Boolean {
    if (shouldShowBudgetSetup) {
        if (isInFirst30Days) { // if first 30, show some categories by default
            if (!BudgetConstants.DEFAULT_EDIT_CATEGORIES.contains(category)) { // && budgetAmount.getFloatOrZero() == 0F) { // budgetAmount condition was in gen1, but not currently in iOS
                return true
            }
        } else if (amountSpentLast30.getFloatOrZero() == 0f) { // no budget set yet so look at amount spent
            return true
        }
    } else if (budgetAmount.getFloatOrZero() == 0f) {
        return true
    }
    return false
}

fun CategorySpending.isOtherSpending(): Boolean {
    return BudgetConstants.CATEGORY_NAME_BE_OTHER_SPENDING == this.category
}

fun CategorySpending.budgetStatus(): BudgetConstants.BudgetStatus {
    return when (this.alertType) {
        BudgetConstants.BUDGET_STATUS_OVER_BUDGET_KEY -> BudgetConstants.BudgetStatus.OVER_BUDGET
        BudgetConstants.BUDGET_STATUS_HIGH_SPENDING_TREND_KEY -> BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND
        else -> BudgetConstants.BudgetStatus.NORMAL
    }
}