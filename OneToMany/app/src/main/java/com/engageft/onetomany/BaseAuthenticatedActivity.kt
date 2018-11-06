package com.engageft.onetomany

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProviders.of(this).get(AuthenticationViewModel::class.java)

        authViewModel.authNavigationObservable.observe(this, Observer { splashNavigationEvent : AuthenticationViewModel.AuthNavigationEvent ->
            Log.e("Joey", "Activity: event!")
        })
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        authViewModel.onUserInteraction()
    }
}