package com.engageft.onetomany


import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.feature.authentication.BaseAuthenticatedActivity

class AuthenticatedActivity : BaseAuthenticatedActivity() {

    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = R.menu.menu_navigation
        override val navigationGraphResourceId = R.navigation.navigation_authenticated
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}
