package com.engageft.fis.pscu.feature.transactions.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.BudgetCategory
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.setParisStyle
import java.util.*

/**
 * CategoryAdapter
 *
 * Created by Travis Tkachuk 1/30/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

class CategoryAdapter(
        private val context: Context,
        private val categories: List<BudgetCategory>,
        private val onCategorySelectedListener: (String) -> Unit,
        private val currentSubCategory: String?)
    : RecyclerView.Adapter<CategoryViewHolder>() {

    private val horizontalPadding = Math.round(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            context.resources.displayMetrics))

    private val itemTextPadding = Math.round(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12f,
            context.resources.displayMetrics))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_with_header_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun getCurrentItemPosition(): Int {
        currentSubCategory?.let { currentSubCategory ->
            categories.forEachIndexed { index, budgetCategory ->
                budgetCategory.subCategories.forEach {
                    if (it.name == currentSubCategory)
                        return index
                }
            }
        }
        return 0
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        val parentCategory = categories[position]
        val subCategories = categories[position].subCategories

        holder.apply {
            val sm = EngageService.getInstance().storageManager
            val lang = Locale.getDefault().language
            val parentName = sm.getBudgetCategoryDescription(parentCategory.name, lang)
            title.text = parentName.toUpperCase()
            categoryContainer.removeAllViews()

            subCategories.forEach { subCategory ->

                categoryContainer.addView(
                        TextView(context).apply {
                            text = sm.getBudgetCategoryDescription(subCategory.name, lang)
                                    .removePrefix("$parentName: ")
                            setParisStyle(Palette.Body)
                            setSingleLine(true)
                            if (currentSubCategory == subCategory.name) {
                                val successCheck = ContextCompat.getDrawable(
                                        context, R.drawable.ic_category_checkmark)?.apply {
                                }
                                compoundDrawablePadding = itemTextPadding
                                setTextColor(Palette.primaryColor)
                                setCompoundDrawablesRelativeWithIntrinsicBounds(
                                        null, //start
                                        null, //top
                                        successCheck, //end
                                        null //bottom
                                )
                            } else {
                                setTextColor(ContextCompat.getColor(context, R.color.structure6))
                            }
                            setPadding(horizontalPadding, itemTextPadding, horizontalPadding, itemTextPadding)
                            setOnClickListener { _ ->
                                onCategorySelectedListener.invoke(subCategory.name)
                            }
                        })

                if (parentCategory.subCategories.last() != subCategory) {
                    categoryContainer.addView(
                            LayoutInflater.from(context).inflate(
                                    R.layout.divider,
                                    holder.categoryContainer,
                                    false).apply {
                                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                                    marginStart = horizontalPadding
                                }
                            })
                }
            }
        }
    }
}
