package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette
import kotlinx.android.synthetic.main.fragment_direct_deposit.*
import utilGen1.AchAccountInfoUtils

class AchBankAccountFragment: BaseEngageFullscreenFragment() {

    private lateinit var achBankAccountViewModel: AchBankAccountViewModel
    private lateinit var binding: FragmentAchBankAccountBinding

    override fun createViewModel(): BaseViewModel? {
        achBankAccountViewModel = ViewModelProviders.of(this).get(AchBankAccountViewModel::class.java)
        return achBankAccountViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAchBankAccountBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = achBankAccountViewModel
            palette = Palette

            arguments?.let {
                achBankAccountViewModel.achAccountInfoId = it.getLong(AccountsAndTransfersListFragment.ACH_BANK_ACCOUNT_ID, 0)
            }

            accountTypeBottomSheet.dialogOptions = ArrayList(AchAccountInfoUtils.accountTypeDisplayStrings(context!!))
        }

        achBankAccountViewModel.apply {
            navigationEventObservable.observe(this@AchBankAccountFragment, Observer {
                binding.root.findNavController().navigate(R.id.action_achBankAccountFragment_to_verifyAchBankAccountFragment)
            })

            formStateObservable.observe(this@AchBankAccountFragment, Observer {
                when (it) {
                    // user can't EDIT actually, so disable all inputFields
                    AchBankAccountViewModel.FormState.EDIT -> disableAllInputFields()
                }
                activity?.invalidateOptionsMenu()
            })

            // just to populate the accountType field because we can't reference strings in VMs.
            populateAccountTypeObservable.observe(this@AchBankAccountFragment, Observer {
                if (it) {
                    accountType.set(getString(R.string.TEXT_CHECKING))
                } else {
                    accountType.set(getString(R.string.TEXT_SAVINGS))
                }
            })

            checkingAccountTypeObservable.observe(this@AchBankAccountFragment, Observer {
                isChecking = it == getString(R.string.TEXT_CHECKING)
            })
        }

        return binding.root
    }

    private fun disableAllInputFields() {
        binding.apply {
            accountNameInputWithLabel.isEnabled = false
            routingNumberInputWithLabel.isEnabled = false
            accountNumberInputWithLabel.isEnabled = false
            accountTypeBottomSheet.isEnabled = false
        }
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