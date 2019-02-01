package com.engageft.fis.pscu.feature.transactions.utils

import android.content.Context
import android.text.TextUtils
import androidx.annotation.NonNull
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.fis.pscu.R
import com.ob.domain.lookup.TransactionStatus
import com.ob.domain.lookup.TransactionType
import com.ob.ws.dom.utility.TransactionInfo
import java.util.*

/**
 * TransactionUtils
 *
 * Helper methods for displaying transaction info.
 *
 * Created by Kurt Mueller on 11/14/16.
 * Converted to Kotlin and expanded for Transaction by Kurt Mueller on 12/6/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object TransactionUtils {

    private var transactionTypeStringToTypeMap: MutableMap<String, TransactionType> = HashMap()
    private var transactionStatusStringToStatusMap: MutableMap<String, TransactionStatus> = HashMap()

    init {
        transactionTypeStringToTypeMap["PURCHASE"] = TransactionType.PURCHASE
        transactionTypeStringToTypeMap["RETURN"] = TransactionType.RETURN
        transactionTypeStringToTypeMap["FEE"] = TransactionType.FEE
        transactionTypeStringToTypeMap["LOAD"] = TransactionType.LOAD
        transactionTypeStringToTypeMap["REVERSE_LOAD"] = TransactionType.REVERSE_LOAD
        transactionTypeStringToTypeMap["TRANSFER"] = TransactionType.TRANSFER
        transactionTypeStringToTypeMap["ATM"] = TransactionType.ATM

        transactionStatusStringToStatusMap["PENDING"] = TransactionStatus.PENDING
        transactionStatusStringToStatusMap["SETTLED"] = TransactionStatus.SETTLED
        transactionStatusStringToStatusMap["DECLINED"] = TransactionStatus.DECLINED
    }

    fun getTransactionType(@NonNull transactionInfo: TransactionInfo): TransactionType? {
        return transactionTypeStringToTypeMap[transactionInfo.transactionType]
    }

    fun getTransactionType(@NonNull transaction: Transaction): TransactionType? {
        return  transactionTypeStringToTypeMap[transaction.transactionType]
    }

    @JvmOverloads
    fun getTransactionTypeText(@NonNull context: Context, @NonNull transactionInfo: TransactionInfo, transactionType: TransactionType? = getTransactionType(transactionInfo)): String {
        when (transactionType) {
            TransactionType.FEE -> return if (!TextUtils.isEmpty(transactionInfo.subcategoryDescription))
                transactionInfo.subcategoryDescription
            else
                context.getString(R.string.TRANSACTION_TYPE_FEE)
            TransactionType.LOAD -> return context.getString(R.string.TRANSACTION_TYPE_LOAD)
            TransactionType.REVERSE_LOAD -> return context.getString(R.string.TRANSACTION_TYPE_REVERSE_LOAD)
            TransactionType.TRANSFER -> return context.getString(R.string.TRANSACTION_TYPE_TRANSFER)
            TransactionType.ATM -> return context.getString(R.string.TRANSACTION_TYPE_ATM)
            TransactionType.PURCHASE, TransactionType.RETURN -> return EngageService.getInstance().storageManager.getBudgetCategoryDescription(
                    transactionInfo.subCategory,
                    Locale.getDefault().language,
                    if (transactionInfo.isOffBudget) context.getString(R.string.TRANSACTION_LIST_OFF_BUDGET_LABEL) else null)
            else -> return EngageService.getInstance().storageManager.getBudgetCategoryDescription(transactionInfo.subCategory, Locale.getDefault().language, if (transactionInfo.isOffBudget) context.getString(R.string.TRANSACTION_LIST_OFF_BUDGET_LABEL) else null)
        }
    }

    @JvmOverloads
    fun getTransactionTypeText(@NonNull context: Context, @NonNull transaction: Transaction, transactionType: TransactionType? = getTransactionType(transaction)): String? {
        when (transactionType) {
            TransactionType.FEE -> return if (!TextUtils.isEmpty(transaction.subCategoryDescription))
                transaction.subCategoryDescription
            else
                context.getString(R.string.TRANSACTION_TYPE_FEE)
            TransactionType.LOAD -> return context.getString(R.string.TRANSACTION_TYPE_LOAD)
            TransactionType.REVERSE_LOAD -> return context.getString(R.string.TRANSACTION_TYPE_REVERSE_LOAD)
            TransactionType.TRANSFER -> return context.getString(R.string.TRANSACTION_TYPE_TRANSFER)
            TransactionType.ATM -> return context.getString(R.string.TRANSACTION_TYPE_ATM)
            TransactionType.PURCHASE, TransactionType.RETURN -> return EngageService.getInstance().storageManager.getBudgetCategoryDescription(
                    transaction.subCategory,
                    Locale.getDefault().language,
                    if (transaction.offBudget) context.getString(R.string.TRANSACTION_LIST_OFF_BUDGET_LABEL) else null)
            else -> return EngageService.getInstance().storageManager.getBudgetCategoryDescription(transaction.subCategory, Locale.getDefault().language, if (transaction.offBudget) context.getString(R.string.TRANSACTION_LIST_OFF_BUDGET_LABEL) else null)
        }
    }

    fun getTransactionStatus(@NonNull transactionInfo: TransactionInfo): TransactionStatus? {
        var transactionStatus: TransactionStatus? = transactionStatusStringToStatusMap[transactionInfo.transactionStatus]
        if (transactionStatus == null) {
            transactionStatus = null
        }

        return transactionStatus
    }

    fun getTransactionStatus(@NonNull transaction: Transaction): TransactionStatus? {
        var transactionStatus: TransactionStatus? = transactionStatusStringToStatusMap[transaction.transactionStatus]
        if (transactionStatus == null) {
            transactionStatus = null
        }

        return transactionStatus
    }

    fun getTransactionStatusText(@NonNull context: Context, transactionInfo: TransactionInfo): String {
        return getTransactionStatusText(context, getTransactionStatus(transactionInfo)!!)
    }

    fun getTransactionStatusText(@NonNull context: Context, transactionStatus: TransactionStatus): String {
        when (transactionStatus) {
            TransactionStatus.PENDING -> return context.getString(R.string.TRANSACTION_STATUS_PENDING_TEXT)
            TransactionStatus.DECLINED -> return context.getString(R.string.TRANSACTION_STATUS_DECLINED_TEXT)
            else -> return ""
        }
    }

    fun showOffBudget(@NonNull transactionInfo: TransactionInfo): Boolean {
        return if (isLoad(transactionInfo) || isFee(transactionInfo) || !transactionInfo.isDailyLiving || isStatusDeclined(transactionInfo) || isTransfer(transactionInfo)) {
            false
        } else {
            true
        }
    }

    fun showOffBudget(@NonNull transaction: Transaction): Boolean {
        return if (isLoad(transaction) || isFee(transaction) || !transaction.dailyLiving || isStatusDeclined(transaction) || isTransfer(transaction)) {
            false
        } else {
            true
        }
    }

    fun isLoad(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.LOAD
    }

    fun isLoad(@NonNull transaction: Transaction): Boolean {
        return getTransactionType(transaction) == TransactionType.LOAD
    }

    fun isReverseLoad(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.REVERSE_LOAD
    }

    fun isFee(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.FEE
    }

    fun isFee(@NonNull transaction: Transaction): Boolean {
        return getTransactionType(transaction) == TransactionType.FEE
    }

    fun isReturn(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.RETURN
    }

    fun isTransfer(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.TRANSFER
    }

    fun isTransfer(@NonNull transaction: Transaction): Boolean {
        return getTransactionType(transaction) == TransactionType.TRANSFER
    }

    fun isTransferCard(@NonNull transactionInfo: TransactionInfo): Boolean {
//        if (EngageService.getInstance().engageConfig.getPursesEnabled()) {
//            val transactionType = getTransactionType(transactionInfo)
//            return transactionType == TransactionType.TRANSFER && transactionInfo.transPurseInfo != null && transactionInfo.transPurseInfo.size == 1
//        } else {
//            return false
//        }

        return false
    }

    fun isStatusSettled(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionStatus(transactionInfo) == TransactionStatus.SETTLED
    }

    fun isStatusPending(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionStatus(transactionInfo) == TransactionStatus.PENDING
    }

    fun isStatusDeclined(@NonNull transactionInfo: TransactionInfo): Boolean {
        return getTransactionStatus(transactionInfo) == TransactionStatus.DECLINED
    }

    fun isStatusDeclined(@NonNull transaction: Transaction): Boolean {
        return getTransactionStatus(transaction) == TransactionStatus.DECLINED
    }
}

inline class TransactionId(val id: String)