package com.engageft.fis.pscu.feature.gatekeeping

import com.engageft.fis.pscu.feature.gatekeeping.items.AccountRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.CIPRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.PinRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TermsRequiredGatedItem
import com.ob.ws.dom.ActivationCardInfo

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 1/7/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class EnrollmentGateKeeper(activationCardInfo: ActivationCardInfo, listener: GateKeeperListener) : BaseGateKeeper(listener) {
    override val gatedItems = arrayListOf(
            PinRequiredGatedItem(activationCardInfo),
            AccountRequiredGatedItem(activationCardInfo),
            CIPRequiredGatedItem(activationCardInfo),
            TermsRequiredGatedItem(activationCardInfo))
}