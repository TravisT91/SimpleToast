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
import com.engageft.fis.pscu.databinding.FragmentCancelCardBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles
import utilGen1.StringUtils

/**
 * CancelCardFragment
 * </p>
 * This fragment allows the user to cancel their card.
 * </p>
 * Created by Travis Tkachuk 11/28/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class CancelCardFragment : BaseEngageFullscreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return  ViewModelProviders.of(this).get(CancelCardViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCancelCardBinding.inflate(inflater,container,false).apply {

            palette = Palette

            cancelCardViewModel =  (createViewModel() as? CancelCardViewModel)?.apply {
                val successObserver = Observer<Boolean> {
                    if (it) showCardCanceledConfirmationDialog()
                }
                cardCanceledSuccess.observe(this@CancelCardFragment, successObserver)
            }

            title.text = StringUtils.applyTypefaceAndColorToSubString(
                    Palette.primaryColor,
                    Palette.font_bold!!,
                    title.text.toString(),
                    getString(R.string.CANCEL_CARD_REPLACEMENT_HEADER_SUBSTRING))

            reportLostStolenButton.setOnClickListener {
                showCancelDialog()
            }
        }
        return binding.root
    }

    private fun showCancelDialog(){
        val dialog = InformationDialogFragment.newInstance(
                title = getString(R.string.CANCEL_CARD_CONFIRMATION_TITLE),
                message = getString(R.string.CANCEL_CARD_CONFIRMATION_MESSAGE),
                buttonPositiveText = getString(R.string.YES),
                buttonNegativeText = getString(R.string.NO),
                listener = object: InformationDialogFragment.InformationDialogFragmentListener{
                    override fun onDialogFragmentNegativeButtonClicked() {

                    }

                    override fun onDialogFragmentPositiveButtonClicked() {
                        (viewModel as? CancelCardViewModel)?.onCancelClicked()
                    }

                    override fun onDialogCancelled() {

                    }

                }
        )
        dialog.applyPaletteStyles(context!!)
        showDialog(dialog)
    }

    private fun showCardCanceledConfirmationDialog(){
        val dialog = InformationDialogFragment.newInstance(
                title = getString(R.string.CANCEL_CARD_NEW_CARD_ORDERED_TITLE),
                message = getString(R.string.CANCEL_CARD_NEW_CARD_ORDERED_MESSAGE),
                buttonPositiveText = getString(R.string.OK),
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
        showDialog(dialog)
    }
}