package com.engageft.feature

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.onetomany.R

/**
 * EducationTourActivity
 *
 * Manages sequence of fragments leading up to enrollment for SB.
 *
 * Created by Joseph Hutchins on 3/28/18.
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
