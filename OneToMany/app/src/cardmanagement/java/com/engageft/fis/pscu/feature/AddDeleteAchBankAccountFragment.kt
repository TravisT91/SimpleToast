package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentAddDeleteAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette

class AddDeleteAchBankAccountFragment: BaseEngageFullscreenFragment() {

    private lateinit var addDeleteAchBankAccountViewModel: AddDeleteAchBankAccountViewModel

    override fun createViewModel(): BaseViewModel? {
        addDeleteAchBankAccountViewModel = ViewModelProviders.of(this).get(AddDeleteAchBankAccountViewModel::class.java)
        return addDeleteAchBankAccountViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAddDeleteAchBankAccountBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = addDeleteAchBankAccountViewModel
            palette = Palette
        }

        savedInstanceState?.let {
            addDeleteAchBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, -1)
        }

        return binding.root
    }
}