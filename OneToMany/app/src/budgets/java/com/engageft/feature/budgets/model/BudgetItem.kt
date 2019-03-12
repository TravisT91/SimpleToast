package com.engageft.feature.budgets.model

import com.engageft.feature.budgets.BudgetConstants
import java.math.BigDecimal


/**
 * BudgetItem
 * <p>
 * Data class to hold budget info for display in budgets list
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
data class BudgetItem(
        val categoryName: String,
        val spentAmount: BigDecimal,
        val budgetAmount: BigDecimal,
        val budgetStatus: BudgetConstants.BudgetStatus,
        val progress: Float,
        val fractionTimePeriodPassed: Float
)