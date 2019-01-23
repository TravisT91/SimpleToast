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
        (categorySpending.isDailyLiving || categorySpending.amountSpentOffBudget.getFloatOrZero() != 0.0f)
    } ?: run { listOf<CategorySpending>() }
}

fun BudgetInfo.getOtherCategoriesForDailyLiving(isInFirst30Days: Boolean = false): List<CategorySpending> {
    // add otherSpending category
    val otherCategories = mutableListOf(this.otherSpending)

    // add other categories
    val showBudgetSetup = showBudgetSetup()
    for (categorySpending in getCategoriesForDailyLiving()) {
        if (categorySpending.isInOtherBudget(showBudgetSetup, isInFirst30Days)) {
            otherCategories.add(categorySpending)
        }
    }

    return otherCategories
}

fun BudgetInfo.getCategoriesSortedByBudgetAmountDescending(withOther: Boolean, isInFirst30Days: Boolean): List<CategorySpending> {
    return filterCategorySpending(withOther, isInFirst30Days).sortedByDescending {
        it.budgetAmount.getFloatOrZero()
    }
}

fun BudgetInfo.hasBudget(): Boolean {
    return this.budgetAmount.getFloatOrZero() != 0.0f
}

fun BudgetInfo.showBudgetSetup(): Boolean {
    return this.budgetAmount.getFloatOrZero() == 0.0f
}

fun BudgetInfo.budgetStatus(): BudgetConstants.BudgetStatus {
    return when (this.alertType) {
        BudgetConstants.BUDGET_STATUS_OVER_BUDGET -> BudgetConstants.BudgetStatus.OVER_BUDGET
        BudgetConstants.BUDGET_STATUS_HIGH_SPENDING_TREND -> BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND
        else -> BudgetConstants.BudgetStatus.NORMAL
    }
}

private fun BudgetInfo.filterCategorySpending(withOther: Boolean, isInFirst30Days: Boolean): List<CategorySpending> {
    val categoriesToInclude = mutableListOf<CategorySpending>()
    val showBudgetSetup = showBudgetSetup()
    for (categorySpending in categorySpendingList) {
        if (!withOther) {
            // ignore those in other categories
            if (!categorySpending.isInOtherBudget(isSetup = showBudgetSetup, isInFirst30Days = isInFirst30Days)) {
                categoriesToInclude.add(categorySpending)
            }
        } else {
            categoriesToInclude.add(categorySpending)
        }
    }
    return categoriesToInclude
}

