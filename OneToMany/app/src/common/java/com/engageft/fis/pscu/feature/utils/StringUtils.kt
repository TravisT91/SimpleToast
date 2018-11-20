package com.engageft.fis.pscu.feature.utils

/**
 * StringUtils
 * <p>
 * Kotlin extensions for string methods
 * </p>
 * Created by joeyhutchins on 11/20/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object StringUtils {
    fun concatenateFromList(items: ArrayList<String>, delimeter: String): String {
        var message = ""
        if (items.isNotEmpty()) {
            var message = items.removeAt(0)
            for (m: String in items) {
                message += delimeter + m
            }
        }
        return message
    }
}