package com.engageft.feature.gatekeeping

import com.engageft.feature.gatekeeping.items.OnboardingGatedItem
import com.engageft.feature.gatekeeping.items.Post30DaysGatedItem
import io.reactivex.disposables.CompositeDisposable

/**
 * DashboardGateKeeper
 * <p>
 * Gatekeeper used by the dashboard for things TBD: TODO(jhutchins): resolve items for dashboard.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DashboardGateKeeper(compositeDisposable: CompositeDisposable, gateKeeperListener: GateKeeperListener) : BaseGateKeeper(gateKeeperListener) {
    override val gatedItems = arrayListOf(OnboardingGatedItem(compositeDisposable), Post30DaysGatedItem(compositeDisposable))
}