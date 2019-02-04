package com.engageft.fis.pscu.feature.recyclerview.rowlabel

import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * RowLabelViewHolder
 * <p>
 * RecyclerView view holder for a configurable label row.
 * <p>
 * Created by kurteous on 2/1/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class RowLabelViewHolder(val binding: com.engageft.fis.pscu.databinding.RowLabelBinding) : RecyclerView.ViewHolder(binding.root)  {

    init {
        binding.palette = Palette
    }

    fun bind(labelString: String, @StyleRes styleRes: Int) {
        binding.labelTextview.apply {
            text = labelString
            style(styleRes)
        }
    }
}