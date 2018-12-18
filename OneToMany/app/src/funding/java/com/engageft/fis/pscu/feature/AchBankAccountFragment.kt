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
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette
import kotlinx.android.synthetic.main.fragment_direct_deposit.*
import kotlinx.android.synthetic.main.fragment_help.*
import utilGen1.AchAccountInfoUtils
import javax.microedition.khronos.egl.EGLDisplay

class AchBankAccountFragment: BaseEngageFullscreenFragment() {

    private lateinit var achBankAccountViewModel: AchBankAccountViewModel
    private lateinit var binding: FragmentAchBankAccountBinding
    private var buttonAndMenuText: String = ""

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

            binding.deleteButtonLayout.setOnClickListener {
                // prompt user if they want to delete
                showDialog(InformationDialogFragment.newLotusInstance(
                        title = getString(R.string.alert_error_title_generic),
                        message = getString(R.string.ach_bank_account_verify_invalid_deposits_error_message),
                        buttonPositiveText = getString(R.string.dialog_information_ok_button)))
            }
        }

        achBankAccountViewModel.apply {


            formStateObservable.observe(this@AchBankAccountFragment, Observer {
                var screenTitle = ""
                // todo change to if
//                when (it) {
//                    // user can't EDIT actually, so disable all inputFields
//                    AchBankAccountViewModel.FormState.EDIT -> {
//                        screenTitle = getString(R.string.ach_bank_transfer_verify_account)
//                        disableAllInputFields()
//                    }
//                }
                when (it) {
                    is FormState.CreateState -> {
                        screenTitle = getString(R.string.ach_bank_account_add_account_title)
                        buttonAndMenuText = getString(R.string.ach_bank_account_button_add)
                    }
                    is FormState.EditState -> {
                        screenTitle = getString(R.string.ach_bank_transfer_verify_account)
                        disableAllInputFields()
                        if (!it.isAccountVerified) {
                            binding.deleteButtonLayout.visibility = View.VISIBLE
                            binding.submitButton.visibility = View.VISIBLE
                            buttonAndMenuText = getString(R.string.ach_bank_transfer_verify_account)
                        }
                        binding.deleteButtonLayout.visibility = View.VISIBLE
                    }
                }
                binding.submitButton.text = buttonAndMenuText
                // this should be only the first time
                toolbarController.setToolbarTitle(screenTitle)
                activity?.invalidateOptionsMenu()
            })

            buttonStateObservable.observe(this@AchBankAccountFragment, Observer {
                when (it) {
                    AchBankAccountViewModel.ButtonState.SHOW -> {
                        binding.submitButton.visibility = View.VISIBLE
                    }
                    AchBankAccountViewModel.ButtonState.HIDE -> {
                        binding.submitButton.visibility = View.GONE
                    }
                }
                activity?.invalidateOptionsMenu()
            })

            navigationEventObservable.observe(this@AchBankAccountFragment, Observer {
                when (it) {
                    AchBankAccountNavigationEvent.VERIFY_ACCOUNT -> {
                        binding.root.findNavController().navigate(R.id.action_achBankAccountFragment_to_verifyAchBankAccountFragment)
                    }
                    AchBankAccountNavigationEvent.BANK_ADDED_SUCCESS -> {

                    }
                }
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
        val submitMenuItem = menu!!.findItem(R.id.submit)
        submitMenuItem.title = buttonAndMenuText
        submitMenuItem.isVisible = achBankAccountViewModel.buttonStateObservable.value == AchBankAccountViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.submit -> run {
                achBankAccountViewModel.onAddOrVerifyAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}