package com.engageft.fis.pscu

import android.os.Bundle
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.DashboardFragment
import com.engageft.fis.pscu.feature.authentication.BaseAuthenticatedActivity


class AuthenticatedActivity : BaseAuthenticatedActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = R.menu.menu_navigation
        override val navigationGraphResourceId = R.navigation.navigation_authenticated
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val menu = getNavigationMenu()!!
        val header = menu.findItem(R.id.menuCardNumberHeader)!!
        val loginResponse = EngageService.getInstance().storageManager.loginResponse!!
        val lastFour = LoginResponseUtils.getCurrentAccountInfo(loginResponse).debitCardInfo.lastFour
        header.title = getString(R.string.nav_drawer_card_header_format, lastFour.toInt())
    }

    override fun onBackPressed() {
        // If the currently displayed fragment is DashboardFragment, give it a chance to handle back pressed.
        // It will do so if its DashboardExpandableView with card management options is expanded, by collapsing it.
        // If it's not expanded, or if the currently displayed fragment is not DashboardFragment, let
        // the parent Activity class handle onBackPressed().
        // This is a one-off hack for dashboard fragment. A more general approach is in SHOW-445.
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { currentFragment ->
                if (currentFragment !is DashboardFragment || !currentFragment.handleBackPressed()) {
                    super.onBackPressed()
                }
            }
        } ?: run {
            super.onBackPressed()
        }
    }
}
