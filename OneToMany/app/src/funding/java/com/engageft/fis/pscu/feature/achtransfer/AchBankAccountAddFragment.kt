package com.engageft.fis.pscu.feature.achtransfer

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
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAchBankAccountAddBinding
import com.engageft.fis.pscu.feature.BaseEngageFullscreenFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import utilGen1.AchAccountInfoUtils
/**
 * AchBankAccountAddFragment
 * </p>
 * Fragment for adding an ACH Bank Account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountAddFragment: BaseEngageFullscreenFragment() {
    companion object {
        const val SUCCESS_SCREEN_TYPE_KEY = "SUCCESS_SCREEN_TYPE_KEY"
        const val ADDED_SUCCESS_TYPE = 0
        const val VERIFIED_SUCCESS_TYPE = 1
    }

    private lateinit var achBankAccountViewModel: AchBankAccountAddViewModel
    private lateinit var binding: FragmentAchBankAccountAddBinding

    private val unsavedChangesDialogListener = object : InformationDialogFragment.InformationDialogFragmentListener {
        override fun onDialogFragmentPositiveButtonClicked() {
            findNavController().navigateUp()
        }
        override fun onDialogFragmentNegativeButtonClicked() {
            // Do nothing.
        }
        override fun onDialogCancelled() {
            // Do nothing.
        }
    }

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            if (achBankAccountViewModel.hasUnsavedChanges()) {
                showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                return true
            }
            return false
        }
    }

    override fun createViewModel(): BaseViewModel? {
        achBankAccountViewModel = ViewModelProviders.of(this).get(AchBankAccountAddViewModel::class.java)
        return achBankAccountViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAchBankAccountAddBinding.inflate(inflater, container, false)

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        binding.apply {
            viewModel = achBankAccountViewModel
            palette = Palette

            accountTypeBottomSheet.dialogOptions = ArrayList(AchAccountInfoUtils.accountTypeDisplayStrings(context!!))

            routingNumberInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    achBankAccountViewModel.validRoutingNumber()
                }
            })

            addButton.setOnClickListener {
                achBankAccountViewModel.isChecking = achBankAccountViewModel.accountType.get()!! == getString(R.string.TEXT_CHECKING)
                achBankAccountViewModel.onAddAccount()
            }
        }

        achBankAccountViewModel.apply {

            buttonStateObservable.observe(this@AchBankAccountAddFragment, Observer {
                when (it) {
                    AchBankAccountAddViewModel.ButtonState.SHOW -> {
                        binding.addButton.visibility = View.VISIBLE
                    }
                    AchBankAccountAddViewModel.ButtonState.HIDE -> {
                        binding.addButton.visibility = View.GONE
                    }
                }
                activity?.invalidateOptionsMenu()
            })

            routingNumberShowErrorObservable.observe(this@AchBankAccountAddFragment, Observer {
                if (it) {
                    if (!binding.routingNumberInputWithLabel.isErrorEnabled) {
                        binding.routingNumberInputWithLabel.setErrorTexts(listOf(getString(R.string.routing_number_validation_error_message)))
                    }
                } else {
                    binding.routingNumberInputWithLabel.setErrorTexts(null)
                }
            })

            navigationEventObservable.observe(this@AchBankAccountAddFragment, Observer {
                when (it) {
                    AchBankAccountNavigationEvent.BANK_ADDED_SUCCESS -> {
                        binding.root.findNavController().navigate(
                                R.id.action_achBankAccountAddFragment_to_achBankAccountAddVerifySuccessFragment,
                                Bundle().apply {
                                    putInt(SUCCESS_SCREEN_TYPE_KEY, ADDED_SUCCESS_TYPE)
                                })
                    }
                }
            })
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
        val submitMenuItem = menu!!.findItem(R.id.submit)
        submitMenuItem.title = getString(R.string.ach_bank_account_button_add)
        submitMenuItem.isVisible = achBankAccountViewModel.buttonStateObservable.value == AchBankAccountAddViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.submit -> run {
                achBankAccountViewModel.onAddAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}