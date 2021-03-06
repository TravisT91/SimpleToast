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

            devEnvironment = object : EngageKitConfig.EngageKitEnvironment() {
                override val serviceUrl: String = "https://appdemo.engageft-008.com"
                override val websiteUrl: String = "https://test-pscu.engageft.com" },

            prodEnvironment = object : EngageKitConfig.EngageKitEnvironment() {
                override val serviceUrl: String = "https://app.engageft-008.com"
                override val websiteUrl: String = "https://staging-pscu.engageft.com" }) {

        override val ipCheckUrl: String = "https://api.ipify.org/"
        override val refCode: String = "PSCU.mydccu-android"
        override val appPushParameter: String = "MYCARDMANAGER"
        override val brand: String = "MYCARDMANAGER"
    }

    override val heapAppId: String = if (BuildConfig.DEBUG) "230876127" else "2688943769"
    override val moEngageAppId: String = "YTI7RS4PJ1G1IVAEUV7YX8DN"
    override val isTestBuild: Boolean
        get() = BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "demo"
}

abstract class BaseAppConfig {
    abstract val engageKitConfig: EngageKitConfig
    abstract val heapAppId: String
    abstract val moEngageAppId: String

    // Defaults:
    open val supportPhone: String = "18662392008"
    open val supportEmail: String = "service@myengageft.com"
    open val supportTechnicalPhone: String = "18662392008"
    open val currencyCode: String = "USD"

    open val isTestBuild = BuildConfig.DEBUG

    var isUsingProdEnvironment: Boolean
        set(value) {
            engageKitConfig.isUsingProdEnvironment = value
        }
        get() {
            return engageKitConfig.isUsingProdEnvironment
        }
}

