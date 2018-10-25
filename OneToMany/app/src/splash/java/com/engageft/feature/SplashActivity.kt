package com.engageft.feature

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.onetomany.R

class SplashActivity: LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_splash
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}