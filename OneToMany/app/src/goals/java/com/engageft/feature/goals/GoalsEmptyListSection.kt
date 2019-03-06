package com.engageft.feature.goals

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.setPalette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class GoalsEmptyListSection(val context: Context) : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.goals_empyt_list_item).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.headerTextView.text = context.getString(R.string.GOALS_EMPTY_LIST_HEADER).applyTypefaceAndColorToSubString(
                ResourcesCompat.getFont(context, R.font.font_bold)!!,
                Palette.primaryColor,
                context.getString(R.string.GOALS_EMPTY_LIST_HEADER_SUBSTRING)
        )

        viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_goals))
        viewHolder.imageView.findViewById<AppCompatImageView>(R.id.imageViewIcon).setPalette(true)
        viewHolder.imageView.setPalette(true)
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)
        private val view: View = itemView.findViewById(R.id.iconView)
        val imageView: AppCompatImageView = view.findViewById(R.id.imageViewIcon)
    }
}