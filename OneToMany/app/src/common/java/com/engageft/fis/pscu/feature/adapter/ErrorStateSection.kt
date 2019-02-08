package com.engageft.fis.pscu.feature.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class ErrorStateSection(private val titleText: String,
                        private val descriptionText: String,
                        private val listener: OnErrorSectionInteractionListener)
    : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.error_layout_shareable).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as ViewHolder).apply {
            titleTextView.text = titleText
            descriptionTextView.text = descriptionText
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        init {
            val errorLayout = itemView.findViewById<LinearLayout>(R.id.errorStateLayout)
            errorLayout.setBackgroundColor(Palette.errorColor)

            errorLayout.setOnClickListener {
                listener.onErrorSectionClicked()
            }
        }
    }

    interface OnErrorSectionInteractionListener {
        fun onErrorSectionClicked()
    }
}