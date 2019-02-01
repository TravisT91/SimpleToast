package com.engageft.feature.budgets.recyclerview

import androidx.annotation.ColorInt
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
        var title: String? = null,          // filled in by BudgetItemSection
        val spentAmount: BigDecimal,
        val budgetAmount: BigDecimal,
        var spentString: String? = null,    // filled in by BudgetItemSection
        @ColorInt var spentStringColor: Int,
        val budgetStatus: BudgetConstants.BudgetStatus,
        val progress: Float,
        @ColorInt var progressColor: Int,   // filled in by BudgetItemSection
        var progressBarHeight: Int,
        val fractionTimePeriodPassed: Float
)