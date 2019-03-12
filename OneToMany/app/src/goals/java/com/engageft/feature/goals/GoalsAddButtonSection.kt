package com.engageft.feature.goals

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.palettebindings.setThemeFilled
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class GoalsAddButtonSection(private val listener: OnButtonSectionListener) : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.goals_add_button_item).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var button: PillButton = itemView.findViewById(R.id.addGoalButton)

        init {
            button.setThemeFilled(true)
            button.setOnClickListener { listener.onButtonClicked() }
        }
    }

    interface OnButtonSectionListener {
        fun onButtonClicked()
    }
}