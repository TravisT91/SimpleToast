package com.engageft.fis.pscu.feature.budgets.extension

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
        (!categorySpending.isDailyLiving || categorySpending.amountSpentOffBudget.parseFloatDefaultToZero() != 0.0f)
    } ?: run { listOf<CategorySpending>() }
}

//fun BudgetInfo.getOtherCategories(dailyLiving: Boolean, isFirst30Days: Boolean): List<CategorySpending>? {
//    val showBudgetSetup = this.showBudgetSetup()
//
//    return this.categorySpendingList?.filter { categorySpending ->
//        categorySpending.isInOtherBudget(showBudgetSetup, isFirst30Days) &&
//                (categorySpending.isDailyLiving == dailyLiving || (!dailyLiving && categorySpending.amountSpentOffBudget.parseFloatDefaultToZero() != 0.0f))
//    }
//}

fun BudgetInfo.getOtherCategories(isInFirst30Days: Boolean = false): List<CategorySpending> {
    val otherCategories = mutableListOf(this.otherSpending)
    val showBudgetSetup = this.showBudgetSetup()

    for (categorySpending in getCategories(true)) {
        if (categorySpending.isInOtherBudget(showBudgetSetup, isInFirst30Days)) {
            otherCategories.add(categorySpending)
        }
    }

    return otherCategories
}

fun BudgetInfo.hasBudget(): Boolean {
    return this.budgetAmount.parseFloatDefaultToZero() != 0.0f
}

fun BudgetInfo.showBudgetSetup(): Boolean {
    return this.budgetAmount.parseFloatDefaultToZero() == 0.0f
}

