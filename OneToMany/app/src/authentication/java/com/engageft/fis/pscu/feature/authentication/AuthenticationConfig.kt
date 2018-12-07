package com.engageft.fis.pscu.feature.authentication

/**
 * AuthenticationConfig
 * <p>
 * Configuration options for the Authentication feature.
 * </p>
 * Created by joeyhutchins on 10/18/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object AuthenticationConfig {
    const val requireEmailConfirmation: Boolean = true

    // TODO(aHashimi): cases such as when the app doesn't allow demo either in production or demo
    const val demoAccountAvailable: Boolean = true

    // TODO(aHashimi): Maybe need to set this to false once in production
    // this is when demo account creation is forbidden in production
    const val shouldAllowDemoAccountCreationInProd: Boolean = true
}