package com.engageft.fis.pscu.feature.gatekeeping

import com.engageft.fis.pscu.feature.gatekeeping.items.CIPRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TermsRequiredGatedItem
import com.ob.ws.dom.ActivationCardInfo

/**
 * CreateAccountEnrollmentGateKeeper
 * <p>
 * This gatekeeper is used by the CreateAccountDelegate to determine which screen to navigate to in the
 * enrollment navigation. These items are reused in other GateKeepers from the other screens.
 * </p>
 * Created by joeyhutchins on 1/7/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CreateAccountEnrollmentGateKeeper(activationCardInfo: ActivationCardInfo, listener: GateKeeperListener) : BaseGateKeeper(listener) {
    override val gatedItems = arrayListOf(
            CIPRequiredGatedItem(activationCardInfo),
            TermsRequiredGatedItem(activationCardInfo))
}