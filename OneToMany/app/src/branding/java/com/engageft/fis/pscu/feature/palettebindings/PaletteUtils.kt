package com.engageft.fis.pscu.feature.palettebindings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
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
    return ColorStateList(s, c)
}

fun getButtonColorStateList(context: Context, @ColorInt buttonColor: Int) : ColorStateList {
    val s = arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))
    val c = intArrayOf(getColor40PercentBlacker(buttonColor), ContextCompat.getColor(context, com.engageft.apptoolbox.R.color.structure4), buttonColor)
    return ColorStateList(s, c)
}

fun getColor40PercentBlacker(@ColorInt color: Int) : Int {
    val alpha = Color.alpha(color)
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)

    val darkerRed = (red.toFloat() * 0.6f).toInt()
    val darkerGreen = (green.toFloat() * 0.6f).toInt()
    val darkerBlue = (blue.toFloat() * 0.6f).toInt()

    return Color.argb(alpha, darkerRed, darkerGreen, darkerBlue)
}

fun getInputLabelColorStateList(@ColorInt focusedColor: Int, @ColorInt notFocused : Int, @ColorInt pressedColor: Int, @ColorInt disabledColor : Int, @ColorInt enabledColor: Int) : ColorStateList {
    val s = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled))

    val c = intArrayOf(focusedColor, notFocused, pressedColor, disabledColor, enabledColor)
    return ColorStateList(s,c)
}

fun getInputLineColorStateList(@ColorInt focusedColor: Int, @ColorInt pressedColor: Int, @ColorInt disabledColor : Int, @ColorInt enabledColor: Int) : ColorStateList {
    val s = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled))

    val c = intArrayOf(focusedColor, pressedColor, disabledColor, enabledColor)
    return ColorStateList(s,c)
}

fun getSwitchColorStateList(@ColorInt checkedColor:Int, @ColorInt unCheckedColor: Int, @ColorInt disabledColor: Int): ColorStateList {
    val s = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked))
    val c = intArrayOf(disabledColor, checkedColor, unCheckedColor)
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