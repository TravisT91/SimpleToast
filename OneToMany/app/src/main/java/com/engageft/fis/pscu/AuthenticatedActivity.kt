package com.engageft.fis.pscu

import android.os.Bundle
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
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
}
