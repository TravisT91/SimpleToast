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
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentChangePasswordBinding

/**
 * ChangePasswordFragment
 * <p>
 * Screen to change password.
 * </p>
 * Created by Atia Hashimi on 11/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangePasswordFragment: BaseEngageFullscreenFragment() {

    private lateinit var changePasswordViewModel: ChangePasswordViewModel

    override fun createViewModel(): BaseViewModel? {
        changePasswordViewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
        return changePasswordViewModel
    }

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return if (changePasswordViewModel.hasUnsavedChanges()) {
                showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                true
            } else {
                false
            }
        }
    }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        binding.apply {
            viewModel = changePasswordViewModel

            newPasswordWithLabel1.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    changePasswordViewModel.validateNewPassword()
                }
            })

            newPasswordWithLabel2.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    changePasswordViewModel.validatePasswordMatch()
                }
            })
        }

        changePasswordViewModel.apply {
            updateButtonStateObservable.observe(this@ChangePasswordFragment, Observer { buttonState ->
                when (buttonState) {
                    ChangePasswordViewModel.UpdateButtonState.GONE -> {
                        binding.updatePasswordButton.visibility = View.GONE
                        activity?.invalidateOptionsMenu()
                    }
                    ChangePasswordViewModel.UpdateButtonState.VISIBLE_ENABLED -> {
                        binding.updatePasswordButton.visibility = View.VISIBLE
                        activity?.invalidateOptionsMenu()
                    }
                }
            })

            newPasswordErrorStateObservable.observe(this@ChangePasswordFragment, Observer {
                when (it) {
                    ChangePasswordViewModel.ErrorState.ERROR_SET -> {
                        binding.newPasswordWithLabel1.setErrorTexts(listOf(
                                getString(R.string.change_password_error_message_invalid_1),
                                getString(R.string.change_password_error_message_invalid_2)))
                    }
                    ChangePasswordViewModel.ErrorState.ERROR_NONE -> {
                        binding.newPasswordWithLabel1.setErrorTexts(null)
                    }
                }
            })

            confirmPasswordErrorObservable.observe(this@ChangePasswordFragment, Observer {
                when (it) {
                    ChangePasswordViewModel.ErrorState.ERROR_SET -> {
                        binding.newPasswordWithLabel2.setErrorTexts(listOf(getString(R.string.change_password_error_message_mismatch)))
                    }
                    ChangePasswordViewModel.ErrorState.ERROR_NONE -> {
                        binding.newPasswordWithLabel2.setErrorTexts(null)
                    }
                    else -> {}
                }
            })

            dialogInfoObservable.observe(this@ChangePasswordFragment, Observer { dialogInfo ->
                when (dialogInfo.dialogType) {
                    DialogInfo.DialogType.GENERIC_SUCCESS -> {
                        val listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                            override fun onDialogFragmentNegativeButtonClicked() {
                            }

                            override fun onDialogFragmentPositiveButtonClicked() {
                                binding.root.findNavController().popBackStack()
                            }

                            override fun onDialogCancelled() {
                                binding.root.findNavController().popBackStack()
                            }
                        }

                        showDialog(infoDialogGenericSuccessTitleMessageNewInstance(context!!, listener = listener))

                    }
                    DialogInfo.DialogType.GENERIC_ERROR -> {
                        showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                    }
                    DialogInfo.DialogType.SERVER_ERROR -> {
                        showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                    }
                    else -> {}
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
        inflater?.inflate(R.menu.change_password_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val updateMenuItem = menu!!.findItem(R.id.update)
        updateMenuItem.isVisible = changePasswordViewModel.updateButtonStateObservable.value == ChangePasswordViewModel.UpdateButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.update -> run {
                changePasswordViewModel.onUpdateClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}