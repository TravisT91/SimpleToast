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
import com.ob.ws.dom.utility.AchLoadInfo
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_accounts_and_transfers_list.*

class AccountsAndTransfersListFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel
    private lateinit var recyclerViewAdapter: AccountsAndTransfersListRecyclerViewAdapter
//    private lateinit var recyclerViewAdapter: SectionedRecyclerViewAdapter

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
            recyclerViewAdapter = AccountsAndTransfersListRecyclerViewAdapter(context!!)
//            recyclerViewAdapter = SectionedRecyclerViewAdapter()
            recyclerView.adapter = recyclerViewAdapter


        }

        accountsAndTransfersListViewModel.apply {

            bankAccountStatusObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                when (it) {
                    AccountsAndTransfersListViewModel.BankAccountStatus.VERIFIED_BANK_ACCOUNT -> {
                        recyclerViewAdapter.setAccountHeaderData("Header bank section", "SubText of Header")
                    }
                }
            })

            achAccountListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                recyclerViewAdapter.setAccountData(it)
            })

            achScheduledLoadListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
//                recyclerViewAdapter.setScheduledLoadData("Scheduled", it)
                // convert list of schedule
                recyclerViewAdapter.setScheduledLoadData("Scheduled", it)
            })

            achHistoricalLoadListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                recyclerViewAdapter.setHistoricalLoadData("Recent Activity", it.second)

            })
        }

        return binding.root
    }
}