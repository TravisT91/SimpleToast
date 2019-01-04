package com.engageft.fis.pscu.feature.transactions.adapter

import com.engageft.engagekit.repository.transaction.vo.Transaction

interface TransactionListener {
    fun onTransactionSelected(transaction: Transaction)
}