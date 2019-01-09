package com.engageft.fis.pscu.feature.gatekeeping.items

import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.ActivationCardInfo

/**
 * CIPRequiredGatedItem
 * <p>
 * Fail if identity needs to be verified.
 * </p>
 * Created by joeyhutchins on 1/7/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CIPRequiredGatedItem(private val activationCardInfo: ActivationCardInfo) : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        if (activationCardInfo.isCipRequired) {
            resultListener.onItemCheckFailed()
        } else {
            resultListener.onItemCheckPassed()
        }
    }
}