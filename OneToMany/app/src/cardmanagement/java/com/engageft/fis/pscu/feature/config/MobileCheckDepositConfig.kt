package com.engageft.fis.pscu.feature.config

import android.app.Activity
import android.content.pm.PackageManager

/**
 * MobileCheckDepositConfig
 * <p>
 * Config object for providing details of ingo app installation state, package name, and website.
 * </p>
 * Created by joeyhutchins on 12/17/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object MobileCheckDepositConfig {
    val ingoAppPackage = "com.chexar.ingo.android"
    val ingoAppWebsite = "https://www.ingomoney.com/"

    fun isIngoPackageInstalled(activity: Activity): Boolean {
        val packageManager = activity.packageManager
        var found = true
        try {
            packageManager.getPackageInfo(MobileCheckDepositConfig.ingoAppPackage, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            found = false
        }

        return found
    }
}