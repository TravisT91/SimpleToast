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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountsAndTransfersListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CC_ACCOUNT_ID
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CC_ACCOUNT_ID_KEY
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * Created by Atia Hashimi 12/14/18
 * Refactored by Joey Hutchins 2/22/19
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountsAndTransfersListFragment: BaseEngagePageFragment() {

    private lateinit var accountsAndTransfersListViewModel: AccountsAndTransfersListViewModel
    private lateinit var recyclerViewAdapter: AccountsAndTransfersListRecyclerViewAdapter
    private lateinit var binding: FragmentAccountsAndTransfersListBinding

    private val selectionListener = object : AccountsAndTransfersListRecyclerViewAdapter.AccountsAndTransfersSelectionListener {
        override fun onItemClicked(listItem: AccountsAndTransferListItem) {
            when (listItem) {
                is AccountsAndTransferListItem.AddItem.AddBankAccountItem -> {
                    findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountAddFragment)
                }
                is AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem -> {
                    findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_cardLoadAddEditCardFragment,
                            bundleOf(CC_ACCOUNT_ID_KEY to CC_ACCOUNT_ID))
                }
                is AccountsAndTransferListItem.CreateTransferItem -> {
                    findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment)
                }
                is AccountsAndTransferListItem.BankAccountItem -> {
                    findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_achBankAccountDetailFragment,
                            Bundle().apply {
                                putLong(ACH_BANK_ACCOUNT_ID, listItem.achAccountId)
                            })
                }
                is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem -> {
                    findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment,
                            Bundle().apply {
                                putLong(SCHEDULED_LOAD_ID, listItem.scheduledLoadId)
                            })
                }
                is AccountsAndTransferListItem.CreditDebitCardItem -> {
                    findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_cardLoadAddEditCardFragment,
                            bundleOf(CC_ACCOUNT_ID_KEY to listItem.ccAccountId))
                }
            }
        }
    }

    private val accountsAndTransfersListObserver = Observer<List<AccountsAndTransferListItem>> { list ->
        recyclerViewAdapter.setAccountsAndTransfersItems(list)
    }

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
            recyclerViewAdapter = AccountsAndTransfersListRecyclerViewAdapter(selectionListener)

            recyclerView.adapter = recyclerViewAdapter

            swipeRefreshLayout.setOnRefreshListener {
                accountsAndTransfersListViewModel.refreshViews()
                swipeRefreshLayout.isRefreshing = false
            }
        }

        accountsAndTransfersListViewModel.apply {
            accountsAndTransfersListObservable.observe(viewLifecycleOwner, accountsAndTransfersListObserver)
            createTransferButtonStateObservable.observe(viewLifecycleOwner, Observer {
                // The state will be extracted when the menu is recreated.
                activity?.invalidateOptionsMenu()
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        accountsAndTransfersListViewModel.refreshViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_accounts_and_transfers, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.menu_add)
        menuItem.isVisible = accountsAndTransfersListViewModel.createTransferButtonStateObservable.value == AccountsAndTransfersListViewModel.CreateTransferButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_add -> run {
                findNavController().navigate(R.id.action_accountsAndTransfersListFragment_to_createEditTransferFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val ACH_BANK_ACCOUNT_ID = "ACH_BANK_ACCOUNT_ID"
        const val SCHEDULED_LOAD_ID = "SCHEDULED_LOAD_ID"
    }
}