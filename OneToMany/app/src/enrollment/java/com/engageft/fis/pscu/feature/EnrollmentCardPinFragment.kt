package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentEnrollmentCardPinBinding

/**
 * EnrollmentCardPinFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentCardPinFragment : BaseEngageFullscreenFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var binding: FragmentEnrollmentCardPinBinding
    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnrollmentCardPinBinding.inflate(inflater, container, false)

        binding.button1.setOnClickListener{
            findNavController().navigate(R.id.action_enrollmentCardPinFragment_to_createAccountFragment)
        }
        binding.button2.setOnClickListener{
            findNavController().navigate(R.id.action_enrollmentCardPinFragment_to_verifyIdentityFragment)
        }
        binding.button3.setOnClickListener{
            findNavController().navigate(R.id.action_enrollmentCardPinFragment_to_termsOfUseFragment)
        }
        binding.button4.setOnClickListener{
            findNavController().navigate(R.id.action_enrollmentCardPinFragment_to_sendingEnrollmentFragment)
        }
        return binding.root
    }
}