package com.engageft.feature.budgets

/**
 * BudgetConstants
 * <p>
 * Shared values for working with budgets
 * <p>
 * Created by kurteous on 1/23/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
object BudgetConstants {
    const val BUDGET_STATUS_HIGH_SPENDING_TREND_KEY = "HIGH_SPENDING_TREND"
    const val BUDGET_STATUS_OVER_BUDGET_KEY = "OVER_BUDGET"

    // This is used to mark the synthetic Total Spending category as such. FE = front end
    const val CATEGORY_NAME_FE_TOTAL_SPENDING = "Total Spent"

    // These are verbatim from the back end (BE = back end)
    private const val CATEGORY_NAME_BE_GROCERIES = "Groceries"
    private const val CATEGORY_NAME_BE_DINING_OUT = "DiningOut"
    private const val CATEGORY_NAME_BE_SHOPPING = "Shopping"
    private const val CATEGORY_NAME_BE_ENTERTAINMENT = "Entertainment"
    const val CATEGORY_NAME_BE_OTHER_SPENDING = "Other Spending"

    const val INT_NOT_SET = 0

    val DEFAULT_EDIT_CATEGORIES = listOf(
            CATEGORY_NAME_BE_DINING_OUT,
            CATEGORY_NAME_BE_ENTERTAINMENT,
            CATEGORY_NAME_BE_GROCERIES,
            CATEGORY_NAME_BE_SHOPPING
    )

    enum class BudgetStatus {
        NORMAL,
        HIGH_SPENDING_TREND,
        OVER_BUDGET
    }
}