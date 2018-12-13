package com.engageft.fis.pscu

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig

/**
 * EnrollmentActivity
 * <p>
 * Activity encapsulating enrollment.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentActivity : LotusActivity() {
    private val enrollmentActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_enrollment
    }
    override fun getLotusActivityConfig(): LotusActivityConfig {
        return enrollmentActivityConfig
    }
}