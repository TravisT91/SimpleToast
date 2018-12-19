package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountsAndTransfersListBinding
import com.engageft.fis.pscu.feature.branding.Palette
/**
 * AccountsAndTransfersListFragment
 * </p>
 * Fragment for displaying and managing of Ach Bank accounts, scheduled transfers, and past transfers list.
 * </p>
 * Created by Atia Hashimi 12/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountsAndTransfersListFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel
    private lateinit var recyclerViewAdapter: AccountsAndTransfersListRecyclerViewAdapter

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
            recyclerViewAdapter = AccountsAndTransfersListRecyclerViewAdapter(context!!,

                    object : AccountsAndTransfersListRecyclerViewAdapter.AchAccountInfoClickListener {
                        override fun onAchAccountInfoClicked(achAccountInfoId: Long) {
                            //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-65
                            //TODO(aHashimi): the new screen must check -1 which means CREATE a new bank transfer acct otherwise it's EDIT
                            Toast.makeText(context!!, "on Ach Account clicked! ID = $achAccountInfoId", Toast.LENGTH_SHORT).show()
                            val bundle = Bundle().apply {
                                putLong(ACH_BANK_ACCOUNT_ID, achAccountInfoId)
                            }

                            if (achAccountInfoId == 0L) {
                               root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountAddFragment, bundle)
                            } else {
                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountDetailFragment, bundle)
                            }

                        }
                    },

                    object : AccountsAndTransfersListRecyclerViewAdapter.ScheduledLoadListClickListener {
                        override fun onScheduledTransferClicked(scheduledLoadInfoId: Long) {
                            //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-113
                            Toast.makeText(context!!, "on scheduled load clicked! ID = $scheduledLoadInfoId", Toast.LENGTH_SHORT).show()
                        }
                    },

                    object : AccountsAndTransfersListRecyclerViewAdapter.CreateTransferButtonClickListener {
                        override fun onCreateTransferClicked() {
                            accountsAndTransfersListViewModel.apply {
                                achAccountsListAndStatusObservable.value?.let { achAccountListAndStatus ->
                                    if (achAccountListAndStatus.bankStatus == AccountsAndTransfersListViewModel.BankAccountStatus.VERIFIED_BANK_ACCOUNT) {
                                        // TODO(aHashimi): FOTM-113 create transfer
                                    } else {
                                        //TODO(aHashimi): FOTM-65 verify bank account
                                        //
                                        root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountVerifyFragment)
                                    }
                                }
                            }
                        }
                    })

            recyclerView.adapter = recyclerViewAdapter
        }

        accountsAndTransfersListViewModel.apply {

            achAccountsListAndStatusObservable.observe(this@AccountsAndTransfersListFragment, Observer { achAccountListAndStatus ->

                if (achAccountListAndStatus.bankStatus == AccountsAndTransfersListViewModel.BankAccountStatus.VERIFIED_BANK_ACCOUNT) {
                    recyclerViewAdapter.setButtonTextAndVisibility(getString(R.string.ach_bank_transfer_create_transfer), true)
                } else {
                    if (achAccountListAndStatus.bankStatus == AccountsAndTransfersListViewModel.BankAccountStatus.NO_BANK_ACCOUNT) {
                        recyclerViewAdapter.setButtonTextAndVisibility("", false)
                    } else {
                        recyclerViewAdapter.setButtonTextAndVisibility(getString(R.string.ach_bank_transfer_verify_account), true)
                    }
                    // show header
                    recyclerViewAdapter.setAccountHeaderData(getString(R.string.ach_bank_transfer_header_title),
                            getString(R.string.ach_bank_transfer_header_description))

                }

                //TODO(aHashimi): FOTM-113
                // activity?.invalidateOptionsMenu()

                recyclerViewAdapter.setAchAccountData(achAccountListAndStatus.achAccountInfoList)
            })

            achScheduledLoadListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                recyclerViewAdapter.setScheduledLoadData(getString(R.string.ach_bank_transfer_scheduled), it)
            })

            achHistoricalLoadListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                recyclerViewAdapter.setHistoricalLoadData(getString(R.string.ach_bank_transfer_recent_activity), it)
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //TODO(aHashimi): should replace with MutableLiveData<loginResponse> in VM?
        accountsAndTransfersListViewModel.refreshViews()
    }

    companion object {
        const val ACH_BANK_ACCOUNT_ID = "ACH_BANK_ACCOUNT_ID"
    }
}