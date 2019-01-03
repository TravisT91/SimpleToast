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

                        override fun onAchAccounDetailClicked(achAccountInfoId: Long) {
                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountDetailFragment,
                                    Bundle().apply {
                                        putLong(ACH_BANK_ACCOUNT_ID, achAccountInfoId)
                                    })
                        }

                        override fun onAddBankAccountClicked() {
                            //TODO(aHashimi): support multiple ACH account later: https://engageft.atlassian.net/browse/FOTM-588
                            if (accountsAndTransfersListViewModel.isAllowedToAddAccount()) {
                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountAddFragment)
                            } else {
                                //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-588
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
                                        root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment)
                                    } else {
                                        // TODO(aHashimi): User can't add more than 1 Ach Bank account, when/if multiple ach bank accounts are added the UI and this logic needs to change.
                                        // As of now the user can't add more than ach bank account but this will need to change if it did because we're just relying on the first item.
                                        // the UI logic doesn't make sense and as a result this will need to change as well.
                                        root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountVerifyFragment,
                                                Bundle().apply {
                                                    putLong(ACH_BANK_ACCOUNT_ID, accountsAndTransfersListViewModel.achBankAccountId)
                                                })
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
                    recyclerViewAdapter.removeHeaderAndNotifyAdapter()
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