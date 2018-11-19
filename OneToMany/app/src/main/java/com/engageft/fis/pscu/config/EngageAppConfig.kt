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
    override val engageKitConfig: EngageKitConfig = object : EngageKitConfig(object : EngageKitConfig.EngageKitEnvironment() {
        override val serviceUrl: String = "https://appdemo.engageft-007.com"
        override val websiteUrl: String = "https://dev-tutuka.engageft.com"
    }, object : EngageKitConfig.EngageKitEnvironment() {
        override val serviceUrl: String = "https://app.engageft-007.com"
        override val websiteUrl: String = "https://account.thepaycard.co.za"
    }) {
        override val ipCheckUrl: String = "https://account.myengageft.com"
        override val refCode: String = "showcase"
        override val appPushParameter: String = "SHOWCASE"
        override val brand: String = "SHOWCASE"
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

    var isUsingProdEnvironment: Boolean
        set(value) {
            engageKitConfig.isUsingProdEnvironment = value
        }
        get() {
            return engageKitConfig.isUsingProdEnvironment
        }
}

