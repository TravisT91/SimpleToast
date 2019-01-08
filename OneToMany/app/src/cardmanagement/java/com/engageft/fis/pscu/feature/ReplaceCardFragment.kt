package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentReplaceCardBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles
import utilGen1.StringUtils

/**
 * ReplaceCardFragment
 * </p>
 * This baseFragmentIm allows the user to order a replacement card.
 * </p>
 * Created by Travis Tkachuk 11/28/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class ReplaceCardFragment : BaseEngagePageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return  ViewModelProviders.of(this).get(ReplaceCardViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentReplaceCardBinding.inflate(inflater,container,false).apply {

            palette = Palette

            replaceCardViewModel =  (fragmentDelegate.viewModel as? ReplaceCardViewModel)?.apply {
                val successObserver = Observer<Boolean> { if (it) showSuccessDialog() }
                replacementRequestIsSuccess.observe(this@ReplaceCardFragment, successObserver)
            }

            title.text = StringUtils.applyTypefaceAndColorToSubString(
                    Palette.primaryColor,
                    Palette.font_bold!!,
                    title.text.toString(),
                    getString(R.string.REPLACE_CARD_REPLACEMENT_SUBSTRING))

            doNotKnowText.text = StringUtils.applyTypefaceToSubstring(
                    Palette.font_bold,
                    doNotKnowText.text.toString(),
                    getString(R.string.REPLACE_CARD_DO_NOT_KNOW_SUBSTRING))


        }
        return binding.root
    }

    private fun showSuccessDialog(){
        val dialog = InformationDialogFragment.newInstance(
                title = getString(R.string.REPLACE_CARD_REPLACEMENT_ORDERED_TITLE),
                message = getString(R.string.REPLACE_CARD_REPLACEMENT_ORDERED_MESSAGE),
                buttonPositiveText = getString(android.R.string.ok),
                listener = object: InformationDialogFragment.InformationDialogFragmentListener{
                    override fun onDialogFragmentNegativeButtonClicked() {

                    }

                    override fun onDialogFragmentPositiveButtonClicked() {
                        view?.findNavController()?.popBackStack()
                    }

                    override fun onDialogCancelled() {

                    }

                }
        )
        dialog.applyPaletteStyles(context!!)
        fragmentDelegate.showDialog(dialog)
    }
}