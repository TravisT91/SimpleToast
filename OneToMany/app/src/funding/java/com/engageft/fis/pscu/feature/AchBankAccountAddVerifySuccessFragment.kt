package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountAddVerifySuccessBinding
import com.engageft.fis.pscu.feature.AchBankAccountAddFragment.Companion.ADDED_SUCCESS_TYPE
import com.engageft.fis.pscu.feature.AchBankAccountAddFragment.Companion.SUCCESS_SCREEN_TYPE_KEY
import com.engageft.fis.pscu.feature.AchBankAccountAddFragment.Companion.VERIFIED_SUCCESS_TYPE
import com.engageft.fis.pscu.feature.branding.Palette
/**
 * AchBankAccountAddVerifySuccessFragment
 * </p>
 * Fragment for displaying success status of Adding or Verifying an ACH bank account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountAddVerifySuccessFragment: BaseEngageFullscreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    private lateinit var binding: FragmentAchBankAccountAddVerifySuccessBinding

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            binding.root.findNavController().popBackStack()
            return true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAchBankAccountAddVerifySuccessBinding.inflate(inflater, container, false)

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)
        toolbarController.setToolbarVisibility(ToolbarVisibilityState.GONE)
        binding.apply {
            palette = Palette

            arguments?.let { bundle ->
                val successType = bundle.getInt(SUCCESS_SCREEN_TYPE_KEY, -1)
                when (successType) {
                    ADDED_SUCCESS_TYPE -> {
                        titleTextView.text = getString(R.string.ach_bank_account_added_successful_title)
                        subTitleTextView.text = getString(R.string.ach_bank_account_added_successful_subtitle)
                        nextButton.text = getString(R.string.ach_bank_account_added_successful_done_button)
                    }
                    VERIFIED_SUCCESS_TYPE -> {
                        titleTextView.text = getString(R.string.ach_bank_account_verified_successful_title)
                        subTitleTextView.text = getString(R.string.ach_bank_account_verified_successful_subtitle)
                        nextButton.text = getString(R.string.ach_bank_account_verified_successful_done_button)
                    }
                    else -> {
                        showGenericSuccessDialogMessageAndPopBackstack(binding.root)
                    }
                }
                nextButton.setOnClickListener {
                    binding.root.findNavController().popBackStack()
                }
            } ?: run {
                showGenericSuccessDialogMessageAndPopBackstack(binding.root)
            }
        }
        return binding.root
    }
}