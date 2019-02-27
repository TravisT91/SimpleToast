package com.engageft.fis.pscu.feature.gatekeeping

import com.engageft.fis.pscu.feature.gatekeeping.items.RequireAcceptTermsGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.SecurityQuestionsGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TwoFactorAuthGatedItem
import io.reactivex.disposables.CompositeDisposable

/**
 * LoginGateKeeper
 * <p>
 * Gatekeeper used by the login fragment for things.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginGateKeeper(compositeDisposable: CompositeDisposable, gateKeeperListener: GateKeeperListener) : BaseGateKeeper(gateKeeperListener) {
    override val gatedItems = arrayListOf(
            TwoFactorAuthGatedItem(compositeDisposable),
//            RequireEmailConfirmationGatedItem(compositeDisposable), TODO(jhutchins): FOTM-781 undo this change once a solution is made for this feature being broken on the backend.
            RequireAcceptTermsGatedItem(compositeDisposable),
            SecurityQuestionsGatedItem(compositeDisposable))
}