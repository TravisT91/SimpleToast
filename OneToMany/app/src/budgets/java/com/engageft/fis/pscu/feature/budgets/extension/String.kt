package com.engageft.fis.pscu.feature.budgets.extension

/**
 * String
 * <p>
 * String extension functions
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

/**
 * Parses a float from a String. Defaults to 0 if parsing fails.
 *
 * Note that this means that errors (missing values, or non-numeric values) in the webservice
 * response will be ignored, and 0 will be returned. This is to maintain consistency with
 * iOS app, which uses [NSString floatValue] extensively to get a float from a String, also
 * defaulting to 0 upon parse failure.
 *
 * From gen1 StringUtils getFloat()
 *
 * @param input the String that may represent a Float
 * @return the float created from the String, if possible, or 0.
 */
fun String.getFloatOrZero(): Float {
    var result = 0f
    if (this.isNotEmpty()) {
        try {
            result = java.lang.Float.parseFloat(this)
        } catch (e: NumberFormatException) {
            // string does not represent a number
        }

    }
    return result
}