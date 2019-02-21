package com.engageft.feature.budgets.extension

import java.math.BigDecimal

/**
 * BigDecimal
 * <p>
 * BigDecimal extension functions
 * <p>
 * Created by kurteous on 1/23/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

fun BigDecimal.isEqualTo(other: BigDecimal): Boolean {
    return this.compareTo(other) == 0
}

fun BigDecimal.isLessThan(other: BigDecimal): Boolean {
    return this.compareTo(other) == -1
}

fun BigDecimal.isLessThanOrEqualTo(other: BigDecimal): Boolean {
    return isLessThan(other) || isEqualTo(other)
}

fun BigDecimal.isGreaterThan(other: BigDecimal): Boolean {
    return this.compareTo(other) == 1
}

fun BigDecimal.isZero(): Boolean {
    return this.isEqualTo(BigDecimal.ZERO)
}