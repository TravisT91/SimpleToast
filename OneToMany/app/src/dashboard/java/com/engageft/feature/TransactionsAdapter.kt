package com.engageft.feature

import android.content.Context
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.tools.StorageManager
import com.engageft.fis.pscu.R
import com.ob.domain.lookup.TransactionStatus
import com.ob.domain.lookup.TransactionType
import com.ob.ws.dom.utility.TransactionInfo
import utilGen1.StringUtils
import utilGen1.TransactionInfoUtils
import java.util.*
import kotlin.math.min

/**
 *  TransactionsAdapter
 *  </p>
 *  RecyclerView adapter for showing all transactions or just deposit transactions in the Overview
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class TransactionsAdapter(private val context: Context, private val listener: OnTransactionsAdapterListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allTransactionsList = mutableListOf<TransactionInfo>()
    private var depositTransactionsList = mutableListOf<TransactionInfo>()
    // initially point main transactionsList to show all transactions
    private var transactionsList = allTransactionsList
    var showDepositsOnly = false
        set(value) {
            if (field != value) {
                transactionsList = if (!value) {
                    allTransactionsList
                } else {
                    depositTransactionsList
                }

                field = value

                notifyDataSetChanged()
            }
        }
    var transactionSelectionEnabled: Boolean = true
    private var isFetchingDataComplete = false

    fun updateTransactionsList(transactionsList: List<TransactionInfo>) {
        // sort by date, newest first
        Collections.sort<TransactionInfo>(transactionsList) { o1, o2 ->
            if (!TextUtils.isEmpty(o1.isoDate) && !TextUtils.isEmpty(o2.isoDate)) {
                StorageManager.compareIso8601DateString(o1.isoDate, o2.isoDate, true)
            } else {
                0
            }
        }
        allTransactionsList.clear()
        allTransactionsList.addAll(transactionsList)

        depositTransactionsList.clear()
        run loop@{
            allTransactionsList.forEach {
                if (it.transactionType == TRANSACTION_TYPE_LOAD) {
                    depositTransactionsList.add(it)
                    // no need to iterate once 20 transactions have been added to the list of deposits
                    if (depositTransactionsList.size >= TRANSACTIONS_LIST_MAX_SIZE) {
                        return@loop // this is how you break out of a loop in Kotlin
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun notifyRetrievingTransactionsStarted() {
        isFetchingDataComplete = false
        transactionsList.clear()
        notifyDataSetChanged()
    }

    fun notifyRetrievingTransactionsFinished() {
        isFetchingDataComplete = true
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (transactionsList.isEmpty()) {
            if (isFetchingDataComplete) VIEW_TYPE_NO_TRANSACTIONS else VIEW_TYPE_PLACEHOLDER
        } else {
            VIEW_TYPE_TRANSACTIONS_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRANSACTIONS_DATA -> TransactionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_info_view, parent, false))
            VIEW_TYPE_NO_TRANSACTIONS -> NoTransactionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_text_view_bank_transfer_list_empty, parent, false))
            else -> PlaceholderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_placeholder_view, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (transactionsList.isEmpty()) {
            if (isFetchingDataComplete) NO_TRANSACTIONS_ITEM_COUNT else TRANSACTIONS_PLACEHOLDER_ITEM_COUNT
        } else {
            min(transactionsList.size, TRANSACTIONS_LIST_MAX_SIZE)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TransactionsViewHolder) {
            val transactionInfo = transactionsList[position]
            holder.transactionInfo = transactionInfo

            holder.transactionsView.visibility = View.VISIBLE
            holder.noTransactionsView.visibility = View.GONE

            val transactionType = TransactionInfoUtils.getTransactionType(transactionInfo)

            holder.dayAndMonthTextView.text = StringUtils.formatDateMonthDayForTransactionRow(transactionInfo.isoDate)
            holder.storeTextView.text = StringUtils.removeRedundantWhitespace(transactionInfo.store)
            holder.categoryTextView.text = TransactionInfoUtils.getTransactionTypeText(context, transactionInfo, transactionType!!)

            val transactionAmount = StringUtils.getFloatFromString(transactionInfo.amount)
            val amountString = StringUtils.formatCurrencyStringWithFractionDigits(transactionAmount, true)
            if (transactionAmount > 0) {
                holder.amountTextView.text = "+$amountString"
                holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.transactionAmountTextPositive))
            } else {
                holder.amountTextView.text = amountString
                holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.transactionAmountTextDefault))
            }

            val transactionStatus = TransactionInfoUtils.getTransactionStatus(transactionInfo)
            holder.statusTextView.visibility = View.GONE
            when (transactionStatus) {
                TransactionStatus.DECLINED -> {
                    holder.statusTextView.visibility = View.VISIBLE
                    holder.statusTextView.text = TransactionInfoUtils.getTransactionStatusText(context, transactionStatus)
                    holder.itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundDeclined), PorterDuff.Mode.SRC_ATOP)
                }
                TransactionStatus.PENDING -> holder.itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundPending), PorterDuff.Mode.SRC_ATOP)
                else -> holder.itemView.background.setColorFilter(ContextCompat.getColor(context, R.color.transactionRowBackgroundDefault), PorterDuff.Mode.SRC_ATOP)
            }

            holder.bottomRule.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
        }
    }

    private inner class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var transactionInfo: TransactionInfo? = null

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
                transactionInfo?.let { transactionInfo ->
                    if (transactionSelectionEnabled) {
                        listener?.onTransactionInfoSelected(transactionInfo)
                    }
                }
            }
        }
    }

    private inner class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private inner class NoTransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noTransactionsTextView: TextView = itemView.findViewById(R.id.tv_label)
    }

    interface OnTransactionsAdapterListener {
        fun onTransactionInfoSelected(transactionInfo: TransactionInfo)
    }

    private companion object {
        const val VIEW_TYPE_TRANSACTIONS_DATA = 0
        const val VIEW_TYPE_PLACEHOLDER = 1
        const val VIEW_TYPE_NO_TRANSACTIONS = 2

        const val TRANSACTIONS_LIST_MAX_SIZE = 20
        // based on the layout and devices we could set this to 4 but just to make sure
        const val TRANSACTIONS_PLACEHOLDER_ITEM_COUNT = 6
        const val NO_TRANSACTIONS_ITEM_COUNT = 1
        val TRANSACTION_TYPE_LOAD = TransactionType.LOAD.name
    }
}