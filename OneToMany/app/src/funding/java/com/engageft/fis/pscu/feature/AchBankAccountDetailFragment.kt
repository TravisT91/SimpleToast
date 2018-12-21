package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountDetailBinding
import com.engageft.fis.pscu.feature.AccountsAndTransfersListFragment.Companion.ACH_BANK_ACCOUNT_ID
import com.engageft.fis.pscu.feature.branding.Palette
/**
 * AchBankAccountDetailFragment
 * </p>
 * Fragment for displaying ACH bank account and deleting it. Also allows user to verify the bank account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountDetailFragment: BaseEngageFullscreenFragment() {

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
                achBankAccountDetailViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, 0)
            } ?: run {
                showGenericSuccessDialogMessageAndPopBackstack(root)
            }

            deleteButtonLayout.setOnClickListener {
                // prompt user if they want to delete
                val bankNameAndNumber = String.format(getString(R.string.TRANSFER_ACCOUNT_DESCRIPTION_FORMAT),
                        achBankAccountDetailViewModel.achAccountInfo!!.bankName, achBankAccountDetailViewModel.achAccountInfo!!.accountLastDigits)

                val deleteDialogInfo = InformationDialogFragment.newLotusInstance(
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
                showDialog(deleteDialogInfo)
            }

            verifyAccountButton.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_achBankAccountDetailFragment_to_achBankAccountVerifyFragment,
                        Bundle().apply {
                            putLong(ACH_BANK_ACCOUNT_ID, achBankAccountDetailViewModel.achAccountInfoId)
                        })
            }
        }

        achBankAccountDetailViewModel.apply {

            checkingAccountTypeObservable.observe(this@AchBankAccountDetailFragment, Observer {
                if (it) {
                    binding.accountTypeTextWithLabel.inputText = getString(R.string.TEXT_CHECKING)
                } else {
                    binding.accountTypeTextWithLabel.inputText = getString(R.string.TEXT_SAVINGS)
                }
            })

            navigationEventObservable.observe(this@AchBankAccountDetailFragment, Observer {
                when (it) {
                    AchBankAccountNavigationEvent.DELETED_BANK_SUCCESS -> {
                        binding.root.findNavController().popBackStack()
                    }
                }
            })
        }

        return binding.root
    }
}