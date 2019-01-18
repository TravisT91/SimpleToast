package com.engageft.fis.pscu.feature.budgets.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.R
import com.engageft.fis.pscu.feature.budgets.model.BudgetModel
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

/**
 * BudgetModelSection
 * <p>
 * StatelessSection for displaying a list of BudgetModels
 * <p>
 * Created by kurteous on 1/18/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetModelSection(val context: Context, val budgetModels: List<BudgetModel>, isTotalSection: Boolean)
    : StatelessSection(SectionParameters.builder().itemResourceId(
        if (isTotalSection) R.layout.row_budget_tracking_panel_parent else R.layout.row_budget_tracking_panel_child
).build()) {
    override fun getContentItemsTotal(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindItemViewHolder(p0: RecyclerView.ViewHolder?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}