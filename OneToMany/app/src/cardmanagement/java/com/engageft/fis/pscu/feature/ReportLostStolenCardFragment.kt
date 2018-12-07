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
import com.engageft.fis.pscu.databinding.FragmentReportLostStolenCardBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles
import utilGen1.StringUtils

/**
 * ReportLostStolenCardFragment
 * </p>
 * This fragment allows the user to report a card as lost or stolen.
 * </p>
 * Created by Travis Tkachuk 11/28/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class ReportLostStolenCardFragment : BaseEngageFullscreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return  ViewModelProviders.of(this).get(ReportLostStolenCardViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentReportLostStolenCardBinding.inflate(inflater,container,false).apply {

            palette = Palette

            reportLostStolenCardViewModel =  (viewModel as? ReportLostStolenCardViewModel)?.apply {
                val successObserver = Observer<Boolean> {
                    if (it) showNewCardOrderedConfirmationDialog()
                }
                lostStolenReportedSuccess.observe(this@ReportLostStolenCardFragment, successObserver)
            }

            title.text = StringUtils.applyTypefaceAndColorToSubString(
                    Palette.primaryColor,
                    Palette.font_bold!!,
                    title.text.toString(),
                    getString(R.string.LOST_STOLEN_CARD_REPLACEMENT_HEADER_SUBSTRING))

            reportLostStolenButton.setOnClickListener {
                showReportLostStolenDialog()
            }
        }
        return binding.root
    }

    private fun showReportLostStolenDialog(){
        val dialog = InformationDialogFragment.newInstance(
                title = getString(R.string.LOST_STOLEN_CARD_REPORT_LOST_STOLEN),
                message = getString(R.string.LOST_STOLEN_CARD_REPLACEMENT_CONFIRMATION_MESSAGE),
                buttonPositiveText = getString(R.string.YES),
                buttonNegativeText = getString(R.string.NO),
                listener = object: InformationDialogFragment.InformationDialogFragmentListener{
                    override fun onDialogFragmentNegativeButtonClicked() {

                    }

                    override fun onDialogFragmentPositiveButtonClicked() {
                        (viewModel as? ReportLostStolenCardViewModel)?.onReportLostStolenClicked()
                    }

                    override fun onDialogCancelled() {

                    }

                }
        )
        dialog.applyPaletteStyles(context!!)
        showDialog(dialog)
    }

    private fun showNewCardOrderedConfirmationDialog(){
        val dialog = InformationDialogFragment.newInstance(
                title = getString(R.string.LOST_STOLEN_CARD_NEW_CARD_ORDERED),
                message = getString(R.string.LOST_STOLEN_CARD_NEW_CARD_ORDERED_MESSAGE),
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