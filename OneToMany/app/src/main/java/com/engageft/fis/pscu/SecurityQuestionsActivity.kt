package com.engageft.fis.pscu

import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig

/**
 * SecurityQuestionsActivity
 * <p>
 * Activity wrapping the mandatory action that a user must enter security questions so they can
 * recover their account.
 * </p>
 * Created by joeyhutchins on 12/17/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SecurityQuestionsActivity : LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_security_questions
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }
}