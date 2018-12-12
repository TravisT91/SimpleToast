package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountsAndTransfersListBinding
import com.engageft.fis.pscu.feature.branding.Palette

class AccountsAndTransfersListFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel
    private lateinit var binding: FragmentAccountsAndTransfersListBinding
    private lateinit var recyclerViewAdapter: AccountsAndTransfersListRecyclerViewAdapter

    override fun createViewModel(): BaseViewModel? {
        accountsAndTransfersListViewModel = ViewModelProviders.of(this).get(AccountsAndTransfersListViewModel::class.java)
        return accountsAndTransfersListViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAccountsAndTransfersListBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = accountsAndTransfersListViewModel
            palette = Palette

            recyclerView.layoutManager = LinearLayoutManager(context!!)
            recyclerViewAdapter = AccountsAndTransfersListRecyclerViewAdapter(context!!,

                    object: AccountsAndTransfersListRecyclerViewAdapter.AchAccountInfoClickListener {
                        override fun onAchAccountInfoClicked(achAccountInfoId: Long) {
                            //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-65
                            //TODO(aHashimi): the new screen must check -1 which means CREATE a new bank transfer acct otherwise it's EDIT
                            Toast.makeText(this@AccountsAndTransfersListFragment.context, "on Ach Account clicked! ID = $achAccountInfoId", Toast.LENGTH_SHORT).show()
                        }
                    },

                    object: AccountsAndTransfersListRecyclerViewAdapter.ScheduledLoadListClickListener {
                        override fun onScheduledTransferClicked(scheduledLoadInfoId: Long) {
                            //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-113
                            Toast.makeText(this@AccountsAndTransfersListFragment.context, "on scheduled load clicked! ID = $scheduledLoadInfoId", Toast.LENGTH_SHORT).show()
                        }
                    })

            recyclerView.adapter = recyclerViewAdapter
        }

        accountsAndTransfersListViewModel.apply {

            achAccountsListAndStatusObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                when (it.first) {
                    AccountsAndTransfersListViewModel.BankAccountStatus.VERIFIED_BANK_ACCOUNT -> {
                        binding.createTransferButton.text = getString(R.string.ach_bank_transfer_create_transfer)
                    }
                    else -> {
                        // show header
                        recyclerViewAdapter.setAccountHeaderData(getString(R.string.ach_bank_transfer_header_title),
                                getString(R.string.ach_bank_transfer_header_description))
                        binding.createTransferButton.text = getString(R.string.ach_bank_transfer_verify_account)
                    }
                }
                recyclerViewAdapter.setAchAccountData(it.second)
            })

            buttonStateObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                when (it) {
                    AccountsAndTransfersListViewModel.ButtonState.SHOW -> binding.createTransferButton.visibility = View.VISIBLE
                    AccountsAndTransfersListViewModel.ButtonState.HIDE -> binding.createTransferButton.visibility = View.GONE
                }
                //TODO(aHashimi): FOTM-113
                // activity?.invalidateOptionsMenu()
            })

            achScheduledLoadListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                recyclerViewAdapter.setScheduledLoadData(getString(R.string.ach_bank_transfer_scheduled), it)
            })

            achHistoricalLoadListObservable.observe(this@AccountsAndTransfersListFragment, Observer {
                recyclerViewAdapter.setHistoricalLoadData(getString(R.string.ach_bank_transfer_recent_activity), it.second)
            })
        }

        return binding.root
    }
}