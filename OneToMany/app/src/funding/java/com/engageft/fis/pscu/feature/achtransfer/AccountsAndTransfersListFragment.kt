package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountsAndTransfersListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CC_ACCOUNT_ID
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CC_ACCOUNT_ID_KEY
import com.engageft.fis.pscu.feature.branding.Palette
import com.ob.ws.dom.utility.AchAccountInfo

/**
 * AccountsAndTransfersListFragment
 * </p>
 * Fragment for displaying and managing of Ach Bank accounts, scheduled transfers, and past transfers list.
 * </p>
 * Created by Atia Hashimi 12/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountsAndTransfersListFragment: BaseEngagePageFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel
    private lateinit var recyclerViewAdapter: AccountsAndTransfersListRecyclerViewAdapter
    private lateinit var binding: FragmentAccountsAndTransfersListBinding

    override fun createViewModel(): BaseViewModel? {
        accountsAndTransfersListViewModel = ViewModelProviders.of(this).get(AccountsAndTransfersListViewModel::class.java)
        return accountsAndTransfersListViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAccountsAndTransfersListBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = accountsAndTransfersListViewModel
            palette = Palette

            recyclerView.layoutManager = LinearLayoutManager(context!!)
            recyclerViewAdapter = AccountsAndTransfersListRecyclerViewAdapter(context!!,

                    object : AccountsAndTransfersListRecyclerViewAdapter.AchAccountInfoClickListener {

                        override fun onAchAccountDetailClicked(achAccountInfoId: Long) {
                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountDetailFragment,
                                    Bundle().apply {
                                        putLong(ACH_BANK_ACCOUNT_ID, achAccountInfoId)
                                    })
                        }

                        override fun onAddBankAccountClicked() {
                            //TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-588
                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountAddFragment)
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
                            //TODO FOTM-66 test REMOVE BEFORE MERGE
                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_cardLoadAddEditCardFragment,
                                    bundleOf(CC_ACCOUNT_ID_KEY to CC_ACCOUNT_ID))
//                            root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_cardLoadAddEditCardFragment,
//                                    bundleOf(CC_ACCOUNT_ID_KEY to CC_ACCOUNT_ID))

//                            if (accountsAndTransfersListViewModel.isBankVerified()) {
//                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment)
//                            } else {
//                                // TODO(aHashimi): User can't add more than 1 Ach Bank account, when/if multiple ach bank accounts are added the UI and this logic needs to change.
//                                // As of now the user can't add more than ach bank account but this will need to change if it did because we're just relying on the first item.
//                                // the UI logic doesn't make sense and as a result this will need to change as well.
//                                root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountVerifyFragment,
//                                        Bundle().apply {
//                                            putLong(ACH_BANK_ACCOUNT_ID, accountsAndTransfersListViewModel.achBankAccountId)
//                                        })
//                            }
                        }
                    })

            recyclerView.adapter = recyclerViewAdapter
        }

        accountsAndTransfersListViewModel.apply {

            isAchEnabledObservable.observe(viewLifecycleOwner, Observer {
                if (!it) {
                    // hide
                    recyclerViewAdapter.removeHeaderAndNotifyAdapter()
                    recyclerViewAdapter.removeAchAccountSection()
                }
            })

            isAllowAddAchAccountObservable.observe(viewLifecycleOwner, Observer {
                if (it) {
                    recyclerViewAdapter.setAccountHeaderData(getString(R.string.ach_bank_transfer_header_title),
                            getString(R.string.ach_bank_transfer_header_description))
                    recyclerViewAdapter.setAchAccountData(listOf(AchAccountInfo()))
                } else {
                    recyclerViewAdapter.removeHeaderAndNotifyAdapter()
                    recyclerViewAdapter.removeAchAccountSection()
                }
            })

            achBankAccountsListObservable.observe(viewLifecycleOwner, Observer { achAccountsList ->
                recyclerViewAdapter.removeHeaderAndNotifyAdapter()
                recyclerViewAdapter.setAchAccountData(achAccountsList)
            })

            achButtonStateObservable.observe(viewLifecycleOwner, Observer {
                when (it) {
                    AccountsAndTransfersListViewModel.AchButtonState.HIDE -> {
                        recyclerViewAdapter.setCreateTransferButtonState("", false)
                    }
                    AccountsAndTransfersListViewModel.AchButtonState.VERIFY_BANK -> {
                        recyclerViewAdapter.setCreateTransferButtonState(getString(R.string.ach_bank_transfer_verify_account), true)
                    }
                    AccountsAndTransfersListViewModel.AchButtonState.CREATE_TRANSFER -> {
                        recyclerViewAdapter.setCreateTransferButtonState(getString(R.string.ach_bank_transfer_create_transfer), true)
                    }
                }
                activity?.invalidateOptionsMenu()
            })

            achScheduledLoadListObservable.observe(viewLifecycleOwner, Observer {
                recyclerViewAdapter.setScheduledLoadData(getString(R.string.ach_bank_transfer_scheduled), it)
            })

            achHistoricalLoadListObservable.observe(viewLifecycleOwner, Observer {
                recyclerViewAdapter.setHistoricalLoadData(getString(R.string.ach_bank_transfer_recent_activity), it)
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        accountsAndTransfersListViewModel.refreshViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.general_options_menu_add_icon, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.add)
        menuItem.isVisible = accountsAndTransfersListViewModel.achButtonStateObservable.value == AccountsAndTransfersListViewModel.AchButtonState.CREATE_TRANSFER
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.add -> run {
                binding.root.findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val ACH_BANK_ACCOUNT_ID = "ACH_BANK_ACCOUNT_ID"
        const val SCHEDULED_LOAD_ID = "SCHEDULED_LOAD_ID"
    }
}