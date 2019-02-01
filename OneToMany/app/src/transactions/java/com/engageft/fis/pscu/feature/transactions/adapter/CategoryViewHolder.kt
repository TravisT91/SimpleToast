package com.engageft.fis.pscu.feature.transactions.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R

/**
 * CategoryViewHolder
 *
 * Created by Travis Tkachuk 1/30/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CategoryViewHolder(val view : View) : RecyclerView.ViewHolder(view){
    val title = view.findViewById<TextView>(R.id.title)!!
    val categoryContainer = view.findViewById<LinearLayout>(R.id.itemContainer)!!
    var containsCurrentlySelected: Boolean = false
}