package com.engageft.feature

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.onetomany.R
/**
 * SplashActivity
 * <p>
 *    Handles showing of splash screen
 * </p>
 * Created by Atia Hashimi on 10/25/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashActivity: LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_splash
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}