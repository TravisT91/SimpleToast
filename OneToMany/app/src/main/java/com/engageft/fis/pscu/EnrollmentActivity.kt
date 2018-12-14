package com.engageft.fis.pscu

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.fis.pscu.feature.EnrollmentViewModel

/**
 * EnrollmentActivity
 * <p>
 * Activity encapsulating enrollment.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentActivity : LotusActivity() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private val enrollmentActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = 0
        override val navigationGraphResourceId = R.navigation.navigation_enrollment
    }
    override fun getLotusActivityConfig(): LotusActivityConfig {
        return enrollmentActivityConfig
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enrollmentViewModel = ViewModelProviders.of(this).get(EnrollmentViewModel::class.java)
        enrollmentViewModel.initEnrollmentNavigation(navController,
                EnrollmentViewModel.EnrollmentNavigations.GetStartedNavigations(R.id.action_getStartedFragment_to_enrollmentCardPinFragment,
                                                                                R.id.action_getStartedFragment_to_createAccountFragment,
                                                                                R.id.action_getStartedFragment_to_verifyIdentityFragment,
                                                                                R.id.action_getStartedFragment_to_termsOfUseFragment,
                                                                                R.id.action_getStartedFragment_to_sendingEnrollmentFragment),
                EnrollmentViewModel.EnrollmentNavigations.EnrollmentCardPinNavigations(R.id.action_enrollmentCardPinFragment_to_createAccountFragment,
                                                                                R.id.action_enrollmentCardPinFragment_to_verifyIdentityFragment,
                                                                                R.id.action_enrollmentCardPinFragment_to_termsOfUseFragment,
                                                                                R.id.action_enrollmentCardPinFragment_to_sendingEnrollmentFragment),
                EnrollmentViewModel.EnrollmentNavigations.CreateAccountNavigations(R.id.action_createAccountFragment_to_verifyIdentityFragment,
                                                                                R.id.action_createAccountFragment_to_termsOfUseFragment,
                                                                                R.id.action_createAccountFragment_to_sendingEnrollmentFragment),
                EnrollmentViewModel.EnrollmentNavigations.VerifyIdentityNavigations(R.id.action_verifyIdentityFragment_to_termsOfUseFragment,
                                                                                R.id.action_verifyIdentityFragment_to_sendingEnrollmentFragment),
                EnrollmentViewModel.EnrollmentNavigations.TermsNavigations(R.id.action_termsOfUseFragment_to_sendingEnrollmentFragment))
    }
}