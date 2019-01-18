package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.R
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
    var progress = 10

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return true
        }
    }

    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSendingEnrollmentBinding.inflate(inflater, container, false).apply {
            sendingEnrollmentDelegate = enrollmentViewModel
            palette = Palette

            toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)
            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)

            progressBar.setProgress(progress)

            var runnable: Runnable? = null
            runnable = Runnable {
                progressBar.setProgress(progress)
                // don't set to 100 yet
                if (progress < 90) {
                    Handler().postDelayed(runnable, PROGRESS_UPDATE_DELAY_TIME_MS)
                }
                progress += PROGRESS_VALUE_INCREMENT
            }
            Handler().postDelayed(runnable, PROGRESS_UPDATE_DELAY_TIME_MS)

            enrollmentViewModel.successSubmissionObservable.observe(viewLifecycleOwner, Observer {
                when (it) {
                    EnrollmentViewModel.ActivationStatus.SUCCESS -> {
                        //ensure progress is set to 100
                        progressBar.setProgress(PROGRESS_VALUE_COMPLETE)
                        sendingTextView.text = getString(R.string.ENROLLMENT_SUBMISSION_SUCCESS)
                        descriptionTextView.visibility = View.GONE
                    }
                    EnrollmentViewModel.ActivationStatus.FAIL -> {
                        navigateAfterDelay(R.id.action_sendingEnrollmentFragment_to_enrollmentErrorFragment)
                    }
                }
            })

            enrollmentViewModel.cardActivationStatusObservable.observe(viewLifecycleOwner, Observer {
                val id = when (it) {
                    EnrollmentViewModel.CardActivationStatus.ACTIVE -> R.id.action_sendingEnrollmentFragment_to_cardActiveFragment
                    EnrollmentViewModel.CardActivationStatus.LINKED -> R.id.action_sendingEnrollmentFragment_to_cardLinkedFragment
                    else -> -1
                }
                navigateAfterDelay(id)
            })
        }

        enrollmentViewModel.finalSubmit()

        return binding.root
    }

    private fun navigateAfterDelay(id: Int) {
        if (id != -1) {
            //let the user see the success screen for 1 second!
            Handler().postDelayed({
                binding.root.findNavController().navigate(id)
            }, NAV_DELAY_TIME_MS)
        }
    }

    private companion object {
        const val NAV_DELAY_TIME_MS = 1000L
        const val PROGRESS_UPDATE_DELAY_TIME_MS = 100L
        const val PROGRESS_VALUE_COMPLETE = 100
        const val PROGRESS_VALUE_INCREMENT = 20
    }
}