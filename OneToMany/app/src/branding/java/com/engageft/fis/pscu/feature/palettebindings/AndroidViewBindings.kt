package com.engageft.fis.pscu.feature.palettebindings

import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.paris.extensions.style
import com.airbnb.paris.styles.Style
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.utils.lighten

/**
 * com.engageft.fis.pscu.feature.AndroidViewBindings
 * </p>
 * Used to hold the binding adapters that are needed to style android views with Paris
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

@BindingAdapter("thumbCheckedColor", "trackCheckedColor", "thumbUncheckedColor", "trackUncheckedColor",
        "thumbDisabledColor", "trackDisabledColor", requireAll = true)
fun SwitchCompat.setSwitchTintList(@ColorInt thumbCheckedColor: Int, @ColorInt trackCheckedColor: Int,
                                   @ColorInt thumbUncheckedColor: Int, @ColorInt trackUncheckedColor: Int,
                                   @ColorInt thumbDisabledColor: Int, @ColorInt trackDisabledColor: Int) {
    this.thumbTintList = getSwitchColorStateList(thumbCheckedColor, thumbUncheckedColor, thumbDisabledColor)
    this.trackTintList = getSwitchColorStateList(trackCheckedColor, trackUncheckedColor, trackDisabledColor)
}


@BindingAdapter("Button.applyPaletteColors")
fun Button.shouldApplyPaletteColors(shouldApply: Boolean) {
    if (shouldApply) {
        this.applyPaletteColors()
    }
}
fun Button.applyPaletteColors() {
    // TODO(jhutchins): Palette should provide click states.
    this.setTextColor(getTextStateList(Palette.primaryColor, Palette.primaryColor, Palette.primaryColor))
}

@BindingAdapter("SwitchCompat.applyPaletteColors")
fun SwitchCompat.shouldApplyPaletteColors(shouldApply: Boolean){
    if (shouldApply) {
        this.applyPaletteColors()
    }
}

fun SwitchCompat.applyPaletteColors() {
    this.setSwitchTintList(
            thumbCheckedColor = Palette.successColor,
            trackCheckedColor = lighten(Palette.successColor, .6f),
            thumbUncheckedColor = ContextCompat.getColor(context!!, R.color.structure3),
            trackUncheckedColor = ContextCompat.getColor(context!!, R.color.structure2),
            thumbDisabledColor = ContextCompat.getColor(context!!, R.color.structure2),
            trackDisabledColor = ContextCompat.getColor(context!!, R.color.structure1))
}