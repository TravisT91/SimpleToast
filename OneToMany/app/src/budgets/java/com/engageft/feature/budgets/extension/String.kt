package com.engageft.feature.budgets.extension

import java.lang.NumberFormatException
import java.math.BigDecimal

/**
 * String
 * <p>
 * String extension functions
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

/**
 * Parses a float from a String. Defaults to 0f if the String is empty.
 * CategorySpending budgetAmount can be empty. For our purposes, this is the same as 0f.
 * If the non-empty String cannot be parsed, this will throw a NumberFormatException.
 *
 * From gen1 StringUtils getFloat()
 *
 * @param input the String that may represent a Float
 * @return the float created from the String, if possible, or 0.
 */
@Throws(NumberFormatException::class)
fun String.toFloatOrZeroIfEmpty(): Float {
    return if (this.isEmpty()) 0f
    else java.lang.Float.parseFloat(this)
}

/**
 * Parses a BigDecimal from a String. Defaults to BigDecimal.ZERO if the String is empty.
 * CategorySpending budgetAmount can be empty. For our purposes, this is the same as BigDecimal.ZERO.
 * If the non-empty String cannot be parsed, this will throw a NumberFormatException.
 */
@Throws(NumberFormatException::class)
fun String.toBigDecimalOrZeroIfEmpty(): BigDecimal {
    return if (this.isEmpty()) BigDecimal.ZERO
    else BigDecimal(this)
}