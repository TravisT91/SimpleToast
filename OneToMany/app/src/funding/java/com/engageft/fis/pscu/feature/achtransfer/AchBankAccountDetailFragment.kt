package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountDetailBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.ACH_BANK_ACCOUNT_ID_KEY
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.newInfoDialogInstance

/**
 * AchBankAccountDetailFragment
 * </p>
 * Fragment for displaying ACH bank account and deleting it. Also allows user to verify the bank account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountDetailFragment: BaseEngagePageFragment() {

    private lateinit var achBankAccountDetailViewModel: AchBankAccountDetailViewModel

    override fun createViewModel(): BaseViewModel? {
        achBankAccountDetailViewModel = ViewModelProviders.of(this).get(AchBankAccountDetailViewModel::class.java)
        return achBankAccountDetailViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAchBankAccountDetailBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = achBankAccountDetailViewModel
            palette = Palette

            arguments?.let {
                achBankAccountDetailViewModel.achAccountInfoId = it.getLong(ACH_BANK_ACCOUNT_ID_KEY, 0)
            } ?: run {
                throw IllegalStateException("must pass arguments")
            }

            deleteButtonLayout.setOnClickListener {
                // prompt user if they want to delete
                val bankNameAndNumber = String.format(getString(R.string.TRANSFER_ACCOUNT_DESCRIPTION_FORMAT),
                        achBankAccountDetailViewModel.achAccountInfo!!.bankName, achBankAccountDetailViewModel.achAccountInfo!!.accountLastDigits)

                val deleteDialogInfo = newInfoDialogInstance(context!!,
                        title = getString(R.string.ach_bank_account_delete_confirmation_title),
                        message = String.format(getString(R.string.ach_bank_account_delete_confirmation_message_format), bankNameAndNumber),
                        buttonPositiveText = getString(R.string.ach_bank_account_delete),
                        buttonNegativeText = getString(R.string.dialog_information_cancel_button),
                        listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                            override fun onDialogFragmentNegativeButtonClicked() {
                            }

                            override fun onDialogFragmentPositiveButtonClicked() {
                                achBankAccountDetailViewModel.onDeleteAccount()
                            }

                            override fun onDialogCancelled() {
                            }

                        })
                deleteDialogInfo.positiveButtonTextColor = Palette.errorColor
                fragmentDelegate.showDialog(deleteDialogInfo)
            }

            verifyAccountButton.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_achBankAccountDetailFragment_to_achBankAccountVerifyFragment,
                        bundleOf(ACH_BANK_ACCOUNT_ID_KEY to achBankAccountDetailViewModel.achAccountInfoId))
            }
        }

        achBankAccountDetailViewModel.apply {

            checkingAccountTypeObservable.observe(viewLifecycleOwner, Observer {
                if (it) {
                    binding.accountTypeTextWithLabel.inputText = getString(R.string.TEXT_CHECKING)
                } else {
                    binding.accountTypeTextWithLabel.inputText = getString(R.string.TEXT_SAVINGS)
                }
            })

            bankDeleteSuccessObservable.observe(viewLifecycleOwner, Observer {
                binding.root.findNavController().popBackStack()
            })
        }

        return binding.root
    }
}