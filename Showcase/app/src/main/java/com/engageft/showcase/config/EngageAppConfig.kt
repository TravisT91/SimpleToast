package com.engageft.showcase.config

import com.engageft.engagekit.config.EngageKitConfig
import com.engageft.showcase.BuildConfig

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/16/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object EngageAppConfig : BaseAppConfig() {
    override val engageKitConfig: EngageKitConfig = object : EngageKitConfig(object : EngageKitConfig.EngageKitEnvironment() {
        override val serviceUrl: String = "https://appdemo.onbudget.com"
        override val websiteUrl: String = "https://dev-millennial.engageft.com"
    }, object : EngageKitConfig.EngageKitEnvironment() {
        override val serviceUrl: String = "https://app.onbudget.com"
        override val websiteUrl: String = "https://account.myengageft.com"
    }) {
        override val ipCheckUrl: String = "https://api.ipify.org/"
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

