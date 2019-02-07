package com.engageft.fis.pscu.feature.recyclerview.toggleablelabel

import android.view.View
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.util.setStyle
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteColors
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class ToggleableLabelSection(private val labelItemList: List<LabelItem>,
                             @StyleRes private var styleId: Int = NOT_SET,
                             private val listener: OnToggleInteractionListener)
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

            if (labelItemList[position].disableSection) {
                labelTextView.isEnabled = false
                switch.isEnabled = false
            }
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
        val switch: SwitchCompat = itemView.findViewById(R.id.toggleableSwitch)

        init {
            switch.applyPaletteColors()
            if (styleId != NOT_SET) {
                labelTextView.setStyle(styleId)
            }
        }
    }

    data class LabelItem(val labelId: Int = 0, val labelText: String, val isChecked: Boolean = false, val disableSection: Boolean)

    interface OnToggleInteractionListener {
        fun onChecked(labelId: Int, isChecked: Boolean)
    }

    private companion object {
        const val NOT_SET = -1
    }
}