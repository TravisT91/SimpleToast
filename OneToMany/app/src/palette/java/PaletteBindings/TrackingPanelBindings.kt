package com.engageft.feature.PaletteBindings

import androidx.databinding.BindingAdapter
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.view.TrackingPanel

/**
 * Tracking Panel Bindings
 * </p>
 * Contains the bindings that are used to style tracking panels using the Paris
 * </p>
 * Created by Travis Tkachuk 11/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

//Tracking Panel Bindings
@BindingAdapter("titleParisStyle", requireAll = true)
fun TrackingPanel.setTitleParisStyle(style: Style){
    this.setTitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}

@BindingAdapter("rightSubtitleParisStyle", requireAll = true)
fun TrackingPanel.setRightSubtitleParisStyle(style: Style){
    this.setRightSubtitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}

@BindingAdapter("bottomSubtitleParisStyle", requireAll = true)
fun TrackingPanel.setBottomSubtitleParisStyle(style: Style){
    this.setBottomSubtitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}

@BindingAdapter("textButtonParisStyle", requireAll = true)
fun TrackingPanel.setTextButtonParisStyle(style: Style){
    this.setTextButtonTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}

@BindingAdapter("indicatorTextParisStyle", requireAll = true)
fun TrackingPanel.setIndicatorTextParisStyle(style: Style){
    this.setIndicatorTextTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}