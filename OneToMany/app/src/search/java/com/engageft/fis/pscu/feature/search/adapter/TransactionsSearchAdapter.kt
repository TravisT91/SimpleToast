package com.engageft.fis.pscu.feature.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionListener
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionsSimpleAdapter

/**
 * TransactionsSearchAdapter
 * <p>
 * RecyclerView Adapter for showing transaction search results.
 * </p>
 * Created by kurteous on 1/6/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class TransactionsSearchAdapter(listener: TransactionListener?) : TransactionsSimpleAdapter(listener) {

    private var message: String? = null

    fun showNoResults(message: String?) {
        this.message = message
        updateTransactions(listOf())
    }

    fun resetSearch() {
        this.message = null
        updateTransactions(listOf())
    }

    override fun getItemCount(): Int {
        return if (transactions.isEmpty()) 1 else super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return if (transactions.isEmpty()) VIEW_TYPE_PLACEHOLDER else VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_PLACEHOLDER) {
            EmptySearchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_empty_search, parent, false), parent.context)
        } else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmptySearchViewHolder) {
            holder.setMessage(message)
        } else super.onBindViewHolder(holder, position)
    }

    companion object {
        private const val VIEW_TYPE_PLACEHOLDER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }

}