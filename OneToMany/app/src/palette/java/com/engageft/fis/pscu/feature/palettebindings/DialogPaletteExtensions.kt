package com.engageft.fis.pscu.feature.palettebindings

import android.content.Context
import androidx.core.content.ContextCompat
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ListBottomSheetDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.Palette

/**
 * DialogPaletteExtensions
 * </p>
 * Contains extension methods for InformationDialogFragment
 * </p>
 * Created by Travis Tkachuk 11/15/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

fun InformationDialogFragment.applyPaletteStyles(context: Context) {
    this.apply {
        titleTextSizeAndFont = getTextSizeAndTypefaceFromParisStyle(context, Palette.Title3Loud)
        messageTextSizeAndFont = getTextSizeAndTypefaceFromParisStyle(context, Palette.Body)
        positiveButtonTextSizeAndFont = getTextSizeAndTypefaceFromParisStyle(context, Palette.FootnoteMedium)
        negativeButtonTextSizeAndFont = getTextSizeAndTypefaceFromParisStyle(context, Palette.FootnoteMedium)
        titleTextColor = ContextCompat.getColor(context, R.color.structure6)
        messageTextColor = ContextCompat.getColor(context, R.color.structure5)
        positiveButtonTextColor = Palette.primaryColor
        negativeButtonTextColor = Palette.primaryColor
    }
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

