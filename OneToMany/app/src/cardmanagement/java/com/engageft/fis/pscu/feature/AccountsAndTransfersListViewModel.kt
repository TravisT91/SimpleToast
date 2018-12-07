package com.engageft.fis.pscu.feature

class AccountsAndTransfersListViewModel: BaseEngageViewModel() {

    enum class BankAccount {
        NONE,
        UNVERIFIED_BANK,
        VERIFIED_BANK
    }

    enum class Transfers {
        SCHEDULED_TRANSFER,
        HISTORICAL_TRANSFERS
    }


}