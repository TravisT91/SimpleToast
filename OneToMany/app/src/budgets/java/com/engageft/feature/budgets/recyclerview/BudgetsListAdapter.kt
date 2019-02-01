package com.engageft.feature.budgets.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.engageft.feature.budgets.model.BudgetItem
import com.engageft.feature.budgets.model.BudgetItemDisplayHelper
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.RowBudgetTrackingPanelBinding
import com.engageft.fis.pscu.databinding.RowLabelBinding
import com.engageft.fis.pscu.feature.recyclerview.rowlabel.RowLabelViewHolder
import kotlin.math.max

/**
 * BudgetsListAdapter
 * <p>
 * RecyclerView adapter show displaying total and category budgets, with a label in between
 * <p>
 * Created by kurteous on 1/31/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetsListAdapter(context: Context,
                         private val listener: BudgetListAdapterListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalBudgetItem: BudgetItem? = null
    private var categoryBudgetItems: List<BudgetItem>? = null

    private var displayHelper = BudgetItemDisplayHelper(context)

    private val categoryLabel = context.getString(R.string.budget_categories_header)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_CATEGORIES_LABEL) {
            val binding = RowLabelBinding.inflate(inflater, parent, false)
            RowLabelViewHolder(binding)
        } else {
            val binding = RowBudgetTrackingPanelBinding.inflate(inflater, parent, false)
            BudgetItemViewHolder(binding, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (totalBudgetItem == null && categoryBudgetItems == null) {
            0
        } else {
            2 + categoryBudgetItems!!.size // 2 because 1 for totalBudgetItem, 1 for categories label
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            POSITION_TOTAL_BUDGET_ITEM -> VIEW_TYPE_TOTAL_BUDGET_ITEM
            POSITION_CATEGORIES_LABEL -> VIEW_TYPE_CATEGORIES_LABEL
            else -> VIEW_TYPE_CATEGORY_BUDGET_ITEMS
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == POSITION_CATEGORIES_LABEL) {
            (holder as? RowLabelViewHolder)?.bind(
                    labelString = categoryLabel,
                    styleRes = R.style.LabelSectionGroupTitle
            )
        } else {
            val viewType = getItemViewType(position)
            val budgetItem =
                    if (viewType == VIEW_TYPE_TOTAL_BUDGET_ITEM) {
                        totalBudgetItem!!
                    } else {
                        categoryBudgetItems!![categoryBudgetItemIndexFromPosition(position)]
                    }
            (holder as? BudgetItemViewHolder)?.bind(
                    budgetItem = budgetItem,
                    displayHelper =  displayHelper
            )
        }
    }

    fun updateBudgetItems(totalBudgetItem: BudgetItem, categoryBudgetItems: List<BudgetItem>) {
        this.totalBudgetItem = totalBudgetItem
        this.categoryBudgetItems = categoryBudgetItems

        notifyDataSetChanged()
    }

    private fun categoryBudgetItemIndexFromPosition(position: Int): Int {
        return max(0, position - 2)
    }

    companion object {
        const val POSITION_TOTAL_BUDGET_ITEM = 0
        const val POSITION_CATEGORIES_LABEL = 1

        const val VIEW_TYPE_TOTAL_BUDGET_ITEM = 0
        const val VIEW_TYPE_CATEGORIES_LABEL = 1
        const val VIEW_TYPE_CATEGORY_BUDGET_ITEMS = 2
    }

    interface BudgetListAdapterListener {
        fun onBudgetCategorySelected(categoryName: String)
    }
}