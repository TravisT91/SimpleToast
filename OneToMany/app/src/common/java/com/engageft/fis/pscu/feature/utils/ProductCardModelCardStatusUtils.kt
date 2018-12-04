package com.engageft.fis.pscu.feature.utils

import androidx.annotation.StringRes
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.fis.pscu.R

@StringRes
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
