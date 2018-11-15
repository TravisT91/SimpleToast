package com.engageft.feature.gatekeeping.items

import com.engageft.engagekit.EngageService
import com.engageft.feature.gatekeeping.GatedItem
import com.engageft.feature.gatekeeping.GatedItemResultListener

/**
 * ActivationAddFundingGatedItem
 * <p>
 * Check if activation is complete.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ActivationAddFundingGatedItem : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        if (EngageService.getInstance().storageManager.isActivationComplete) {
            resultListener.onItemCheckFailed()
        } else {
            resultListener.onItemCheckPassed()
        }
    }
}