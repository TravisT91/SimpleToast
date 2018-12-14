package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette

class AchBankAccountFragment: BaseEngageFullscreenFragment() {

    private lateinit var achBankAccountViewModel: AchBankAccountViewModel

    override fun createViewModel(): BaseViewModel? {
        achBankAccountViewModel = ViewModelProviders.of(this).get(AchBankAccountViewModel::class.java)
        return achBankAccountViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAchBankAccountBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = achBankAccountViewModel
            palette = Palette
        }

        savedInstanceState?.let {
            achBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, -1)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_ach_bank_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.delete)
        saveMenuItem.isVisible = achBankAccountViewModel.formStateObservable.value == AchBankAccountViewModel.FormState.EDIT
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.delete -> run {
                achBankAccountViewModel.deleteAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}