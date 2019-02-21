package com.engageft.fis.pscu.feature.onboarding

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.SubFragmentOnboardingItemBinding
import com.engageft.fis.pscu.feature.BaseEngageSubFragment

/**
 * Created by joeyhutchins on 2/13/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class OnboardingItemFragment : BaseEngageSubFragment() {
    var icon: Drawable? = null
    lateinit var title: CharSequence
    lateinit var message: CharSequence

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<SubFragmentOnboardingItemBinding>(layoutInflater, R.layout.sub_fragment_onboarding_item, container, false)

        binding.icon.setImageDrawable(icon)
        binding.titleTextView.text = title
        binding.messageTextView.text = message

        return binding.root
    }
}