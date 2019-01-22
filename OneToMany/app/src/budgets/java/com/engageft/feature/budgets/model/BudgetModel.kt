package com.engageft.feature.budgets.model

import java.math.BigDecimal

/**
 * BudgetModel
 * <p>
 * Model class to hold budget info for display in budgets list
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
data class BudgetModel(val categoryName: String? = null, var title: String? = null, val spentAmount: BigDecimal, val budgetAmount: BigDecimal, val fractionTimePeriodPassed: Float)