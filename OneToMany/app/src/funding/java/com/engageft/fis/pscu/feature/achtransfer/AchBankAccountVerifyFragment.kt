package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceToSubstringsList
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountVerifyBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.SUCCESS_SCREEN_TYPE_KEY
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * AchBankAccountVerifyFragment
 * </p>
 * Fragment for verifying an ACH bank account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountVerifyFragment: BaseEngagePageFragment() {
    private lateinit var verifyAchBankAccountViewModel: AchBankAccountVerifyViewModel

    override fun createViewModel(): BaseViewModel? {
        verifyAchBankAccountViewModel = ViewModelProviders.of(this).get(AchBankAccountVerifyViewModel::class.java)
        return verifyAchBankAccountViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAchBankAccountVerifyBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = verifyAchBankAccountViewModel
            palette = Palette

            //TODO(Hashimi) https://engageft.atlassian.net/browse/SHOW-459 set maxLength of input fields to 2
            amountInputWithLabel1.currencyCode = EngageAppConfig.currencyCode
            amountInputWithLabel2.currencyCode = EngageAppConfig.currencyCode

            instructionsTextView.text = getString(R.string.ach_bank_account_verify_instructions).applyTypefaceToSubstringsList(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    listOf(getString(R.string.ach_bank_account_verify_instructions_substring1),
                            getString(R.string.ach_bank_account_verify_instructions_substring2)))

            amountInputWithLabel1.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    verifyAchBankAccountViewModel.validateNonEmptyAmount1AndShowError()
                }
            })

            amountInputWithLabel2.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    verifyAchBankAccountViewModel.validateNonEmptyAmount2AndShowError()
                }
            })
        }

        arguments?.let {
            verifyAchBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, 0)
        }

        verifyAchBankAccountViewModel.apply {
            amount1ShowErrorObservable.observe(viewLifecycleOwner, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel1.isErrorEnabled) {
                        binding.amountInputWithLabel1.setErrorTexts(listOf(getString(R.string.ach_bank_account_verify_amount_error_message)))
                    }
                } else {
                    binding.amountInputWithLabel1.setErrorTexts(null)
                }
            })

            amount2ShowErrorObservable.observe(viewLifecycleOwner, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel2.isErrorEnabled) {
                        binding.amountInputWithLabel2.setErrorTexts(listOf(getString(R.string.ach_bank_account_verify_amount_error_message)))
                    }
                } else {
                    binding.amountInputWithLabel2.setErrorTexts(null)
                }
            })

            buttonStateObservable.observe(viewLifecycleOwner, Observer {
                when (it) {
                    AchBankAccountVerifyViewModel.ButtonState.SHOW -> {
                        binding.submitButton.visibility = View.VISIBLE
                    }
                    AchBankAccountVerifyViewModel.ButtonState.HIDE -> {
                        binding.submitButton.visibility = View.GONE
                    }
                }
                activity?.invalidateOptionsMenu()
            })

            navigationEventObservable.observe(viewLifecycleOwner, Observer {
                if (it == AchBankAccountNavigationEvent.BANK_VERIFIED_SUCCESS) {
                    binding.root.findNavController().navigate(R.id.action_achBankAccountVerifyFragment_to_achBankAccountAddVerifySuccessFragment,
                            bundleOf(SUCCESS_SCREEN_TYPE_KEY to SuccessType.VERIFIED_ACH_ACCOUNT))
                }
            })
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_ach_bank_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val submitMenuItem = menu!!.findItem(R.id.submit)
        submitMenuItem.title = getString(R.string.ach_bank_account_verify_button)
        submitMenuItem.isVisible = verifyAchBankAccountViewModel.buttonStateObservable.value == AchBankAccountVerifyViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.submit -> {
                verifyAchBankAccountViewModel.onVerifyAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}