package com.engageft.feature.budgets.recyclerview

import androidx.annotation.ColorInt

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
        val title: String,
        val spent: String,
        @ColorInt val spentColor: Int,
        val progress: Float,
        @ColorInt val progressColor: Int,
        val fractionTimePeriodPassed: Float
)