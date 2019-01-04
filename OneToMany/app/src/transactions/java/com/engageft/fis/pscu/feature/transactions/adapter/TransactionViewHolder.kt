package com.engageft.fis.pscu.feature.transactions.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.transactions.utils.TransactionUtils
import com.ob.domain.lookup.TransactionStatus
import utilGen1.StringUtils

class TransactionViewHolder(itemView: View, val context: Context, val listener: TransactionListener?) : RecyclerView.ViewHolder(itemView) {
    private var transaction: Transaction? = null

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
                    listener?.onTransactionSelected(transaction)
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
}