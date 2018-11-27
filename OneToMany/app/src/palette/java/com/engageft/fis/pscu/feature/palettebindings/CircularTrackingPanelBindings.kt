package com.engageft.fis.pscu.feature.palettebindings

import androidx.databinding.BindingAdapter
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.view.CircularTrackingPanel

/**
 * CircularTrackingPanelBindings
 * </p>
 * Contains the bindings that are used to style circular tracking panels using the Paris
 * </p>
 * Created by Travis Tkachuk 11/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

//fontsAndTypeface
@BindingAdapter("topAndCenteredTextParisStyle", requireAll = true)
fun CircularTrackingPanel.setTopAndCenteredLeftTextSizeAndFont(style: Style): CircularTrackingPanel {
    this.setTopAndCenteredLeftTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
    return this
}

@BindingAdapter("topRightTextParisStyle", requireAll = true)
fun CircularTrackingPanel.setTopRightTextSizeAndFont(style: Style): CircularTrackingPanel {
    this.setTopRightTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
    return this
}

@BindingAdapter("bottomLeftTextParisStyle", requireAll = true)
fun CircularTrackingPanel.setBottomLeftTextTextSizeAndFont(style: Style): CircularTrackingPanel {
    this.setBottomLeftTextTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
    return this
}

@BindingAdapter("bottomRightTextParisStyle", requireAll = true)
fun CircularTrackingPanel.setBottomRightTextTextSizeAndFont(style: Style): CircularTrackingPanel {
    this.setBottomRightTextTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
    return this
}