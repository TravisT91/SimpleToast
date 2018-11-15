package com.engageft.feature.gatekeeping

import com.engageft.feature.gatekeeping.items.PendingCardActivationGatedItem
import com.engageft.feature.gatekeeping.items.SecurityQuestionsGatedItem
import io.reactivex.disposables.CompositeDisposable

/**
 * LoginGateKeeper
 * <p>
 * Gatekeeper used by the login fragment for things TBD: TODO(jhutchins): resolve items for login.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginGateKeeper(compositeDisposable: CompositeDisposable, gateKeeperListener: GateKeeperListener) : BaseGateKeeper(gateKeeperListener) {
    override val gatedItems = arrayListOf(SecurityQuestionsGatedItem(compositeDisposable), PendingCardActivationGatedItem(compositeDisposable))
}