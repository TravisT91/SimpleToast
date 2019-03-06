package com.engageft.fis.pscu

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.DashboardFragment
import com.engageft.fis.pscu.feature.authentication.BaseAuthenticatedActivity
import com.engageft.fis.pscu.feature.branding.Palette


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

        ViewModelProviders.of(this).get(NavMenuViewModel::class.java).apply {
            goalsEnableStateObservable.observe(this@AuthenticatedActivity, Observer { goalsEnabled ->
                menu.findItem(R.id.goalsListFragment)?.let { menuItem -> menuItem.isVisible = goalsEnabled }
            })

            lastFourCardDigitsObservable.observe(this@AuthenticatedActivity, Observer { fourDigits ->
                header.title = getString(R.string.nav_drawer_card_header_format, fourDigits)
            })
        }
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

    // Variables for tracking keycode easter eggs:
    private var numVolumeUpPresses = 0
    private var numVolumeDownPresses = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!EngageAppConfig.isTestBuild) {
            return super.onKeyDown(keyCode, event)
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    numVolumeDownPresses = 0
                    numVolumeUpPresses++
                    if (numVolumeUpPresses > 2) {
                        numVolumeUpPresses = 0

                        Palette.useMockBranding = true
                        Toast.makeText(this, "Mock branding applied!", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    numVolumeUpPresses = 0
                    numVolumeDownPresses++
                    if (numVolumeDownPresses > 2) {
                        numVolumeDownPresses = 0

                        Palette.useMockBranding = false
                        Toast.makeText(this, "Mock branding removed!", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                else -> {
                    return false
                }
            }
        }
    }
}
