package com.engageft.showcase.config

import com.engageft.engagekit.config.EngageKitConfig

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/16/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object EngageAppConfig : BaseAppConfig() {
    override val engageKitConfig: EngageKitConfig = object : EngageKitConfig() {
        override val environment: Environment = Environment.DEV
        override val serviceUrl: String = if (environment == Environment.DEV) "https://appdemo.onbudget.com" else "https://app.onbudget.com"
        override val websiteUrl: String = if (environment == Environment.DEV) "https://dev-millennial.engageft.com" else "https://account.myengageft.com"
        override val ipCheckUrl: String = if (environment == Environment.DEV) "https://api.ipify.org/" else "https://api.ipify.org/"
        override val refCode: String = "showcase"
        override val appPushParameter: String = "SHOWCASE"
    }

    override val environment: EngageKitConfig.Environment = engageKitConfig.environment

    override val heapAppId: String = if (environment == EngageKitConfig.Environment.DEV) "" else ""
}

abstract class BaseAppConfig {
    abstract val environment: EngageKitConfig.Environment
    abstract val engageKitConfig: EngageKitConfig
    abstract val heapAppId: String

    // Defaults:
    open val supportPhone: String = "18662392008"
    open val supportEmail: String = "service@myengageft.com"
    open val supportTechnicalPhone: String = "18662392008"
}

