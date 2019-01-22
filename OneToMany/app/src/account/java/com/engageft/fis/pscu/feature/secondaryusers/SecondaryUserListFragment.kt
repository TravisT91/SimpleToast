package com.engageft.fis.pscu.feature.secondaryusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentSecondaryUsersListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * Created by joeyhutchins on 1/15/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SecondaryUserListFragment: BaseEngagePageFragment() {

    private lateinit var secondaryUserListViewModel: SecondaryUserListViewModel
    private lateinit var recyclerViewAdapter: SecondaryUserListRecyclerViewAdapter
    private lateinit var binding: FragmentSecondaryUsersListBinding

    override fun createViewModel(): BaseViewModel? {
        secondaryUserListViewModel = ViewModelProviders.of(this).get(SecondaryUserListViewModel::class.java)
        return secondaryUserListViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSecondaryUsersListBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = secondaryUserListViewModel
            palette = Palette

            recyclerView.layoutManager = LinearLayoutManager(context!!)
            recyclerViewAdapter = SecondaryUserListRecyclerViewAdapter(context!!,

                    object : AccountsAndTransfersListRecyclerViewAdapter.AchAccountInfoClickListener {

                        override fun onAchAccounDetailClicked(achAccountInfoId: Long) {
                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountDetailFragment,
                                    Bundle().apply {
                                        putLong(ACH_BANK_ACCOUNT_ID, achAccountInfoId)
                                    })
                        }

                        override fun onAddBankAccountClicked() {
                            //TODO(aHashimi): support multiple ACH account later: https://engageft.atlassian.net/browse/FOTM-588
                            if (secondaryUserListViewModel.isAllowedToAddAccount()) {
                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountAddFragment)
                            } else {
                                //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-588
                            }
                        }
                    },

                    object : AccountsAndTransfersListRecyclerViewAdapter.ScheduledLoadListClickListener {
                        override fun onScheduledTransferClicked(scheduledLoadInfoId: Long) {
                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment,
                                    Bundle().apply {
                                        putLong(SCHEDULED_LOAD_ID, scheduledLoadInfoId)
                                    })

                        }
                    },

                    object : AccountsAndTransfersListRecyclerViewAdapter.CreateTransferButtonClickListener {
                        override fun onCreateTransferClicked() {
                            if (secondaryUserListViewModel.isBankVerified()) {
                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment)
                            } else {
                                // TODO(aHashimi): User can't add more than 1 Ach Bank account, when/if multiple ach bank accounts are added the UI and this logic needs to change.
                                // As of now the user can't add more than ach bank account but this will need to change if it did because we're just relying on the first item.
                                // the UI logic doesn't make sense and as a result this will need to change as well.
                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountVerifyFragment,
                                        Bundle().apply {
                                            putLong(ACH_BANK_ACCOUNT_ID, secondaryUserListViewModel.achBankAccountId)
                                        })
                            }
                        }
                    })

            recyclerView.adapter = recyclerViewAdapter
        }

        secondaryUserListViewModel.apply {

            achAccountsListAndStatusObservable.observe(this@SecondaryUserListFragment, Observer { achAccountListAndStatus ->

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

                activity?.invalidateOptionsMenu()

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
        secondaryUserListViewModel.refreshViews()
    }

    companion object {
        const val ACH_BANK_ACCOUNT_ID = "ACH_BANK_ACCOUNT_ID"
        const val SCHEDULED_LOAD_ID = "SCHEDULED_LOAD_ID"
    }
}