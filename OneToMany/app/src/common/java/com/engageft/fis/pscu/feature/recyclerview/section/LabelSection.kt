package com.engageft.fis.pscu.feature.recyclerview.section

import android.view.View
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

/**
 * LabelSection
 * <p>
 * Reusable Stateless section for displaying text labels in RecyclerViews
 * <p>
 * Created by kurteous on 1/22/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class LabelSection(parameters: SectionParameters) : StatelessSection(parameters) {
    private lateinit var label: CharSequence
    @StyleRes
    private var styleRes: Int = 0

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as? ViewHolder)?.textView?.text = label
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var textView: TextView = itemView.findViewById(R.id.label_textview)
    }

//    companion object {
//        fun newInstanceGroupTitle(label: String): LabelSection {
//
//        }
//    }
}