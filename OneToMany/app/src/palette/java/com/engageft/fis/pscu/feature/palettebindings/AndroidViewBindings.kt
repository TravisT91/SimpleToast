package com.engageft.fis.pscu.feature.palettebindings

import android.graphics.PorterDuff
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.airbnb.paris.extensions.style
import com.airbnb.paris.styles.Style

/**
 * com.engageft.fis.pscu.feature.PaletteBindings
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

@BindingAdapter("switchButtonTint",requireAll = true)
fun Switch.setButtonTint(@ColorInt color: Int){
    this.thumbDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    this.trackDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
}

