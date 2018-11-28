package com.engageft.fis.pscu

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.DashboardAnimationEvent
import com.engageft.fis.pscu.feature.DashboardFragment
import com.engageft.fis.pscu.feature.DashboardNavigationEvent
import com.engageft.fis.pscu.feature.DashboardViewModel
import com.engageft.fis.pscu.feature.authentication.BaseAuthenticatedActivity


class AuthenticatedActivity : BaseAuthenticatedActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = R.menu.menu_navigation
        override val navigationGraphResourceId = R.navigation.navigation_authenticated
    }

    private lateinit var viewModel: DashboardViewModel

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

        viewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        setupDashboardNavigationObserving()
        setupDashboardAnimationObserving()
    }

    override fun onBackPressed() {
        // If the currently displayed fragment is DashboardFragment, give it a chance to handle back pressed.
        // It will do so if its DashboardExpandableView with card management options is expanded, by collapsing it.
        // If it's not expanded, or if the currently displayed fragment is not DashboardFragment, let
        // the parent Activity class handle onBackPressed().
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

    private fun setupDashboardNavigationObserving() {
        viewModel.navigationObservable.observe(this, Observer { navigationEvent ->
            when (navigationEvent) {
                // TODO(kurt) these may not be needed for alerts and transactions search, once transactions are overhauled in a future task.
                DashboardNavigationEvent.ALERTS -> Toast.makeText(this, "Alerts selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.TRANSACTION_SEARCH -> Toast.makeText(this, "Transactions search selected", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDashboardAnimationObserving() {
        viewModel.animationObservable.observe(this, Observer { animationEvent ->
            when (animationEvent) {
                DashboardAnimationEvent.EXPAND_IMMEDIATE -> {
                    //navView.visibility = View.GONE // for bottom nav
                    showToolbar(show = false, animate = false)
                }
                DashboardAnimationEvent.EXPAND_START -> {
                    //navView.visibility = View.GONE // for bottom nav
                    showToolbar(show = false, animate = true, animationDurationMS = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong())
                }
                DashboardAnimationEvent.EXPAND_END -> {
                    // intentionally left blank
                }
                DashboardAnimationEvent.COLLAPSE_START -> {
                    //navView.visibility = View.VISIBLE // for bottom nav
                    showToolbar(show = true, animate = true, animationDurationMS = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong())
                }
                DashboardAnimationEvent.COLLAPSE_END -> {
                    // intentionally left blank
                }
            }
        })
    }
}
