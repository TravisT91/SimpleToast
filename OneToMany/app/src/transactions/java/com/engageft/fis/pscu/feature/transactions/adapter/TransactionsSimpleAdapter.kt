package com.engageft.fis.pscu.feature.transactions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.fis.pscu.R

open class TransactionsSimpleAdapter(protected val listener: TransactionListener?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var transactions: List<Transaction> = listOf()

    fun updateTransactions(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transaction_info_view, parent, false), parent.context, listener)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TransactionViewHolder && transactions.size > position) {
            holder.bindTo(transactions[position])
        }
    }
}