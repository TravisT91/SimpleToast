package com.engageft.fis.pscu.feature.authentication

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivity
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.EasterEggGestureDetector
import com.engageft.fis.pscu.feature.EasterEggGestureListener


/**
 * BaseAuthenticatedActivity
 * <p>
 * Base Activity all authenticated activities should inherit from for this project. This activity
 * manages the job of observing authentication state and interrupting the navigation flow if/when the
 * authentication timer expires.
 * </p>
 * Created by joeyhutchins on 10/30/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseAuthenticatedActivity : LotusActivity() {
    private lateinit var authViewModel: AuthTimerViewModel
    private lateinit var gestureDetector: EasterEggGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProviders.of(this).get(AuthTimerViewModel::class.java)

        authViewModel.authNavigationObservable.observe(this, Observer { authNavigationEvent : AuthTimerViewModel.AuthNavigationEvent ->
            when(authNavigationEvent) {
                AuthTimerViewModel.AuthNavigationEvent.PROMPT_PASSWORD -> {
                    val dialog = AuthExpiredPasswordDialog()
                    dialog.show(supportFragmentManager, "TAG")
                }
                AuthTimerViewModel.AuthNavigationEvent.PROMPT_NONE -> {

                }
            }
        })

        // Add a DEBUG ONLY gesture detector to simular authentication expiration.
//        gestureDetector = EasterEggGestureDetector(this, findViewById(R.id.activityContainer), object : EasterEggGestureListener {
//            override fun onEasterEggActivated() {
//            }
//            override fun onEasterEggDeactivated() {
//                // To simulate auth expiration.
//                EngageService.getInstance().authManager.setAuthExpired()
//            }
//        })
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        authViewModel.onUserInteraction()
    }
}