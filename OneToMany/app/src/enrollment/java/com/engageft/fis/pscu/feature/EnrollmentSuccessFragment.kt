package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentEnrollmentSuccessBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * EnrollmentSuccessFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentSuccessFragment : BaseEngagePageFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var binding: FragmentEnrollmentSuccessBinding
    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnrollmentSuccessBinding.inflate(inflater, container, false)
        binding.viewModel = enrollmentViewModel
        binding.palette = Palette
//        binding.button1.setOnClickListener{
//            findNavController().navigate(R.id.action_enrollmentSuccessFragment_to_cardActiveFragment)
//        }
//        binding.button2.setOnClickListener{
//            findNavController().navigate(R.id.action_enrollmentSuccessFragment_to_cardLinkedFragment)
//        }

        enrollmentViewModel.successSubmissionObservable.observe(this, Observer {
            when (it) {
                EnrollmentViewModel.ActivationStatus.SUCCESS -> {

                }
                EnrollmentViewModel.ActivationStatus.FAIL -> {
                    binding.root.findNavController().navigate(R.id.action_sendingEnrollmentFragment_to_enrollmentErrorFragment)
                }
            }
        })
        return binding.root
    }
    var progress = 5

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var runnable: Runnable? = null
        runnable = Runnable {
            binding.progressBar.setProgress(progress)
            progress += 5
            if (progress < 100) {
                Handler().postDelayed(runnable, 500)
            }
        }
        Handler().postDelayed(runnable, 500)
    }
}