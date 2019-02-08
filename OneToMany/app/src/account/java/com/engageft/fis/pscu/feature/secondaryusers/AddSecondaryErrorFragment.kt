package com.engageft.fis.pscu.feature.secondaryusers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentAddSecondaryErrorBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * Created by joeyhutchins on 2/7/19.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AddSecondaryErrorFragment : BaseEngagePageFragment() {
    private lateinit var binding: FragmentAddSecondaryErrorBinding

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            binding.root.findNavController().popBackStack(R.id.secondaryUserListFragment, false)
            return true
        }
    }

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddSecondaryErrorBinding.inflate(inflater, container, false)
        binding.apply {
            palette = Palette

            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)
            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)

            callSupportButton.setOnClickListener {
                activity?.startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${EngageAppConfig.supportPhone}")
                })
            }

            tryAgainButton.setOnClickListener {
                binding.root.findNavController().popBackStack()
            }
        }

        return binding.root
    }
}