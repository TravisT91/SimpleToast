package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentSplashBinding


/**
 * SplashFragment
 * <p>
 * First fragment in the navigation of the application. Here, we determine where to navigate the user
 * after initialization.
 * </p>
 * Created by joeyhutchins on 8/22/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashFragment : BaseEngagePageFragment() {
    lateinit var binding: FragmentSplashBinding

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(SplashScreenViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)

        (fragmentDelegate.viewModel as SplashScreenViewModel).navigationObservable.observe(this, Observer { splashNavigationEvent : SplashScreenViewModel.SplashNavigationEvent ->
            val destinationId = when (splashNavigationEvent) {
                SplashScreenViewModel.SplashNavigationEvent.FIRST_TIME -> {
                    R.id.action_splashFragment_to_welcomeActivity
                }
                SplashScreenViewModel.SplashNavigationEvent.NOT_LOGGED_IN -> {
                    R.id.action_splashFragment_to_notAuthenticatedActivity
                }
                SplashScreenViewModel.SplashNavigationEvent.LOGGED_IN -> {
                    R.id.action_splashFragment_to_authenticatedActivity
                }
            }
            binding.root.findNavController().navigate(destinationId)
            activity!!.finish()
        })

        return binding.root
    }
}