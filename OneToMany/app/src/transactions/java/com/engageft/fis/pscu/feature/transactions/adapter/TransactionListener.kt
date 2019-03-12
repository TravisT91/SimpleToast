package com.engageft.fis.pscu.feature.transactions.adapter

import com.engageft.engagekit.repository.transaction.vo.Transaction

/**
 * TransactionListener
 * <p>
 * Interface for responding to transaction list item selection
 * </p>
 * Created by kurteous on 1/6/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
interface TransactionListener {
    fun onTransactionSelected(transaction: Transaction)
}