package com.engageft.fis.pscu.feature

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class GoalsEmptyListSection : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.goals_empyt_list_item).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}