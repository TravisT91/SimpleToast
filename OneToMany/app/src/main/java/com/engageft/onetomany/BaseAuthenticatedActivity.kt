package com.engageft.onetomany

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivity
import com.engageft.engagekit.EngageService
import com.engageft.feature.AuthExpiredPasswordDialog
import com.engageft.feature.EasterEggGestureDetector
import com.engageft.feature.EasterEggGestureListener
import com.engageft.onetomany.feature.enrollment.AuthenticationViewModel



/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/30/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseAuthenticatedActivity : LotusActivity() {
    private lateinit var authViewModel: AuthenticationViewModel
    private lateinit var gestureDetector: EasterEggGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProviders.of(this).get(AuthenticationViewModel::class.java)

        authViewModel.authNavigationObservable.observe(this, Observer { authNavigationEvent : AuthenticationViewModel.AuthNavigationEvent ->
            when(authNavigationEvent) {
                AuthenticationViewModel.AuthNavigationEvent.PROMPT_PASSWORD -> {
                    val dialog = AuthExpiredPasswordDialog()
                    dialog.show(supportFragmentManager, "TAG")
                }
                AuthenticationViewModel.AuthNavigationEvent.PROMPT_NONE -> {

                }
            }
        })

        // Add a DEBUG ONLY gesture detector to simular authentication expiration.
        gestureDetector = EasterEggGestureDetector(this, findViewById(R.id.activityContainer), object : EasterEggGestureListener {
            override fun onEasterEggActivated() {
            }
            override fun onEasterEggDeactivated() {
                // To simulate auth expiration.
                EngageService.getInstance().authManager.setAuthExpired()
            }
        })
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        authViewModel.onUserInteraction()
    }
}