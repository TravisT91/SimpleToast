package com.engageft.fis.pscu

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.EnrollmentViewModel
import com.engageft.fis.pscu.feature.branding.Palette

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
                EnrollmentViewModel.EnrollmentNavigations.TermsNavigations(R.id.action_termsOfUseFragment_to_sendingEnrollmentFragment),
                EnrollmentViewModel.EnrollmentNavigations.SendingNavigations(R.id.action_sendingEnrollmentFragment_to_enrollmentErrorFragment,
                                                                                R.id.action_sendingEnrollmentFragment_to_cardActiveFragment,
                                                                                R.id.action_sendingEnrollmentFragment_to_cardLinkedFragment),
                EnrollmentViewModel.EnrollmentNavigations.LinkedNavigations(R.id.action_cardLinkedFragment_to_authenticatedActivity2,
                                                                                R.id.action_cardLinkedFragment_to_notAuthenticatedActivity3),
                EnrollmentViewModel.EnrollmentNavigations.ActiveNavigations(R.id.action_cardActiveFragment_to_authenticatedActivity2,
                                                                                R.id.action_cardActiveFragment_to_notAuthenticatedActivity3))
    }

    // Variables for tracking keycode easter eggs:
    private var numVolumeUpPresses = 0
    private var numVolumeDownPresses = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!EngageAppConfig.isTestBuild) {
            return super.onKeyDown(keyCode, event)
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    numVolumeDownPresses = 0
                    numVolumeUpPresses++
                    if (numVolumeUpPresses > 2) {
                        numVolumeUpPresses = 0

                        Palette.useMockBranding = true
                        Toast.makeText(this, "Mock branding applied!", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    numVolumeUpPresses = 0
                    numVolumeDownPresses++
                    if (numVolumeDownPresses > 2) {
                        numVolumeDownPresses = 0

                        Palette.useMockBranding = false
                        Toast.makeText(this, "Mock branding removed!", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                else -> {
                    return false
                }
            }
        }
    }
}


