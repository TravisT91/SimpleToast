package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.adapter.HeaderLabelTitleWithSubtitleSection
import com.engageft.fis.pscu.databinding.FragmentAccountsAndTransfersListBinding
import com.engageft.fis.pscu.feature.branding.Palette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_accounts_and_transfers_list.*

class AccountsAndTransfersListFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel
//    private lateinit var recyclerViewAdapter: AccountsAndTransfersListRecyclerViewAdapter
    private lateinit var recyclerViewAdapter: SectionedRecyclerViewAdapter

    override fun createViewModel(): BaseViewModel? {
        accountsAndTransfersListViewModel = ViewModelProviders.of(this).get(AccountsAndTransfersListViewModel::class.java)
        return accountsAndTransfersListViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountsAndTransfersListBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = accountsAndTransfersListViewModel
            palette = Palette

            recyclerView.layoutManager = LinearLayoutManager(context!!)
//            recyclerViewAdapter = AccountsAndTransfersListRecyclerViewAdapter()
            recyclerViewAdapter = SectionedRecyclerViewAdapter()
            recyclerView.adapter = recyclerViewAdapter


        }

        accountsAndTransfersListViewModel.apply {
            bankAccountStatusObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                when (it) {
                    AccountsAndTransfersListViewModel.BankAccountStatus.UNVERIFIED_BANK_ACCOUNT -> {
                        recyclerViewAdapter.addSection(HeaderLabelTitleWithSubtitleSection(), "type")
                        recyclerViewAdapter.getSectionPosition("type")
                        recyclerViewAdapter.notifyHeaderChangedInSection()
                    }
                }
            })
        }

        return binding.root
    }
}