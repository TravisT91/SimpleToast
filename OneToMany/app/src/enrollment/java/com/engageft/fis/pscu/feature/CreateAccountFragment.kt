package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCreateAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * CreateAccountFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CreateAccountFragment : BaseEngagePageFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var createAccountViewModel: CreateAccountDelegate
    private lateinit var binding: FragmentCreateAccountBinding
    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        createAccountViewModel = enrollmentViewModel.createAccountDelegate
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false).apply {
            this.viewModel = createAccountViewModel
            this.palette = Palette
        }

        createAccountViewModel.emailValidationObservable.observe(this, Observer { emailError ->
            when (emailError) {
                CreateAccountDelegate.EmailValidationError.NONE -> {
                    binding.emailInput.setErrorTexts(null)
                }
                CreateAccountDelegate.EmailValidationError.AT_REQUIRED -> {
                    binding.emailInput.setErrorTexts(listOf(getString(R.string.PROFILE_INPUT_ERROR_EMAIL_AT_REQUIRED)))
                }
                CreateAccountDelegate.EmailValidationError.EMPTY -> {
                    // This shouldn't show an error text.
                    binding.emailInput.setErrorTexts(null)
                }
            }
        })
        createAccountViewModel.passwordValidationObservable.observe(this, Observer { passwordError ->
            when (passwordError) {
                CreateAccountDelegate.PasswordValidationError.NONE -> {
                    binding.passwordInput.setErrorTexts(null)
                }
                CreateAccountDelegate.PasswordValidationError.INVALID -> {
                    binding.passwordInput.setErrorTexts(listOf(getString(R.string.change_password_error_message_invalid_1),
                            getString(R.string.change_password_error_message_invalid_2)))
                }
                CreateAccountDelegate.PasswordValidationError.EMPTY -> {
                    // This shouldn't show an error text.
                    binding.passwordInput.setErrorTexts(null)
                }
            }
        })
        createAccountViewModel.confirmPasswordValidationObservable.observe(this, Observer { confirmPasswordError ->
            when (confirmPasswordError) {
                CreateAccountDelegate.ConfirmPasswordValidationError.NONE -> {
                    binding.passwordConfirmInput.setErrorTexts(null)
                }
                CreateAccountDelegate.ConfirmPasswordValidationError.INVALID -> {
                    binding.passwordConfirmInput.setErrorTexts(listOf(getString(R.string.change_password_error_message_mismatch)))
                }
                CreateAccountDelegate.ConfirmPasswordValidationError.EMPTY -> {
                    // This shouldn't show an error text.
                    binding.passwordConfirmInput.setErrorTexts(null)
                }
            }
        })

        createAccountViewModel.nextButtonStateObservable.observe(this, Observer { saveButtonState ->
            when (saveButtonState) {
                CreateAccountDelegate.NextButtonState.GONE -> {
                    binding.nextButton.visibility = View.GONE
                    activity?.invalidateOptionsMenu()
                }
                CreateAccountDelegate.NextButtonState.VISIBLE_ENABLED -> {
                    binding.nextButton.visibility = View.VISIBLE
                    binding.nextButton.isEnabled = true
                    activity?.invalidateOptionsMenu()
                }
            }
        })

        binding.emailInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                createAccountViewModel.validateEmailInput(false)
            }
        })
        binding.emailInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.passwordInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                createAccountViewModel.validatePasswordInput(false)
            }
        })
        binding.passwordInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.passwordConfirmInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                createAccountViewModel.validateConfirmPasswordInput(false)
            }
        })
        binding.passwordConfirmInput.setImeOptions(EditorInfo.IME_ACTION_GO)
        binding.passwordConfirmInput.onImeAction(EditorInfo.IME_ACTION_GO) { createAccountViewModel.onNextClicked() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.create_account_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.next)
        saveMenuItem.isVisible = createAccountViewModel.nextButtonStateObservable.value == CreateAccountDelegate.NextButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.next -> run {
                createAccountViewModel.onNextClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}