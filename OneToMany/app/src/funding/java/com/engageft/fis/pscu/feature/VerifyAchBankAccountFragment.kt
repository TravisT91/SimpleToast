package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentVerifyAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette

class VerifyAchBankAccountFragment: BaseEngageFullscreenFragment() {

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
        }

        arguments?.let {
            verifyAchBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, 0)
        }

        return binding.root
    }
}