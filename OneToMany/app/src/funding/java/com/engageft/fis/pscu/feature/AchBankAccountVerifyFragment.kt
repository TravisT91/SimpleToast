package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.util.applyTypefaceToSubstringsList
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountVerifyBinding
import com.engageft.fis.pscu.feature.AchBankAccountAddFragment.Companion.SUCCESS_SCREEN_TYPE_KEY
import com.engageft.fis.pscu.feature.AchBankAccountAddFragment.Companion.VERIFIED_SUCCESS_TYPE
import com.engageft.fis.pscu.feature.branding.Palette

class AchBankAccountVerifyFragment: BaseEngageFullscreenFragment() {
    val TAG = "VerifyBankFragment"
    private lateinit var verifyAchBankAccountViewModel: AchBankAccountVerifyViewModel

    override fun createViewModel(): BaseViewModel? {
        verifyAchBankAccountViewModel = ViewModelProviders.of(this).get(AchBankAccountVerifyViewModel::class.java)
        return verifyAchBankAccountViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAchBankAccountVerifyBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = verifyAchBankAccountViewModel
            palette = Palette

            amountInputWithLabel1.currencyCode = EngageAppConfig.currencyCode
            amountInputWithLabel2.currencyCode = EngageAppConfig.currencyCode

            instructionsTextView.text = getString(R.string.ach_bank_account_verify_instructions).applyTypefaceToSubstringsList(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    listOf(getString(R.string.ach_bank_account_verify_instructions_substring1),
                            getString(R.string.ach_bank_account_verify_instructions_substring2)))

            amountInputWithLabel1.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    verifyAchBankAccountViewModel.validateAmount1AndShowError()
                }
            })

            amountInputWithLabel2.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    verifyAchBankAccountViewModel.validateAmount2AndShowError()
                }
            })
        }

        arguments?.let {
            verifyAchBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, 0)
        }

        verifyAchBankAccountViewModel.apply {
            amount1ShowErrorObservable.observe(this@AchBankAccountVerifyFragment, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel1.isErrorEnabled) {
                        binding.amountInputWithLabel1.setErrorTexts(listOf(getString(R.string.ach_bank_account_verify_amount_error_message)))
                    }
                } else {
                    binding.amountInputWithLabel1.setErrorTexts(null)
                }
            })

            amount2ShowErrorObservable.observe(this@AchBankAccountVerifyFragment, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel2.isErrorEnabled) {
                        binding.amountInputWithLabel2.setErrorTexts(listOf(getString(R.string.ach_bank_account_verify_amount_error_message)))
                    }
                } else {
                    binding.amountInputWithLabel2.setErrorTexts(null)
                }
            })

            buttonStateObservable.observe(this@AchBankAccountVerifyFragment, Observer {
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

            dialogInfoObservable.observe(this@AchBankAccountVerifyFragment, Observer {
                if (it is AchBankAccountDialogInfo) {
                    when (it.achBankAccountDialogType) {
//                        AchBankAccountDialogInfo.AchBankAccountType.DEPOSIT_AMOUNT_INVALID -> {
//                            // show error message
//                            showDialog(InformationDialogFragment.newLotusInstance(
//                                    title = getString(R.string.alert_error_title_generic),
//                                    message = getString(R.string.ach_bank_account_verify_invalid_deposits_error_message),
//                                    buttonPositiveText = getString(R.string.dialog_information_ok_button)))
//                        }
                        AchBankAccountDialogInfo.AchBankAccountType.DEPOSIT_AMOUNT_MISMATCH -> {
                            // show error message
                            showDialog(InformationDialogFragment.newLotusInstance(
                                    title = getString(R.string.ach_bank_account_verify_incorrect_deposit_error_message_title),
                                    message = getString(R.string.ach_bank_account_verify_incorrect_deposit_error_message),
                                    buttonPositiveText = getString(R.string.dialog_information_ok_button)))
                        }
                    }
                }
            })

            navigationEventObservable.observe(this@AchBankAccountVerifyFragment, Observer {
                binding.root.findNavController().navigate(R.id.action_achBankAccountVerifyFragment_to_achBankAccountAddVerifySuccessFragment,
                        Bundle().apply {
                            putInt(SUCCESS_SCREEN_TYPE_KEY, VERIFIED_SUCCESS_TYPE)
                        })
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
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
            R.id.submit -> run {
                verifyAchBankAccountViewModel.onVerifyAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}