package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.LotusFullScreenFragment



/**
 * SplashFragment
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/22/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashFragment : LotusFullScreenFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        val model = ViewModelProviders.of(this).get(SplashScreenViewModel::class.java)
        model.navigationObservable.observe(this, Observer { splashNavigationEvent : SplashScreenViewModel.SplashNavigationEvent ->
            val navDestinationId = when (splashNavigationEvent) {
                SplashScreenViewModel.SplashNavigationEvent.NOT_LOGGED_IN -> {
                    R.id.action_splash_fragment_to_login_fragment
                }
                SplashScreenViewModel.SplashNavigationEvent.LOGGED_IN -> {
                    R.id.action_splash_fragment_to_activityWithNavigation
                }
                SplashScreenViewModel.SplashNavigationEvent.FIRST_USE -> {
                    R.id.action_splash_fragment_to_get_started_fragment
                }
            }
            view.findNavController().navigate(navDestinationId)
        })

        return view
    }
}