package com.engageft.fis.pscu

import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.feature.OverviewAnimationEvent
import com.engageft.feature.OverviewNavigationEvent
import com.engageft.feature.OverviewViewModel
import com.engageft.fis.pscu.feature.authentication.BaseAuthenticatedActivity
import com.ob.ws.dom.utility.TransactionInfo


class AuthenticatedActivity : BaseAuthenticatedActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = R.menu.menu_navigation
        override val navigationGraphResourceId = R.navigation.navigation_authenticated
    }

    private lateinit var viewModel: OverviewViewModel
    private var navigationEnabled = true

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

        viewModel = ViewModelProviders.of(this).get(OverviewViewModel::class.java)
        setupOverviewNavigationObserving()
        setupOverviewAnimationObserving()
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navigationEnabled) {
            super.onSupportNavigateUp()
        } else {
            false
        }
    }

    private fun setupOverviewNavigationObserving() {
        viewModel.navigationObservable.observe(this, Observer { (navigationEvent, item) ->
            when (navigationEvent) {
                OverviewNavigationEvent.MOVE_MONEY -> Toast.makeText(this, "Move money selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.LOCK_UNLOCK_CARD -> Toast.makeText(this, "Lock/unlock card selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.CHANGE_PIN -> Toast.makeText(this, "Change PIN selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.REPLACE_CARD -> Toast.makeText(this, "Replace card selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.REPORT_LOST_STOLEN -> Toast.makeText(this, "Report card lost/stolen selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.CANCEL_CARD -> Toast.makeText(this, "Cancel card selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.SPENDING -> Toast.makeText(this, "Spending balance selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.SET_ASIDE -> Toast.makeText(this, "Set aside balance selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.TRANSACTION_SELECTED -> Toast.makeText(this, "Transaction selected: " + (item as? TransactionInfo)!!.store, Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.ALERTS -> Toast.makeText(this, "Alerts selected", Toast.LENGTH_SHORT).show()
                OverviewNavigationEvent.TRANSACTION_SEARCH -> Toast.makeText(this, "Transactions search selected", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupOverviewAnimationObserving() {
        viewModel.animationObservable.observe(this, Observer { animationEvent ->
            when (animationEvent) {
                OverviewAnimationEvent.EXPAND_IMMEDIATE -> {
                    //navView.visibility = View.GONE // for bottom nav
                    showToolbar(show = false, animate = false)
                }
                OverviewAnimationEvent.EXPAND_START -> {
                    //navView.visibility = View.GONE // for bottom nav
                    showToolbar(show = false, animate = true)
                }
                OverviewAnimationEvent.EXPAND_END -> {
                    // intentionally left blank
                }
                OverviewAnimationEvent.COLLAPSE_START -> {
                    //navView.visibility = View.VISIBLE // for bottom nav
                    showToolbar(show = true, animate = true)
                }
                OverviewAnimationEvent.COLLAPSE_END -> {
                    // intentionally left blank
                }
            }
        })
    }

    private fun showToolbar(show: Boolean, animate: Boolean) {
        // animate alpha of toolbar -- fade out as OverviewView expands
        val alphaEnd = if (show) {
            1F
        } else {
            0F
        }

        if (animate) {
            val alphaAnimator = ObjectAnimator.ofFloat(getToolbar(), "alpha", alphaEnd)
            alphaAnimator.duration = resources.getInteger(R.integer.overview_disclose_hide_duration_ms).toLong()
            alphaAnimator.start()
        } else {
            getToolbar()!!.alpha = alphaEnd
        }

        navigationEnabled = show
    }
}
