package com.engageft.fis.pscu.feature.palettebindings

import androidx.databinding.BindingAdapter
import com.engageft.apptoolbox.view.ProductCardView

/**
 * TODO: Class Name
 * </p>
 * TODO: Class Description
 * </p>
 * Created by Travis Tkachuk 12/4/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

@BindingAdapter("ProductCardView.shouldApplyBranding")
fun ProductCardView.shouldApplyBranding(shouldStyle: Boolean){
    if (shouldStyle){
        this.applyBranding()
    }
}

fun ProductCardView.applyBranding(){
    this.background =
}