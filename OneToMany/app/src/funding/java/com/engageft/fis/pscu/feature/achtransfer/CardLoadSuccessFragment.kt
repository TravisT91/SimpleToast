package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardLoadSuccessBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.SUCCESS_SCREEN_TYPE_KEY
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * CardLoadSuccessFragment
 * </p>
 * Success fragment for adding a debit/credit card, ACH bank account or verifying an ACH bank successfully.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardLoadSuccessFragment: BaseEngagePageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    private lateinit var binding: FragmentCardLoadSuccessBinding

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            binding.root.findNavController().popBackStack()
            return true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardLoadSuccessBinding.inflate(inflater, container, false)

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)
        //TODO(aHashimi): user can still click UP button, this's a bug. https://engageft.atlassian.net/browse/FOTM-707
        toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)

        binding.apply {
            palette = Palette

            val imageView = imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon)
            arguments?.let { bundle ->
                val successType = bundle.getSerializable(SUCCESS_SCREEN_TYPE_KEY) as? SuccessType
                when (successType) {
                    SuccessType.ADD_CARD -> {
                        imageView.setImageResource(R.drawable.ic_add_card)
                        titleTextView.text = getString(R.string.card_load_add_confirmation_header)
                        subTitleTextView.text = getString(R.string.card_load_add_confirmation_subHeader)
                    }
                    SuccessType.ADD_ACH_ACCOUNT -> {
                        imageView.setImageResource(R.drawable.ic_add_ach_bank)
                        titleTextView.text = getString(R.string.ach_bank_account_added_successful_title)
                        subTitleTextView.text = getString(R.string.ach_bank_account_added_successful_subtitle)
                    }
                    SuccessType.VERIFIED_ACH_ACCOUNT -> {
                        imageView.setImageResource(R.drawable.ic_bank_account_verified)
                        titleTextView.text = getString(R.string.ach_bank_account_verified_successful_title)
                        subTitleTextView.text = getString(R.string.ach_bank_account_verified_successful_subtitle)
                    }
                    else -> {
                        throw IllegalStateException("Unknown success type")
                    }
                }
                nextButton.setOnClickListener {
                    binding.root.findNavController().popBackStack()
                }
            } ?: run {
                throw IllegalStateException("Arguments required")
            }
        }
        return binding.root
    }
}
enum class SuccessType {
    ADD_CARD,
    ADD_ACH_ACCOUNT,
    VERIFIED_ACH_ACCOUNT
}