package com.engageft.fis.pscu

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.DashboardAnimationEvent
import com.engageft.fis.pscu.feature.DashboardNavigationEvent
import com.engageft.fis.pscu.feature.DashboardViewModel
import com.engageft.fis.pscu.feature.authentication.BaseAuthenticatedActivity
import com.ob.ws.dom.utility.TransactionInfo


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

    private fun setupDashboardNavigationObserving() {
        viewModel.navigationObservable.observe(this, Observer { (navigationEvent, item) ->
            when (navigationEvent) {
                DashboardNavigationEvent.MOVE_MONEY -> Toast.makeText(this, "Move money selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.LOCK_UNLOCK_CARD -> Toast.makeText(this, "Lock/unlock card selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.CHANGE_PIN -> Toast.makeText(this, "Change PIN selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.REPLACE_CARD -> Toast.makeText(this, "Replace card selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.REPORT_LOST_STOLEN -> Toast.makeText(this, "Report card lost/stolen selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.CANCEL_CARD -> Toast.makeText(this, "Cancel card selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.SPENDING -> Toast.makeText(this, "Spending balance selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.SET_ASIDE -> Toast.makeText(this, "Set aside balance selected", Toast.LENGTH_SHORT).show()
                DashboardNavigationEvent.TRANSACTION_SELECTED -> Toast.makeText(this, "Transaction selected: " + (item as? TransactionInfo)!!.store, Toast.LENGTH_SHORT).show()
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
