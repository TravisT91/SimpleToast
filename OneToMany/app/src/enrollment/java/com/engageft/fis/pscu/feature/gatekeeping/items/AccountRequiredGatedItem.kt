package com.engageft.fis.pscu.feature.gatekeeping.items

import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.ActivationCardInfo

/**
 * AccountRequiredGatedItem
 * <p>
 * Fail if account creation is required
 * </p>
 * Created by joeyhutchins on 1/7/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class AccountRequiredGatedItem(private val activationCardInfo: ActivationCardInfo) : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        if (activationCardInfo.isAccountRequired) {
            resultListener.onItemCheckFailed()
        } else {
            resultListener.onItemCheckPassed()
        }
    }
}