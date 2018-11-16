package com.engageft.feature.PaletteBindings

import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ListBottomSheetDialogFragment

/**
 * DialogPaletteExtensions
 * </p>
 * Contains extension methods for InformationDialogFragment
 * </p>
 * Created by Travis Tkachuk 11/15/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

fun InformationDialogFragment.setTitleTextSizeAndFont(style: Style){
    this.setTitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!,style))
}

fun InformationDialogFragment.setMessageTextSizeAndFont(style: Style){
    this.setMessageTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!,style))
}

fun InformationDialogFragment.setPositiveButtonTextSizeAndFont(style: Style){
    this.setPositiveButtonTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!,style))
}

fun InformationDialogFragment.setNegativeButtonTextSizeAndFont(style: Style){
    this.setNegativeButtonTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!,style))
}


fun ListBottomSheetDialogFragment.setOptionTextSizeAndFont(style: Style){
    this.setOptionTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!,style))
}

fun ListBottomSheetDialogFragment.setTitleTextSizeAndFont(style: Style){
    this.setTitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!,style))
}

fun ListBottomSheetDialogFragment.setSubtitleTextSizeAndFont(style: Style){
    this.setSubtitleTextSizeAndFont(getTextSizeAndTypefaceFromParisStyle(context!!, style))
}