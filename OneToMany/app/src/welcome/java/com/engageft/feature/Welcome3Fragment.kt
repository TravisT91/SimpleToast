package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.Welcome2FragmentBinding
/**
 * Welcome3Fragment
 *
 * Welcome screen 3.
 *
 * Created by Atia Hashimi 11/6/2018.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class Welcome3Fragment: LotusFullScreenFragment() {

    lateinit var binding: Welcome2FragmentBinding

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.welcome2_fragment, container, false)

        binding.imageViewIcon.background = ContextCompat.getDrawable(context!!, R.drawable.welcome3_icon_background)
        binding.titleTextView.text = getString(R.string.welcome_title3)
        binding.messageTextView.text = getString(R.string.welcome_message3)
        return binding.root
    }
}