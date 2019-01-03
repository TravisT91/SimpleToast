package com.engageft.fis.pscu

import android.app.Application
import android.content.Context
import com.engageft.fis.pscu.config.EngageAppConfig
import com.moe.pushlibrary.MoEHelper
import com.moe.pushlibrary.utils.MoEHelperConstants
import com.moengage.core.Logger
import com.moengage.core.MoEngage
import com.ob.ws.dom.utility.AccountInfo

/**
 *  MoEngageUtils
 *  </p>
 *  Provides helper functions for initializing MoEngage notifications and managing user identity
 *  </p>
 *  Created by Kurt Mueller on 9/5/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
object MoEngageUtils {

    @JvmStatic
    private fun getMoEngageAppId(): String {
        return EngageAppConfig.moEngageAppId
    }

    @JvmStatic
    private fun isMoEngageEnabled(): Boolean {
        return getMoEngageAppId().isNotBlank()
    }

    @JvmStatic
    fun initMoEngage() {
        if (isMoEngageEnabled()) {
            val application = OneToManyApplication.sInstance
            val moEngage = MoEngage.Builder(application, getMoEngageAppId())
                    //.setNotificationSmallIcon(R.drawable.ic_notification) // TODO(kurt): waiting on FOTM-642
                    .setNotificationLargeIcon(R.mipmap.ic_launcher)
                    .setLogLevel(Logger.VERBOSE)
                    .build()
            MoEngage.initialise(moEngage)
            MoEHelper.getInstance(application.applicationContext).setExistingUser(false)
        }
    }

    @JvmStatic
    fun setUserAttributes(user: AccountInfo) {
        if (isMoEngageEnabled()) {
            MoEHelper.getInstance(OneToManyApplication.sInstance.applicationContext)?.apply {
                // mimicking iOS exactly
                setUniqueId(user.accountId)
                setUserAttribute(MoEHelperConstants.USER_ATTRIBUTE_USER_NAME, user.email) // no helper method in MoEHelper for username, for some reason
                setFirstName(user.firstName)
                setLastName(user.lastName)
                setEmail(user.email)
                setNumber(user.phone)
            }
        }
    }

    @JvmStatic
    fun logout() {
        if (isMoEngageEnabled()) {
            MoEHelper.getInstance(OneToManyApplication.sInstance.applicationContext).logoutUser()
        }
    }
}