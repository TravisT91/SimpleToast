package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentWelcomeSharedBinding

/**
 * Welcome4Fragment
 *
 * Welcome screen 4.
 *
 * Created by Atia Hashimi 11/6/2018.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class Welcome4Fragment: LotusFullScreenFragment() {

    lateinit var binding: FragmentWelcomeSharedBinding

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome_shared, container, false)

        binding.imageViewIcon.setImageResource(R.drawable.welcome4_icon_background)
        binding.titleTextView.text = getString(R.string.welcome_title4)
        binding.messageTextView.text = getString(R.string.welcome_message4)
        return binding.root
    }
}