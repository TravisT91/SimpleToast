package com.engageft.feature.budgets.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.EngageService
import com.engageft.feature.budgets.BudgetConstants
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.RowBudgetTrackingPanelBinding
import utilGen1.StringUtils
import java.math.BigDecimal
import java.util.*
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

    private val totalSpentTitle = context.getString(R.string.budget_category_title_total_spent)
    private val otherSpendingTitle = context.getString(R.string.budget_category_title_other_spending)
    private val spentNormalFormat = context.getString(R.string.budget_spent_of_amount_format)
    private val spentOverFormat = context.getString(R.string.budget_spent_over_format)
    @ColorInt
    private val spentColorNormal = ContextCompat.getColor(context, R.color.budget_spent_text_normal)
    @ColorInt
    private val spentColorOverBudget = ContextCompat.getColor(context, R.color.budget_spent_text_over)
    @ColorInt
    private val progressColorNormal = ContextCompat.getColor(context, R.color.budget_bar_normal)
    @ColorInt
    private val progressColorHighSpendingTrend = ContextCompat.getColor(context, R.color.budget_bar_warning)
    @ColorInt
    private val progressColorOverBudget = ContextCompat.getColor(context, R.color.budget_bar_over)
    private val progressBarHeightTotal = context.resources.getDimensionPixelSize(R.dimen.trackingPanelProgressBarHeightParentGrandparent)
    private val progressBarHeightCategory = context.resources.getDimensionPixelSize(R.dimen.trackingPanelProgressBarHeightChild)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
//        if (viewType == VIEW_TYPE_CATEGORIES_LABEL) {
//
//        } else {
            val binding = RowBudgetTrackingPanelBinding.inflate(inflater, parent, false)
            return BudgetItemViewHolder(binding, listener)
//        }
    }

    override fun getItemCount(): Int {
        return if (totalBudgetItem == null && categoryBudgetItems == null) {
            0
        } else {
            1 + categoryBudgetItems!!.size
        } // 2 because 1 for totalBudgetItem, 1 for categories label
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            POSITION_TOTAL_BUDGET_ITEM -> VIEW_TYPE_TOTAL_BUDGET_ITEM
            //POSITION_CATEGORIES_LABEL -> VIEW_TYPE_CATEGORIES_LABEL
            else -> VIEW_TYPE_CATEGORY_BUDGET_ITEMS
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (position == POSITION_CATEGORIES_LABEL) {
//            // TODO
//        } else {
        val viewType = getItemViewType(position)
            val budgetItem =
                    if (viewType == VIEW_TYPE_TOTAL_BUDGET_ITEM) {
                        totalBudgetItem!!
                    } else {
                        categoryBudgetItems!![categoryBudgetItemIndexFromPosition(position)]
                    }
            budgetItem.title = getTitleFromCategoryName(budgetItem.categoryName)
            budgetItem.spentString = spentString(budgetItem.spentAmount, budgetItem.budgetAmount, budgetItem.budgetStatus)
            budgetItem.spentStringColor = spentColor(budgetItem.budgetStatus)
            budgetItem.progressColor = progressColor(budgetItem.budgetStatus)
            budgetItem.progressBarHeight = if (viewType == VIEW_TYPE_TOTAL_BUDGET_ITEM) progressBarHeightTotal else progressBarHeightCategory

            (holder as? BudgetItemViewHolder)?.bind(budgetItem)
//        }
    }

    fun updateBudgetItems(totalBudgetItem: BudgetItem, categoryBudgetItems: List<BudgetItem>) {
        this.totalBudgetItem = totalBudgetItem
        this.categoryBudgetItems = categoryBudgetItems

        notifyDataSetChanged()
    }

    private fun categoryBudgetItemIndexFromPosition(position: Int): Int {
        return max(0, position - 1)  // TODO: change this to - 2 when label is added
    }

    private fun getTitleFromCategoryName(categoryName: String): String {
        return when (categoryName) {
            BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING -> totalSpentTitle
            BudgetConstants.CATEGORY_NAME_BE_OTHER_SPENDING -> otherSpendingTitle
            else -> EngageService.getInstance().storageManager.getBudgetCategoryDescription(categoryName, Locale.getDefault().language)
        }
    }

    private fun spentString(spentAmount: BigDecimal, budgetAmount: BigDecimal, budgetStatus: BudgetConstants.BudgetStatus): String {
        return if (budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            String.format(spentOverFormat, StringUtils.formatCurrencyString(spentAmount.minus(budgetAmount).toFloat()))
        } else {
            String.format(spentNormalFormat, StringUtils.formatCurrencyString(spentAmount.toFloat()), StringUtils.formatCurrencyString(budgetAmount.toFloat()))
        }
    }

    @ColorInt
    private fun spentColor(budgetStatus: BudgetConstants.BudgetStatus): Int {
        return if (budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            spentColorOverBudget
        } else {
            spentColorNormal
        }
    }

    @ColorInt
    private fun progressColor(budgetStatus: BudgetConstants.BudgetStatus): Int {
        return when (budgetStatus) {
            BudgetConstants.BudgetStatus.NORMAL -> progressColorNormal
            BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND -> progressColorHighSpendingTrend
            BudgetConstants.BudgetStatus.OVER_BUDGET -> progressColorOverBudget
        }
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