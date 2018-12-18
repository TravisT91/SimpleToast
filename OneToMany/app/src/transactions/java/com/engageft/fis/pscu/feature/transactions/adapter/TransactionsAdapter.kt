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

    open fun getListUpdateCallback(): ListUpdateCallback {
        return object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                adapterCallback.onInserted(position, count)
            }


            override fun onRemoved(position: Int, count: Int) {
                adapterCallback.onRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                adapterCallback.onMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                adapterCallback.onChanged(position, count, payload)
            }
        }
    }

    private val differ = AsyncPagedListDiffer<Transaction>(getListUpdateCallback(), asyncDifferConfig)

    fun submitList(pagedList: PagedList<Transaction>?) {
        differ.submitList(pagedList)
    }

    var transactionSelectionEnabled: Boolean = true

    override fun getItemCount(): Int {
        return differ.itemCount
    }

    override fun getItemViewType(position: Int): Int {
//        return if (transactionsList.isEmpty()) {
//            if (isFetchingDataComplete) VIEW_TYPE_NO_TRANSACTIONS else VIEW_TYPE_PLACEHOLDER
//        } else {
//            VIEW_TYPE_TRANSACTIONS_DATA
//        }
        return VIEW_TYPE_TRANSACTIONS_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when (viewType) {
//            VIEW_TYPE_TRANSACTIONS_DATA -> TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_info_view, parent, false))
//            VIEW_TYPE_NO_TRANSACTIONS -> NoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_text_view_bank_transfer_list_empty, parent, false))
//            else -> PlaceholderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_placeholder_view, parent, false))
//        }

        return TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_info_view, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TransactionViewHolder) {
            differ.getItem(position)?.let { transaction ->
                holder.transaction = transaction

                holder.transactionsView.visibility = View.VISIBLE
                holder.noTransactionsView.visibility = View.GONE

                val transactionType = TransactionUtils.getTransactionType(transaction)

                holder.dayAndMonthTextView.text = StringUtils.formatDateMonthDayForTransactionRow(transaction.date)
                holder.storeTextView.text = if (transaction.store.isNullOrBlank()) "" else StringUtils.removeRedundantWhitespace(transaction.store!!)
                holder.categoryTextView.text = TransactionUtils.getTransactionTypeText(context, transaction, transactionType!!)

                val transactionAmount = transaction.amount.toFloat()
                val amountString = StringUtils.formatCurrencyStringWithFractionDigits(transactionAmount, true)
                if (transactionAmount > 0F) {
                    holder.amountTextView.text = "+$amountString"
                    holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.transactionAmountTextPositive))
                } else {
                    holder.amountTextView.text = amountString
                    holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.transactionAmountTextDefault))
                }

                val transactionStatus = TransactionUtils.getTransactionStatus(transaction)
                holder.statusTextView.visibility = View.GONE
                when (transactionStatus) {
                    TransactionStatus.DECLINED -> {
                        holder.statusTextView.visibility = View.VISIBLE
                        holder.statusTextView.text = TransactionUtils.getTransactionStatusText(context, transactionStatus)
                        holder.itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundDeclined), PorterDuff.Mode.SRC_ATOP)
                    }
                    TransactionStatus.PENDING -> holder.itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundPending), PorterDuff.Mode.SRC_ATOP)
                    else -> holder.itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundDefault), PorterDuff.Mode.SRC_ATOP)
                }

                holder.bottomRule.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
            } ?: run {
                // got a null item, so show placeholder

            }
        }
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var transaction: Transaction? = null

        val noTransactionsView: View = itemView.findViewById(R.id.rl_no_transactions)
        val transactionsView: View = itemView.findViewById(R.id.cl_transactions)

        val dayAndMonthTextView: TextView = itemView.findViewById(R.id.tv_transaction_day_month)
        val storeTextView: TextView = itemView.findViewById(R.id.tv_transaction_store)
        val categoryTextView: TextView = itemView.findViewById(R.id.tv_transaction_category)
        val amountTextView: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val statusTextView: TextView = itemView.findViewById(R.id.tv_transaction_status)

        val bottomRule: View = itemView.findViewById(R.id.view_horizontal_rule_bottom)

        init {
            this.itemView.setOnClickListener {
                transaction?.let {
                    if (transactionSelectionEnabled) {
                        listener?.onTransactionSelected(it)
                    }
                }
            }
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
        const val VIEW_TYPE_PLACEHOLDER = 1
        const val VIEW_TYPE_NO_TRANSACTIONS = 2
        const val VIEW_TYPE_DASHBOARD_HEADER = 3

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