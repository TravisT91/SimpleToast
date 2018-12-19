package com.engageft.fis.pscu.config

import com.engageft.engagekit.config.EngageKitConfig
import com.engageft.fis.pscu.BuildConfig

/**
 * EngageAppConfig
 * <p>
 * Configuration for an EngageApplication. Feature specific configurations should be put into
 * feature specific config files.
 * </p>
 * Created by joeyhutchins on 10/16/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

object EngageAppConfig : BaseAppConfig() {
    override val engageKitConfig: EngageKitConfig = object : EngageKitConfig(
            //TODO: FTM-65 undo when ready for PR
            devEnvironment = object : EngageKitConfig.EngageKitEnvironment() {
                override val serviceUrl: String = "https://appdemo.engageft-006.com"
                override val websiteUrl: String = "https://dev-care.engageft.com/" },

            prodEnvironment = object : EngageKitConfig.EngageKitEnvironment() {
                override val serviceUrl: String = "https://app.engageft-008.com"
                override val websiteUrl: String = "https://staging-pscu.engageft.com" }) {

        override val ipCheckUrl: String = "https://api.ipify.org/"
        override val refCode: String = "PSCU.mydccu-android"
        override val appPushParameter: String = "MYCARDMANAGER"
        override val brand: String = "MYCARDMANAGER"
    }

    override val heapAppId: String = if (BuildConfig.DEBUG) "" else ""
}

abstract class BaseAppConfig {
    abstract val engageKitConfig: EngageKitConfig
    abstract val heapAppId: String

    // Defaults:
    open val supportPhone: String = "18662392008"
    open val supportEmail: String = "service@myengageft.com"
    open val supportTechnicalPhone: String = "18662392008"
    open val currencyCode: String = "USD"

    var isUsingProdEnvironment: Boolean
        set(value) {
            engageKitConfig.isUsingProdEnvironment = value
        }
        get() {
            return engageKitConfig.isUsingProdEnvironment
        }
}

