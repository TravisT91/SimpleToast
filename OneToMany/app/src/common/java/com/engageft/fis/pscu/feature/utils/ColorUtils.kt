package com.engageft.fis.pscu.feature.utils

import android.graphics.Color

/**
 * Lightens a color by a given factor.
 * https://stackoverflow.com/questions/4928772/using-color-and-color-darker-in-android
 *
 * @param color
 * The color to lighten
 * @param factor
 * The factor to lighten the color. 0 will make the color unchanged. 1 will make the
 * color white.
 * @return lighten version of the specified color.
 *
 */
fun lighten(color: Int, factor: Float): Int {
    val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
    return Color.argb(Color.alpha(color), red, green, blue)
}