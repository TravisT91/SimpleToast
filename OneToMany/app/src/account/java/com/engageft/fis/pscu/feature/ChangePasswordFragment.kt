package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentChangePasswordBinding

/**
 * ChangePasswordFragment
 * <p>
 * ViewModel for changing password Screen.
 * </p>
 * Created by Atia Hashimi on 11/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangePasswordFragment: LotusFullScreenFragment() {

    private lateinit var changePasswordViewModel: ChangePasswordViewModel

    override fun createViewModel(): BaseViewModel? {
        changePasswordViewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
        return changePasswordViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = changePasswordViewModel

            updatePasswordButton.setOnClickListener {
                changePasswordViewModel.updatePassword()
            }

            newPasswordWithLabel1.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    changePasswordViewModel.passwordValidationErrorObservable.value?.let {
                        changePasswordViewModel.isPasswordValid()
                    }
                }
            })
            newPasswordWithLabel1.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    changePasswordViewModel.passwordValidationErrorObservable.value?.let {
                        changePasswordViewModel.isPasswordValid()
                    }
                }
            })
        }
        changePasswordViewModel.apply {

            updateButtonStateObservable.observe(this@ChangePasswordFragment, Observer { buttonState ->
                when (buttonState) {
                    ChangePasswordViewModel.UpdateButtonState.GONE -> {
                        binding.updatePasswordButton.visibility = View.GONE
                    }
                    ChangePasswordViewModel.UpdateButtonState.VISIBLE_ENABLED -> {
                        binding.updatePasswordButton.visibility = View.VISIBLE
                    }
                }
            })

            passwordValidationErrorObservable.observe(this@ChangePasswordFragment, Observer {
                when (it) {
                    pro
                    ChangePasswordViewModel.PasswordValidationError.INVALID -> {
                        binding.newPasswordWithLabel1.setErrorTexts(listOf(getString(R.string.change_password_error_message_invalid)))
                        binding.newPasswordWithLabel2.setErrorTexts(listOf(getString(R.string.change_password_error_message_invalid)))
                    }
                    ChangePasswordViewModel.PasswordValidationError.VALID -> {
                        binding.newPasswordWithLabel1.setErrorTexts(null)
                        binding.newPasswordWithLabel2.setErrorTexts(null)
                    }
                    ChangePasswordViewModel.PasswordValidationError.MISMATCH -> {
                        binding.newPasswordWithLabel1.setErrorTexts(listOf(getString(R.string.change_password_error_message_mismatch)))
                        binding.newPasswordWithLabel2.setErrorTexts(listOf(getString(R.string.change_password_error_message_mismatch)))
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
}