package utilGen1

import android.content.Context
import android.text.TextUtils
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.ob.domain.lookup.TransactionStatus
import com.ob.domain.lookup.TransactionType
import com.ob.ws.dom.utility.TransactionInfo
import java.util.*

/**
 * TODO: CLASS NAME
 *
 * TODO: CLASS DESCRIPTION
 *
 * Created by Kurt Mueller on 11/14/16.
 * Copyright (c) 2016 Engage FT. All rights reserved.
 */
object TransactionInfoUtils {

    private val transactionTypeStringToTypeMap: MutableMap<String, TransactionType>
    private val transactionStatusStringToStatusMap: MutableMap<String, TransactionStatus>

    init {
        transactionTypeStringToTypeMap = HashMap()
        transactionTypeStringToTypeMap["PURCHASE"] = TransactionType.PURCHASE
        transactionTypeStringToTypeMap["RETURN"] = TransactionType.RETURN
        transactionTypeStringToTypeMap["FEE"] = TransactionType.FEE
        transactionTypeStringToTypeMap["LOAD"] = TransactionType.LOAD
        transactionTypeStringToTypeMap["REVERSE_LOAD"] = TransactionType.REVERSE_LOAD
        transactionTypeStringToTypeMap["TRANSFER"] = TransactionType.TRANSFER
        transactionTypeStringToTypeMap["ATM"] = TransactionType.ATM

        transactionStatusStringToStatusMap = HashMap()
        transactionStatusStringToStatusMap["PENDING"] = TransactionStatus.PENDING
        transactionStatusStringToStatusMap["SETTLED"] = TransactionStatus.SETTLED
        transactionStatusStringToStatusMap["DECLINED"] = TransactionStatus.DECLINED
    }

    fun getTransactionType(transactionInfo: TransactionInfo): TransactionType? {
        var transactionType = transactionTypeStringToTypeMap[transactionInfo.transactionType]
        if (transactionType == null) {
            transactionType = null
        }

        return transactionType
    }

    fun getTransactionTypeText(context: Context, transactionInfo: TransactionInfo, transactionType: TransactionType = getTransactionType(transactionInfo)!!): String {
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

    fun getTransactionStatus(transactionInfo: TransactionInfo): TransactionStatus? {
        var transactionStatus = transactionStatusStringToStatusMap[transactionInfo.transactionStatus]
        if (transactionStatus == null) {
            transactionStatus = null
        }

        return transactionStatus
    }

    fun getTransactionStatusText(context: Context, transactionInfo: TransactionInfo): String {
        return getTransactionStatusText(context, getTransactionStatus(transactionInfo)!!)
    }

    fun getTransactionStatusText(context: Context, transactionStatus: TransactionStatus): String {
        return when (transactionStatus) {
            TransactionStatus.PENDING ->
                context.getString(R.string.TRANSACTION_STATUS_PENDING_TEXT)
            TransactionStatus.DECLINED ->
                context.getString(R.string.TRANSACTION_STATUS_DECLINED_TEXT)
            else -> ""
        }
    }

    fun showOffBudget(transactionInfo: TransactionInfo): Boolean {
        return !(isLoad(transactionInfo) ||
                        this.isFee(transactionInfo) ||
                        !transactionInfo.isDailyLiving ||
                        isStatusDeclined(transactionInfo) ||
                        isTransfer(transactionInfo))
    }

    fun isLoad(transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.LOAD
    }

    fun isReverseLoad(transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.REVERSE_LOAD
    }

    fun isFee(transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.FEE
    }

    fun isReturn(transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.RETURN
    }

    fun isTransfer(transactionInfo: TransactionInfo): Boolean {
        return getTransactionType(transactionInfo) == TransactionType.TRANSFER
    }

    fun isTransferCard(transactionInfo: TransactionInfo): Boolean {
        // In gen1 this is always false:
        return false
        /*if (EngageService.getInstance().engageConfig.getPursesEnabled()) {
            val transactionType = getTransactionType(transactionInfo)
            return transactionType == TransactionType.TRANSFER && transactionInfo.transPurseInfo != null && transactionInfo.transPurseInfo.size == 1
        } else {
            return false
        }*/
    }

    fun isStatusSettled(transactionInfo: TransactionInfo): Boolean {
        return getTransactionStatus(transactionInfo) == TransactionStatus.SETTLED
    }

    fun isStatusPending(transactionInfo: TransactionInfo): Boolean {
        return getTransactionStatus(transactionInfo) == TransactionStatus.PENDING
    }

    fun isStatusDeclined(transactionInfo: TransactionInfo): Boolean {
        return getTransactionStatus(transactionInfo) == TransactionStatus.DECLINED
    }
}
