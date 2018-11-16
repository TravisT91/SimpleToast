package com.engageft.fis.pscu.feature

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.fis.pscu.R

/**
 * WelcomeActivity
 *
 * Activity that has navigation graph for Welcome screen flow.
 *
 * Created by Atia Hashimi 11/6/2018.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class WelcomeActivity : LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_welcome
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}
