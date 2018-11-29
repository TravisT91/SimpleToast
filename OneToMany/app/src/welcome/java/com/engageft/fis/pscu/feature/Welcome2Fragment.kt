package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentWelcomeSharedBinding

/**
 * Welcome2Fragment
 *
 * Welcome screen 2.
 *
 * Created by Atia Hashimi 11/6/2018.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class Welcome2Fragment: LotusFullScreenFragment() {

    lateinit var binding: FragmentWelcomeSharedBinding

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome_shared, container, false)

        binding.iconView.findViewById<AppCompatImageView>(R.id.imageViewIcon).apply {
            setImageResource(R.drawable.ic_welcome2)
        }
        binding.titleTextView.text = getString(R.string.welcome_title2)
        binding.messageTextView.text = getString(R.string.welcome_message2)
        return binding.root
    }
}