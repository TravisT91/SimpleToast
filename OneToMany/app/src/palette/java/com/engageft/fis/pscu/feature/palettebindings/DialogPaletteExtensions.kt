package com.engageft.fis.pscu.feature.palettebindings

import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ListBottomSheetDialogFragment
import com.engageft.fis.pscu.feature.Palette

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

fun InformationDialogFragment.applyPaletteStyles(){
    this.apply {
        setTitleTextSizeAndFont(Palette.Title3Loud)
        setMessageTextSizeAndFont(Palette.Body)
        setPositiveButtonTextSizeAndFont(Palette.FootnoteLoud)
    }
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

