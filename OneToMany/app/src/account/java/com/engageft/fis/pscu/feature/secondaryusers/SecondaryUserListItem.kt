package com.engageft.fis.pscu.feature.secondaryusers

import com.engageft.apptoolbox.view.ProductCardModelCardStatus

/**
 * Created by joeyhutchins on 1/22/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

sealed class SecondaryUserListItem(val viewType: Int) {
    class ActiveSecondaryUserType(val name: CharSequence, val cardStatus: ProductCardModelCardStatus, val userId: Long, val debitCardId: Long) : SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_USER)
    class AddUserType : SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_ADD_USER)
    class CardHeaderType(val cardDisplayName: String): SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_CARD_HEADER)
    class CardFooterType(val cardUserLimit: Int): SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_CARD_FOOTER)
}