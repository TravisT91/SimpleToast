package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentAchBankAddVerifySuccessBinding
import com.engageft.fis.pscu.feature.AchBankAccountFragment.Companion.ADDED_SUCCESS_TYPE
import com.engageft.fis.pscu.feature.AchBankAccountFragment.Companion.SUCCESS_SCREEN_TYPE_KEY
import com.engageft.fis.pscu.feature.AchBankAccountFragment.Companion.VERIFIED_SUCCESS_TYPE
import com.engageft.fis.pscu.feature.branding.Palette

class AchBankAddVerifySuccessFragment: BaseEngageFullscreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAchBankAddVerifySuccessBinding.inflate(inflater, container, false)
        binding.apply {
            palette = Palette

            arguments?.let { bundle ->
                val successType = bundle.getInt(SUCCESS_SCREEN_TYPE_KEY, -1)
                when (successType) {
                    ADDED_SUCCESS_TYPE -> {

                    }
                    VERIFIED_SUCCESS_TYPE -> {

                    }
                    else -> {
                        showGenericSuccessDialogMessageAndPopBackstack(binding.root)
                    }
                }
            } ?: run {
                showGenericSuccessDialogMessageAndPopBackstack(binding.root)
            }
        }
        return binding.root
    }
}