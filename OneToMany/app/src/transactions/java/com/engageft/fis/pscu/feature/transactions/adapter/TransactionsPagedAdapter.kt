package com.engageft.fis.pscu.feature.transactions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.engagekit.repository.util.NetworkState
import com.engageft.fis.pscu.R
import com.ob.domain.lookup.TransactionType

/**
 *  TransactionsPagedAdapter
 *  </p>
 *  RecyclerView.Adapter for showing a PagedList of transactions
 *  </p>
 *  Created by Kurt Mueller on 12/10/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
open class TransactionsPagedAdapter(private val listener: TransactionListener?, protected val retryCallback: () -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), TransactionListener {

    protected val adapterCallback = AdapterListUpdateCallback(this)

    private val asyncDifferConfig = AsyncDifferConfig.Builder<Transaction>(DIFF_CALLBACK).build()

    private var networkState: NetworkState? = null

    // subclasses that have additional items before paged list, like DashboardTransactionsAdapter,
    // need to override this to account for additional rows.
    protected open fun adjustedListUpdateCallbackPosition(position: Int): Int {
        return position
    }

    private val differ = AsyncPagedListDiffer<Transaction>(
            object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {
                    adapterCallback.onInserted(adjustedListUpdateCallbackPosition(position), count)
                }

                override fun onRemoved(position: Int, count: Int) {
                    adapterCallback.onRemoved(adjustedListUpdateCallbackPosition(position), count)
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    adapterCallback.onMoved(adjustedListUpdateCallbackPosition(fromPosition), adjustedListUpdateCallbackPosition(toPosition))
                }

                override fun onChanged(position: Int, count: Int, payload: Any?) {
                    adapterCallback.onChanged(adjustedListUpdateCallbackPosition(position), count, payload)
                }
            },
            asyncDifferConfig)

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    fun submitList(pagedList: PagedList<Transaction>?) {
        differ.submitList(pagedList)
    }

    var transactionSelectionEnabled: Boolean = true

    override fun getItemCount(): Int {
        return getItemCountInternal()
    }

    private fun getItemCountInternal(): Int {
        return differ.itemCount + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        // don't call getItemCount() directly here, as that can be overriden by a subclass like DashboardTransactionsAdapter
        return if (hasExtraRow() && position == getItemCountInternal() - 1) {
            VIEW_TYPE_NETWORK_STATE
        } else {
            VIEW_TYPE_TRANSACTIONS_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRANSACTIONS_DATA -> TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_info_view, parent, false), parent.context, this)
            VIEW_TYPE_NETWORK_STATE -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NetworkStateItemViewHolder) {
            holder.bindTo(networkState)
        } else if (holder is TransactionViewHolder) {
            differ.getItem(position)?.let { transaction ->
                holder.bindTo(transaction)
            }
        }
    }

    override fun onTransactionSelected(transaction: Transaction) {
        if (transactionSelectionEnabled) {
            listener?.onTransactionSelected(transaction)
        }
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(itemCount)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    fun clear() {
        differ.submitList(null)
    }

//    private inner class PlaceholderViewHolder(itemView: View) : RecyclerView.TransactionViewHolder(itemView)
//
//    private inner class NoViewHolder(itemView: View) : RecyclerView.TransactionViewHolder(itemView) {
//        val noTransactionsTextView: TextView = itemView.findViewById(R.id.tv_label)
//    }

    companion object {
        const val VIEW_TYPE_TRANSACTIONS_DATA = 0
        const val VIEW_TYPE_NETWORK_STATE = 1

        const val TRANSACTIONS_LIST_MAX_SIZE = 20
        // based on the layout and devices we could set this to 4 but just to make sure
        const val TRANSACTIONS_PLACEHOLDER_ITEM_COUNT = 6
        const val NO_TRANSACTIONS_ITEM_COUNT = 1
        val TRANSACTION_TYPE_LOAD = TransactionType.LOAD.name

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
                    oldItem.transactionId == newItem.transactionId


            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
                    oldItem == newItem
        }
    }
}