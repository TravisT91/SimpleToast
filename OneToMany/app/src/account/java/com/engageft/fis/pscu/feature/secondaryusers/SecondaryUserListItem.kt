package com.engageft.fis.pscu.feature.secondaryusers

/**
 * Created by joeyhutchins on 1/22/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

sealed class SecondaryUserListItem(val viewType: Int) {
    class ActiveSecondaryUserType(val name: CharSequence, val active: Boolean) : SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_USER)
    class AddUserType : SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_ADD_USER)
    class CardHeaderType(val cardDisplayName: String): SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_CARD_HEADER)
    class CardFooterType(val cardUserLimit: Int): SecondaryUserListItem(SecondaryUserListRecyclerViewAdapter.VIEW_TYPE_CARD_FOOTER)
}