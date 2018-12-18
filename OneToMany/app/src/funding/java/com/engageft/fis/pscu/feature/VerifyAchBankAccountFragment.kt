package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentVerifyAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette

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
                        binding.amountInputWithLabel1.setErrorTexts(listOf("Amount must be between $0.01 and $0.99"))
                    }
                } else {
                    binding.amountInputWithLabel1.setErrorTexts(null)
                }
            })

            amount2ShowErrorObservable.observe(this@VerifyAchBankAccountFragment, Observer {
                if (it) {
                    if (!binding.amountInputWithLabel2.isErrorEnabled) {
                        binding.amountInputWithLabel2.setErrorTexts(listOf("Amount must be between $0.01 and $0.99"))
                    }
                } else {
                    binding.amountInputWithLabel2.setErrorTexts(null)
                }
            })
        }

        return binding.root
    }
}