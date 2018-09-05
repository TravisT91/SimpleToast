package com.engageft.showcase

import android.os.Bundle
import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig

class ActivityWithoutNavigation : LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0 // Not using a nav view!
        override val navigationGraphResourceId = R.navigation.activity_without_navigation_graph
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /*override fun instantiateNavHostFragment(): NavHostFragment {
        return NavHostFragment.create(R.navigation.nav_graph)
    }*/
}