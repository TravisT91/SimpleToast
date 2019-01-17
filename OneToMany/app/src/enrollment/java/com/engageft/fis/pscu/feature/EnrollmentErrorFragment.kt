package com.engageft.fis.pscu.feature

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentEnrollmentErrorBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * EnrollmentErrorFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentErrorFragment : BaseEngagePageFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var binding: FragmentEnrollmentErrorBinding

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            // at this point SendingEnrollmentFragment is popped from the stack.
            // so, go back to the last step the user completed
            binding.root.findNavController().popBackStack()
            return true
        }
    }

    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnrollmentErrorBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = enrollmentViewModel
            palette = Palette

            toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            callSupportButton.setOnClickListener {
                activity?.startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${EngageAppConfig.supportPhone}")
                })
            }

            tryAgainButton.setOnClickListener {
                // at this point SendingEnrollmentFragment is popped from the stack.
                // so, go back to the last step the user completed
                binding.root.findNavController().popBackStack()
            }
        }

        return binding.root
    }
}