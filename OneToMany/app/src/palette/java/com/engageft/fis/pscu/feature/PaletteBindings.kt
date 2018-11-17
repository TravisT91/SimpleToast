package com.engageft.fis.pscu.feature

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.paris.extensions.style
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.R
import com.engageft.apptoolbox.view.PillButton

/**
 * TODO: Class Name
 * </p>
 * TODO: Class Description
 * </p>
 * Created by Travis Tkachuk 11/6/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

@BindingAdapter("parisStyle", requireAll = true)
fun TextView.setParisStyle(style: Style?){
    style?.let {
        this.style(it)
    }
}

@BindingAdapter("switchButtonTint",requireAll = true)
fun Switch.setButtonTint(@ColorInt color: Int){
    this.thumbDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    this.trackDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
}

@BindingAdapter("setThemeWithFill", requireAll = true)
fun PillButton.setThemeWithFill(isFilled: Boolean){
    if (isFilled){
        this.setColorStateList(
                fillPressed = Palette.secondaryColor,
                strokePressed = Palette.secondaryColor,
                fillEnabled = Palette.primaryColor,
                strokeEnabled = Palette.primaryColor,
                fillDisabled = ContextCompat.getColor(this.context, R.color.structure2),
                strokeDisabled = ContextCompat.getColor(this.context, R.color.structure2))
        this.setTextColor(
                getTextStateList(
                        pressedColor = ContextCompat.getColor(this.context, R.color.white),
                        enabledColor = ContextCompat.getColor(this.context, R.color.white),
                        disabledColor = ContextCompat.getColor(this.context, R.color.structure4)))
    } else {
        this.setColorStateList(
                fillPressed = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokePressed = Palette.secondaryColor,
                fillEnabled = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokeEnabled = Palette.primaryColor,
                fillDisabled = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokeDisabled = ContextCompat.getColor(this.context, R.color.structure2))
        this.setTextColor(
                getTextStateList(
                        pressedColor = Palette.secondaryColor,
                        enabledColor = Palette.primaryColor,
                        disabledColor = ContextCompat.getColor(this.context, R.color.structure4)))
    }
}


fun getTextStateList(@ColorInt pressedColor : Int, @ColorInt disabledColor : Int, @ColorInt enabledColor: Int) : ColorStateList {
    val s = arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))
    val c = intArrayOf(pressedColor, disabledColor, enabledColor)
    return ColorStateList(s,c)
}

fun getTextSizeAndTypefaceFromParisStyle(context: Context, style: Style): Pair<Float?, Typeface?> {
    val attrs = style.obtainStyledAttributes(context, R.styleable.TextAppearance)
    val textSize = attrs.getFloat(R.styleable.TextAppearance_android_textSize)
    val typeface = attrs.getFont(R.styleable.TextAppearance_android_fontFamily)
    attrs.recycle()
    return Pair(textSize, typeface)
}