package com.engageft.feature.PaletteBindings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.R

/**
 * Palette Utils
 * </p>
 * Contains utility methods relating to styling views with the Paris
 * </p>
 * Created by Travis Tkachuk 11/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

fun getTextStateList(@ColorInt pressedColor : Int, @ColorInt disabledColor : Int, @ColorInt enabledColor: Int) : ColorStateList {
    val s = arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))
    val c = intArrayOf(pressedColor, disabledColor, enabledColor)
    return ColorStateList(s,c)
}

@SuppressLint("PrivateResource")
fun getTextSizeAndTypefaceFromParisStyle(context: Context, style: Style): Pair<Float?, Typeface?> {
    val attrs = style.obtainStyledAttributes(context, R.styleable.TextAppearance)
    val textSize = attrs.getFloat(R.styleable.TextAppearance_android_textSize)
    val typeface = attrs.getFont(R.styleable.TextAppearance_android_fontFamily)
    attrs.recycle()
    return Pair(textSize, typeface)
}