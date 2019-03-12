package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentTermsOfUseBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * TermsOfUseFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class TermsOfUseFragment : BaseEngagePageFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var binding: FragmentTermsOfUseBinding
    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTermsOfUseBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = enrollmentViewModel.termsOfUseDelegate
            palette = Palette

            declineTermsButton.setOnClickListener {
                fragmentDelegate.showDialog(infoDialogYesNoNewInstance(context!!,
                        title = getString(R.string.ENROLLMENT_TERMS_OF_USE_ALERT_TITLE),
                        message = getString(R.string.ENROLLMENT_TERMS_OF_USE_ALERT_MESSAGE),
                        listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                            override fun onDialogFragmentNegativeButtonClicked() {}

                            override fun onDialogFragmentPositiveButtonClicked() {
                                binding.root.findNavController().popBackStack(R.id.getStartedFragment, false)
                            }

                            override fun onDialogCancelled() {}

                        }))
            }
        }
        return binding.root
    }
}