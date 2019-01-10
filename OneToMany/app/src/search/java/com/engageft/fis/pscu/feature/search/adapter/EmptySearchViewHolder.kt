package com.engageft.fis.pscu.feature.search.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R

/**
 * EmptySearchViewHolder
 * <p>
 * RecyclerView view holder for showing the empty and no results states of transaction searches.
 * </p>
 * Created by kurteous on 1/6/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class EmptySearchViewHolder(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
    private val messageTextView: TextView = itemView.findViewById(R.id.emptyLabel)

    fun setMessage(message: String?) {
        if (message.isNullOrBlank()) {
            messageTextView.visibility = View.GONE
        } else {
            messageTextView.text = message
            messageTextView.visibility = View.VISIBLE
        }
    }
}