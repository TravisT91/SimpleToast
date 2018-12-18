package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.apptoolbox.util.applyTypefaceToSubstringsList
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentVerifyAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette
import kotlinx.android.synthetic.main.fragment_cancel_card.*

class VerifyAchBankAccountFragment: BaseEngageFullscreenFragment() {
    val TAG = "VerifyBankFragment"
    private lateinit var verifyAchBankAccountViewModel: VerifyAchBankAccountViewModel

    override fun createViewModel(): BaseViewModel? {
        verifyAchBankAccountViewModel = ViewModelProviders.of(this).get(VerifyAchBankAccountViewModel::class.java)
        return verifyAchBankAccountViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentVerifyAchBankAccountBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = verifyAchBankAccountViewModel
            palette = Palette

            //todo remove hard coded currencycode
            amountInputWithLabel1.currencyCode = "USD"
            amountInputWithLabel2.currencyCode = "USD"

            instructionsTextView.text = getString(R.string.ach_bank_account_verify_instructions).applyTypefaceToSubstringsList(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    listOf(getString(R.string.ach_bank_account_verify_instructions_substring1),
                            getString(R.string.ach_bank_account_verify_instructions_substring2)))

            amountInputWithLabel1.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    verifyAchBankAccountViewModel.validateAmount1()
                }
            })

            amountInputWithLabel2.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    verifyAchBankAccountViewModel.validateAmount2()
                }
            })
        }

        arguments?.let {
            verifyAchBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, 0)
        }

        verifyAchBankAccountViewModel.apply {
            amount1ShowErrorObservable.observe(this@VerifyAchBankAccountFragment, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel1.isErrorEnabled) {
                        binding.amountInputWithLabel1.setErrorTexts(listOf(getString(R.string.ach_bank_account_verify_amount_error_message)))
                    }
                } else {
                    binding.amountInputWithLabel1.setErrorTexts(null)
                }
            })

            amount2ShowErrorObservable.observe(this@VerifyAchBankAccountFragment, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel2.isErrorEnabled) {
                        binding.amountInputWithLabel2.setErrorTexts(listOf(getString(R.string.ach_bank_account_verify_amount_error_message)))
                    }
                } else {
                    binding.amountInputWithLabel2.setErrorTexts(null)
                }
            })

            dialogInfoObservable.observe(this@VerifyAchBankAccountFragment, Observer {
                if (it is AchBankAccountDialogInfo) {
                    when (it.achBankAccountDialogType) {
                        AchBankAccountDialogInfo.AchBankAccountType.DEPOSIT_AMOUNT_INVALID -> {
                            // show error message
                            showDialog(InformationDialogFragment.newLotusInstance(
                                    title = getString(R.string.alert_error_title_generic),
                                    message = getString(R.string.ach_bank_account_verify_invalid_deposits_error_message),
                                    buttonPositiveText = getString(R.string.dialog_information_ok_button)))
                        }
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
        }

        return binding.root
    }
}