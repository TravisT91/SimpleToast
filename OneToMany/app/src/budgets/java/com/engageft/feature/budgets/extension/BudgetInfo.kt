package com.engageft.feature.budgets.extension

import com.engageft.feature.budgets.BudgetConstants
import com.ob.ws.dom.utility.BudgetInfo
import com.ob.ws.dom.utility.CategorySpending

/**
 * BudgetInfo
 * <p>
 * BudgetInfo model extension functions
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */


fun BudgetInfo.getCategoriesForDailyLiving(): List<CategorySpending> {
    return this.categorySpendingList?.filter { categorySpending ->
        (categorySpending.isDailyLiving || categorySpending.amountSpentOffBudget.toFloatOrZeroIfEmpty() != 0.0f)
    } ?: run { listOf<CategorySpending>() }
}

fun BudgetInfo.getOtherCategoriesForDailyLiving(isInFirst30Days: Boolean = false): List<CategorySpending> {
    // add otherSpending category
    val otherCategories = mutableListOf(this.otherSpending)

    // add other categories
    val shouldShowBudgetSetup = shouldShowBudgetSetup()
    for (categorySpending in getCategoriesForDailyLiving()) {
        if (categorySpending.isInOtherBudget(shouldShowBudgetSetup, isInFirst30Days)) {
            otherCategories.add(categorySpending)
        }
    }

    return otherCategories
}

fun BudgetInfo.getCategoriesSortedByBudgetAmountDescending(withOther: Boolean, isInFirst30Days: Boolean): List<CategorySpending> {
    return filterCategorySpending(withOther, isInFirst30Days).sortedByDescending {
        it.budgetAmount.toFloatOrZeroIfEmpty()
    }
}

fun BudgetInfo.hasBudget(): Boolean {
    return this.budgetAmount.toFloatOrZeroIfEmpty() != 0.0f
}

fun BudgetInfo.shouldShowBudgetSetup(): Boolean {
    return this.budgetAmount.toFloatOrZeroIfEmpty() == 0.0f
}

fun BudgetInfo.budgetStatus(): BudgetConstants.BudgetStatus {
    return when (this.alertType) {
        BudgetConstants.BUDGET_STATUS_OVER_BUDGET_KEY -> BudgetConstants.BudgetStatus.OVER_BUDGET
        BudgetConstants.BUDGET_STATUS_HIGH_SPENDING_TREND_KEY -> BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND
        else -> BudgetConstants.BudgetStatus.NORMAL
    }
}

private fun BudgetInfo.filterCategorySpending(includeCategoryOtherSpending: Boolean, isInFirst30Days: Boolean): List<CategorySpending> {
    val categoriesToInclude = mutableListOf<CategorySpending>()
    val shouldShowBudgetSetup = shouldShowBudgetSetup()
    for (categorySpending in categorySpendingList) {
        if (!includeCategoryOtherSpending) {
            // ignore those in other categories
            if (!categorySpending.isInOtherBudget(shouldShowBudgetSetup = shouldShowBudgetSetup, isInFirst30Days = isInFirst30Days)) {
                categoriesToInclude.add(categorySpending)
            }
        } else {
            categoriesToInclude.add(categorySpending)
        }
    }
    return categoriesToInclude
}

