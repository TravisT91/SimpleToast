package com.engageft.fis.pscu.feature.palettebindings

import androidx.databinding.BindingAdapter
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.view.TrendsTrackingPanel

/**
 * TrendsPanelBinding
 * </p>
 * Contains the bindings that are used to style trends panels using the Paris
 * </p>
 * Created by Travis Tkachuk 11/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

@BindingAdapter("topRightParisStyle", requireAll = true)
fun TrendsTrackingPanel.setTopRightTextTextSizeAndFont(style: Style){
    this.setTopRightTextTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}

@BindingAdapter("topLeftParisStyle", requireAll = true)
fun TrendsTrackingPanel.setTopLeftTextTextSizeAndFont(style: Style){
    this.setTopLeftTextTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}

@BindingAdapter("titleParisStyle", requireAll = true)
fun TrendsTrackingPanel.setTitleTextSizeAndFont(style: Style){
    this.setTitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context,style))
}
