package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentAccountsAndTransfersListBinding

class AccountsAndTransfersListFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel

    override fun createViewModel(): BaseViewModel? {
        accountsAndTransfersListViewModel = ViewModelProviders.of(this).get(AccountsAndTransfersListViewModel::class.java)
        return accountsAndTransfersListViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountsAndTransfersListBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = accountsAndTransfersListViewModel
            palette = Palette
        }

        return binding.root
    }
}