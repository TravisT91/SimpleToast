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
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
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
    private lateinit var sendingDelegate: SendingEnrollmentDelegate
    private lateinit var binding: FragmentSendingEnrollmentBinding
    var progress = 10

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            fragmentDelegate.showDialog(dialogPromptToStay())
            return true
        }
    }

    override fun createViewModel(): BaseViewModel? {
        val enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        sendingDelegate = enrollmentViewModel.sendingDelegate
        return enrollmentViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSendingEnrollmentBinding.inflate(inflater, container, false).apply {
            sendingEnrollmentDelegate = sendingDelegate
            palette = Palette

            toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            progressBar.setProgress(progress)

            var runnable: Runnable? = null
            runnable = Runnable {
                progressBar.setProgress(progress)
                // don't set to 100 yet
                if (progress < 90) {
                    Handler().postDelayed(runnable, 100)
                }
                progress += 20
            }
            Handler().postDelayed(runnable, 100)

            sendingDelegate.successSubmissionObservable.observe(this@SendingEnrollmentFragment, Observer {
                when (it) {
                    SendingEnrollmentDelegate.ActivationStatus.SUCCESS -> {
                        //ensure progress is set to 100
                        progressBar.setProgress(100)
                        sendingTextView.text = getString(R.string.ENROLLMENT_SUBMISSION_SUCCESS)
                        descriptionTextView.visibility = View.GONE
                    }
                    SendingEnrollmentDelegate.ActivationStatus.FAIL -> {
                        navigateAfterDelay(R.id.action_sendingEnrollmentFragment_to_enrollmentErrorFragment)
                    }
                }
            })

            sendingDelegate.cardActivationStatusObservable.observe(this@SendingEnrollmentFragment, Observer {
                val id = when (it) {
                    SendingEnrollmentDelegate.CardActivationStatus.ACTIVE -> R.id.action_sendingEnrollmentFragment_to_cardActiveFragment
                    SendingEnrollmentDelegate.CardActivationStatus.LINKED -> R.id.action_sendingEnrollmentFragment_to_cardLinkedFragment
                    else -> -1
                }
                navigateAfterDelay(id)
            })
        }

        sendingDelegate.submitAcceptTerms()

        return binding.root
    }

    private fun navigateAfterDelay(id: Int) {
        if (id != -1) {
            //let the user see the success screen for 2 seconds!
            Handler().postDelayed({
                binding.root.findNavController().navigate(id)
            }, 2000)
        }
    }

    private fun dialogPromptToStay(): InformationDialogFragment {
        return InformationDialogFragment.newLotusInstance(title = getString(R.string.ENROLLMENT_SUBMISSION_PROMPT_TITLE),
                message = getString(R.string.ENROLLMENT_SUBMISSION_PROMPT_MESSAGE),
                buttonPositiveText = getString(R.string.dialog_information_ok_button),
                buttonNegativeText = getString(R.string.ENROLLMENT_SUBMISSION_PROMPT_EXIT),
                listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                    override fun onDialogFragmentNegativeButtonClicked() {
                        binding.root.findNavController().popBackStack()
                    }

                    override fun onDialogFragmentPositiveButtonClicked() {}

                    override fun onDialogCancelled() {}

                })
    }
}