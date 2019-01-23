package com.engageft.feature.budgets.model

import androidx.annotation.ColorInt

/**
 * BudgetModel
 * <p>
 * Model class to hold budget info for display in budgets list
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
data class BudgetModel(
        val categoryName: String,
        val title: String,
        val spent: String,
        @ColorInt val spentColor: Int,
        val progress: Float,
        @ColorInt val progressColor: Int,
        val fractionTimePeriodPassed: Float
)