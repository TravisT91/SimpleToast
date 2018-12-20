package com.engageft.fis.pscu.feature.transactions.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import com.engageft.fis.pscu.feature.transactions.utils.TransactionUtils
import com.ob.domain.lookup.TransactionStatus
import com.ob.domain.lookup.TransactionType
import utilGen1.StringUtils

/**
 *  TransactionsAdapter
 *  </p>
 *  RecyclerView PagedListAdapter for showing all transactions or just deposit transactions in the Dashboard
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
open class TransactionsAdapter(private val context: Context,
                               private val listener: OnTransactionsAdapterListener?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            VIEW_TYPE_TRANSACTIONS_DATA -> TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_info_view, parent, false))
            VIEW_TYPE_NETWORK_STATE -> NetworkStateItemViewHolder.create(parent, null)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is TransactionViewHolder) {
//            if (hasExtraRow() && position == getItemCountInternal() - 1) {
//                // row is last, so show loading state
//                holder.bindTo(networkState)
//            } else {
//                differ.getItem(position)?.let { transaction ->
//                    holder.bindTo(transaction)
//                }
//            }
//        }
        if (holder is NetworkStateItemViewHolder) {
            holder.bindTo(networkState)
        } else if (holder is TransactionViewHolder) {
            differ.getItem(position)?.let { transaction ->
                holder.bindTo(transaction)
            }
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

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var transaction: Transaction? = null

        private val loadingView: View = itemView.findViewById(R.id.pb_loading)

        private val noTransactionsView: View = itemView.findViewById(R.id.rl_no_transactions)
        private val transactionsView: View = itemView.findViewById(R.id.cl_transactions)

        private val dayAndMonthTextView: TextView = itemView.findViewById(R.id.tv_transaction_day_month)
        private val storeTextView: TextView = itemView.findViewById(R.id.tv_transaction_store)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tv_transaction_category)
        private val amountTextView: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        private val statusTextView: TextView = itemView.findViewById(R.id.tv_transaction_status)

        private val bottomRule: View = itemView.findViewById(R.id.view_horizontal_rule_bottom)

        init {
            this.itemView.setOnClickListener {
                transaction?.let { transaction ->
                    if (transactionSelectionEnabled) {
                        listener?.onTransactionSelected(transaction)
                    }
                }
            }
        }

        fun bindTo(transaction: Transaction) {
            this.transaction = transaction

            loadingView.visibility = View.GONE
            noTransactionsView.visibility = View.GONE
            transactionsView.visibility = View.VISIBLE

            val transactionType = TransactionUtils.getTransactionType(transaction)

            dayAndMonthTextView.text = StringUtils.formatDateMonthDayForTransactionRow(transaction.date)
            storeTextView.text = if (transaction.store.isNullOrBlank()) "" else StringUtils.removeRedundantWhitespace(transaction.store!!)
            categoryTextView.text = TransactionUtils.getTransactionTypeText(context, transaction, transactionType!!)

            val transactionAmount = transaction.amount.toFloat()
            val amountString = StringUtils.formatCurrencyStringWithFractionDigits(transactionAmount, true)
            if (transactionAmount > 0F) {
                amountTextView.text = "+$amountString"
                amountTextView.setTextColor(ContextCompat.getColor(context, R.color.transactionAmountTextPositive))
            } else {
                amountTextView.text = amountString
                amountTextView.setTextColor(ContextCompat.getColor(context, R.color.transactionAmountTextDefault))
            }

            val transactionStatus = TransactionUtils.getTransactionStatus(transaction)
            statusTextView.visibility = View.GONE
            when (transactionStatus) {
                TransactionStatus.DECLINED -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text = TransactionUtils.getTransactionStatusText(context, transactionStatus)
                    itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundDeclined), PorterDuff.Mode.SRC_ATOP)
                }
                TransactionStatus.PENDING -> itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundPending), PorterDuff.Mode.SRC_ATOP)
                else -> itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundDefault), PorterDuff.Mode.SRC_ATOP)
            }
            //bottomRule.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
        }

        fun bindTo(networkState: NetworkState?) {
            // currently not using networkState
            transactionsView.visibility = View.INVISIBLE
            noTransactionsView.visibility = View.INVISIBLE
            loadingView.visibility = View.VISIBLE
        }
    }

//    private inner class PlaceholderViewHolder(itemView: View) : RecyclerView.TransactionViewHolder(itemView)
//
//    private inner class NoViewHolder(itemView: View) : RecyclerView.TransactionViewHolder(itemView) {
//        val noTransactionsTextView: TextView = itemView.findViewById(R.id.tv_label)
//    }

    interface OnTransactionsAdapterListener {
        fun onTransactionSelected(transaction: Transaction)
    }

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