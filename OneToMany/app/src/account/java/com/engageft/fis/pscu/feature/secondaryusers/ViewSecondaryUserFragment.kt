package com.engageft.fis.pscu.feature.secondaryusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentViewSecondaryUserBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.utils.CardStatusUtils
import kotlinx.android.synthetic.main.fragment_view_secondary_user.cardStatusText

/**
 * Created by joeyhutchins on 2/26/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class ViewSecondaryUserFragment : BaseEngagePageFragment() {
    companion object {
        const val KEY_SECONDARY_USER_ID = "KEY_SECONDARY_USER_ID"
        const val KEY_SECONDARY_CARD_ID = "KEY_SECONDARY_CARD_ID"
    }

    private lateinit var viewSecondaryViewModel: ViewSecondaryUserViewModel
    private lateinit var binding: FragmentViewSecondaryUserBinding

    override fun createViewModel(): BaseViewModel? {
        viewSecondaryViewModel = ViewModelProviders.of(this).get(ViewSecondaryUserViewModel::class.java)
        return viewSecondaryViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentViewSecondaryUserBinding.inflate(inflater, container, false)

        viewSecondaryViewModel.titleObservable.observe(viewLifecycleOwner, Observer {
            toolbarController.setToolbarTitle(it)
        })

        binding.apply {
            viewModel = viewSecondaryViewModel
            palette = Palette

            arguments?.let {
                viewSecondaryViewModel.userId = it.getLong(KEY_SECONDARY_USER_ID, ViewSecondaryUserViewModel.INVALID_ID)
                viewSecondaryViewModel.debitCardId = it.getLong(KEY_SECONDARY_CARD_ID, ViewSecondaryUserViewModel.INVALID_ID)
            } ?: run {
                throw IllegalStateException("must pass arguments")
            }

            firstNameText.setEnable(false)
            lastNameText.setEnable(false)
            phoneNumberText.setEnable(false)
            dobText.setEnable(false)
            cardStatusText.setEnable(false)

            viewSecondaryViewModel.fetchUser()
        }

        viewSecondaryViewModel.cardStatusObservable.observe(viewLifecycleOwner, Observer { cardStatus ->
            cardStatusText.inputText = CardStatusUtils.cardStatusStringForProductCardModelCardStatus(context!!, cardStatus)
        })
        return binding.root
    }
}