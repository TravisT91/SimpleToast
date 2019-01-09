package com.engageft.fis.pscu.feature.gatekeeping

import com.engageft.fis.pscu.feature.gatekeeping.items.AccountRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.CIPRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TermsRequiredGatedItem
import com.ob.ws.dom.ActivationCardInfo

/**
 * CardPinEnrollmentGateKeeper
 * <p>
 * This gatekeeper is used by the CardPinDelegate to determine which screen to navigate to in the
 * enrollment navigation. These items are reused in other GateKeepers from the other screens.
 * </p>
 * Created by joeyhutchins on 1/7/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CardPinEnrollmentGateKeeper(activationCardInfo: ActivationCardInfo, listener: GateKeeperListener) : BaseGateKeeper(listener) {
    override val gatedItems = arrayListOf(
            AccountRequiredGatedItem(activationCardInfo),
            CIPRequiredGatedItem(activationCardInfo),
            TermsRequiredGatedItem(activationCardInfo))
}