package com.engageft.fis.pscu.feature.gatekeeping

import com.engageft.fis.pscu.feature.gatekeeping.items.RequireAcceptTermsGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.RequireEmailConfirmationGatedItem
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
            RequireEmailConfirmationGatedItem(compositeDisposable),
            RequireAcceptTermsGatedItem(compositeDisposable),
            SecurityQuestionsGatedItem(compositeDisposable))
}