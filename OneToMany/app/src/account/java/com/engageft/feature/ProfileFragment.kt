package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.databinding.FragmentProfileBinding

/**
 * ChangeSecurityQuestionsFragment
 * <p>
 * Fragment for changing/setting a user's security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ProfileFragment : LotusFullScreenFragment() {
    private lateinit var profileViewModel: ProfileViewModel
    override fun createViewModel(): BaseViewModel? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return profileViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewModel = profileViewModel
        binding.palette = Palette

        profileViewModel.navigationObservable.observe(this, Observer { navigationEvent ->
            when (navigationEvent) {
            }
        })

        return binding.root
    }
}