package com.engageft.showcase


import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig

class ActivityWithNavigation : LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = R.menu.menu_navigation
        override val navigationGraphResourceId = R.navigation.activity_with_navigation_graph
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}
