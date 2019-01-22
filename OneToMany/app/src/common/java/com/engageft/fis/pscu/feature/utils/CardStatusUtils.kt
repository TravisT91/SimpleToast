package com.engageft.fis.pscu.feature.utils

import android.content.Context
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.DebitCardInfoUtils
import com.engageft.fis.pscu.R
import com.ob.domain.lookup.DebitCardStatus
import com.ob.ws.dom.ActivationCardInfo
import com.ob.ws.dom.utility.DebitCardInfo

/**
 * Created by joeyhutchins on 1/22/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
object CardStatusUtils {
    fun productCardModelStatusFromDebitCardInfo(debitCardInfo: DebitCardInfo): ProductCardModelCardStatus {
        return if (DebitCardInfoUtils.hasVirtualCard(debitCardInfo) && EngageService.getInstance().engageConfig.virtualCardEnabled)
            ProductCardModelCardStatus.CARD_STATUS_VIRTUAL
        else if (DebitCardInfoUtils.isLocked(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_LOCKED
        else if (DebitCardInfoUtils.isPendingActivation(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_PENDING
        else if (DebitCardInfoUtils.isLostStolen(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_REPLACED
        else if (DebitCardInfoUtils.isCancelled(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_CANCELED
        else if (DebitCardInfoUtils.isSuspended(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_SUSPENDED
        else if (DebitCardInfoUtils.isFraudStatus(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_CLOSED
        else if (DebitCardInfoUtils.isOrdered(debitCardInfo))
            ProductCardModelCardStatus.CARD_STATUS_ORDERED
        else
            ProductCardModelCardStatus.CARD_STATUS_ACTIVE
    }

    fun cardStatusStringForProductCardModelCardStatus(context: Context, productCardModelCardStatus :ProductCardModelCardStatus): CharSequence {
        return when (productCardModelCardStatus) {
            ProductCardModelCardStatus.CARD_STATUS_ACTIVE -> context.getString(R.string.CARD_STATUS_DISPLAY_ACTIVE)
            ProductCardModelCardStatus.CARD_STATUS_VIRTUAL -> context.getString(R.string.CARD_STATUS_DISPLAY_VIRTUAL)
            ProductCardModelCardStatus.CARD_STATUS_PENDING -> context.getString(R.string.CARD_STATUS_DISPLAY_PENDING)
            ProductCardModelCardStatus.CARD_STATUS_LOCKED -> context.getString(R.string.CARD_STATUS_DISPLAY_LOCKED)
            ProductCardModelCardStatus.CARD_STATUS_REPLACED -> context.getString(R.string.CARD_STATUS_DISPLAY_REPLACED)
            ProductCardModelCardStatus.CARD_STATUS_CANCELED -> context.getString(R.string.CARD_STATUS_DISPLAY_CANCELLED)
            ProductCardModelCardStatus.CARD_STATUS_SUSPENDED -> context.getString(R.string.CARD_STATUS_DISPLAY_SUSPENDED)
            ProductCardModelCardStatus.CARD_STATUS_CLOSED -> context.getString(R.string.CARD_STATUS_DISPLAY_CLOSED)
            ProductCardModelCardStatus.CARD_STATUS_ORDERED -> context.getString(R.string.CARD_STATUS_DISPLAY_ORDERED)
        }
    }

    fun cardStatusStringFromDebitCardInfo(context: Context, debitCardInfo: DebitCardInfo): CharSequence {
        val productCardModelCardStatus = productCardModelStatusFromDebitCardInfo(debitCardInfo)
        return cardStatusStringForProductCardModelCardStatus(context, productCardModelCardStatus)
    }

    fun ProductCardModelCardStatus.cardStatusStringRes(): Int {
        return when (this) {
            ProductCardModelCardStatus.CARD_STATUS_ACTIVE -> R.string.CARD_STATUS_DISPLAY_ACTIVE
            ProductCardModelCardStatus.CARD_STATUS_VIRTUAL -> R.string.CARD_STATUS_DISPLAY_VIRTUAL
            ProductCardModelCardStatus.CARD_STATUS_PENDING -> R.string.CARD_STATUS_DISPLAY_PENDING
            ProductCardModelCardStatus.CARD_STATUS_LOCKED -> R.string.CARD_STATUS_DISPLAY_LOCKED
            ProductCardModelCardStatus.CARD_STATUS_REPLACED -> R.string.CARD_STATUS_DISPLAY_REPLACED
            ProductCardModelCardStatus.CARD_STATUS_CANCELED -> R.string.CARD_STATUS_DISPLAY_CANCELLED
            ProductCardModelCardStatus.CARD_STATUS_SUSPENDED -> R.string.CARD_STATUS_DISPLAY_SUSPENDED
            ProductCardModelCardStatus.CARD_STATUS_CLOSED -> R.string.CARD_STATUS_DISPLAY_CLOSED
            ProductCardModelCardStatus.CARD_STATUS_ORDERED -> R.string.CARD_STATUS_DISPLAY_ORDERED
        }
    }

    /**
     * I am attempting to resolve differences in DebitCardStatus enums to ProductCardModelCardStatus
     * enums. Some don't exist in the other.
     */
    fun productCardModelStatusFromActivationInfo(activationCardInfo: ActivationCardInfo): ProductCardModelCardStatus {
        return when (activationCardInfo.cardStatus) {
            DebitCardStatus.ACTIVE.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_ACTIVE
            }
            DebitCardStatus.PENDING_ACTIVATION.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_PENDING
            }
            DebitCardStatus.CANCELED.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_CANCELED
            }
            DebitCardStatus.REPLACED.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_REPLACED
            }
            DebitCardStatus.LOCKED_USER.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            DebitCardStatus.LOCKED_PARENT.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            DebitCardStatus.LOCKED_CSR.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            DebitCardStatus.LOCKED_ADMIN.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            else -> {
                // This is bad...
                throw IllegalStateException("Unknown card status. ")
            }
        }
    }
}