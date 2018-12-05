package com.engageft.fis.pscu.feature.palettebindings

import androidx.databinding.BindingAdapter
import com.engageft.apptoolbox.view.ProductCardView
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.squareup.picasso.Picasso

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
    val picasso = Picasso.Builder(context).loggingEnabled(true).build()
    picasso
            .load(BrandingInfoRepo.cardImageUrl)
            .into(this.binding.cardBackground)
    picasso.load("https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg").into(this.binding.productLogoImageView)
}