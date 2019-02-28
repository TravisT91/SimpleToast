package com.engageft.fis.pscu.feature.achtransfer

object CardLoadConstants {
    const val CC_ACCOUNT_ID = -1L
    const val CC_ACCOUNT_ID_KEY = "CC_ACCOUNT_ID_KEY"
    const val CARD_NUMBER_FORMAT = "**** **** **** %s"
    const val CARD_NUMBER_REQUIRED_LENGTH = 16
    const val CVV_NUMBER_MIN_LENGTH = 3
    const val CVV_NUMBER_MAX_LENGTH = 4

    const val SUCCESS_SCREEN_TYPE_KEY = "SUCCESS_SCREEN_TYPE_KEY"
    const val ADD_ACH_BANK_SUCCESS_TYPE = 0
    const val ADD_CARD_SUCCESS_TYPE = 1
    const val VERIFIED_SUCCESS_TYPE = 2
}