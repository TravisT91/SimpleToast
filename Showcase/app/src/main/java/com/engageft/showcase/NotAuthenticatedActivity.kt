package com.engageft.showcase

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig

class NotAuthenticatedActivity : LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_not_authenticated
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}