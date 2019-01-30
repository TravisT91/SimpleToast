package com.engageft.fis.pscu.feature

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class ToggleableLabelSection(val context: Context, private val labelItemList: List<LabelItem>, val listener: OnToggleInteractionListener)
    : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.row_toggleable_label_section).build()) {

    override fun getContentItemsTotal(): Int {
        return labelItemList.size
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as ViewHolder).apply {
            labelTextView.text = labelItemList[position].labelText
            switch.isChecked = labelItemList[position].isChecked

            switch.setOnCheckedChangeListener { _, isChecked ->
                listener.onChecked(labelItemList[position].labelId, isChecked)
            }
        }

    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
        var switch: SwitchCompat = itemView.findViewById(R.id.toggleableSwitch)
    }

    data class LabelItem(var labelId: Int = 0, val labelText: String, var isChecked: Boolean = false)

    interface OnToggleInteractionListener {
        fun onChecked(labelId: Int, isChecked: Boolean)
    }
}