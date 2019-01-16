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
import com.engageft.fis.pscu.databinding.FragmentSendingEnrollmentBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * SendingEnrollmentFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SendingEnrollmentFragment : BaseEngagePageFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var binding: FragmentSendingEnrollmentBinding
    var progress = 5

    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSendingEnrollmentBinding.inflate(inflater, container, false)
        binding.viewModel = enrollmentViewModel
        binding.palette = Palette
//        binding.button1.setOnClickListener{
//            findNavController().navigate(R.id.action_enrollmentSuccessFragment_to_cardActiveFragment)
//        }
//        binding.button2.setOnClickListener{
//            findNavController().navigate(R.id.action_enrollmentSuccessFragment_to_cardLinkedFragment)
//        }
        var runnable: Runnable? = null
        runnable = Runnable {
            binding.progressBar.setProgress(progress)
            if (progress < 100) {
                Handler().postDelayed(runnable, 100)
            }
            progress += 25
        }
        Handler().postDelayed(runnable, 100)

        enrollmentViewModel.successSubmissionObservable.observe(this, Observer {
            when (it) {
                EnrollmentViewModel.ActivationStatus.SUCCESS -> {
                    binding.sendingTextView.text = "Success!"
                    binding.descriptionTextView.visibility = View.GONE
                }
                EnrollmentViewModel.ActivationStatus.FAIL -> {
                    binding.root.findNavController().navigate(R.id.action_sendingEnrollmentFragment_to_enrollmentErrorFragment)
                }
            }
        })

        enrollmentViewModel.cardActivationStatusObservable.observe(this, Observer {
//            var id = 0
            val id = when (it) {
                EnrollmentViewModel.CardActivationStatus.PENDING -> R.id.action_sendingEnrollmentFragment_to_cardActiveFragment
                EnrollmentViewModel.CardActivationStatus.LINKED -> R.id.action_sendingEnrollmentFragment_to_cardLinkedFragment
            }
            //let the user see the success screen for 2 seconds!
            Handler().postDelayed({
                binding.root.findNavController().navigate(id)
            }, 200)
        })
        enrollmentViewModel.submitAcceptTerms()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}