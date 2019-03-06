package com.engageft.fis.pscu.feature.secondaryusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAddSecondarySuccessBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.setPalette

/**
 * Created by joeyhutchins on 2/7/19.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AddSecondarySuccessFragment : BaseEngagePageFragment() {
    private lateinit var binding: FragmentAddSecondarySuccessBinding

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            binding.root.findNavController().popBackStack()
            return true
        }
    }

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddSecondarySuccessBinding.inflate(inflater, container, false)
        binding.apply {
            palette = Palette

            toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)
            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
            val icon = imageViewLayout.findViewById<AppCompatImageView>(R.id.imageViewIcon)
            icon.setPalette(true)

            doneButton.setOnClickListener {
                binding.root.findNavController().popBackStack()
            }
        }

        return binding.root
    }
}