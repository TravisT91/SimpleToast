package com.engageft.fis.pscu.feature.search.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R

class EmptySearchViewHolder(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
    private val messageTextView: TextView = itemView.findViewById(R.id.emptyLabel)

    fun setMessage(message: String?) {
        messageTextView.text = if (message.isNullOrBlank()) "" else message
    }
}