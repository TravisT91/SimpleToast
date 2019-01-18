package com.engageft.fis.pscu.feature.budgets.extension

import com.ob.ws.dom.utility.CategorySpending

/**
 * CategorySpending
 * <p>
 * CategorySpending extension functions
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

fun CategorySpending.isInOtherBudget(isSetup: Boolean, isInFirst30Days: Boolean): Boolean {
    if (isSetup) {
        if (isInFirst30Days) { // if first 30, show some categories by default
            if (!defaultEditCategories.contains(category)) { // && budgetAmount.parseFloatDefaultToZero() == 0F) { // budgetAmount condition was in gen1, but not currently in iOS
                return true
            }
        } else if (amountSpentLast30.parseFloatDefaultToZero() == 0f) { // no budget set yet so look at amount spent
            return true
        }
    } else if (budgetAmount.parseFloatDefaultToZero() == 0f) {
        return true
    }
    return false
}

private val defaultEditCategories: List<String> = listOf("Groceries", "DiningOut", "Shopping", "Entertainment")