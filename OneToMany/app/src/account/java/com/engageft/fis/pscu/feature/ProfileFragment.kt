package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.feature.BaseEngageFullscreenFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentProfileBinding


/**
 * ChangeSecurityQuestionsFragment
 * <p>
 * Fragment for changing/setting a user's security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ProfileFragment : BaseEngageFullscreenFragment() {
    private lateinit var profileViewModel: ProfileViewModel
    override fun createViewModel(): BaseViewModel? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return profileViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewModel = profileViewModel
        binding.palette = Palette

        binding.legalNameInput.setEnable(false)
        profileViewModel.saveEventObservable.observe(this, Observer { saveEvent ->
            saveEvent?.let {
                var wasError = false
                val messages = ArrayList<String>()
                saveEvent.emailResult?.let {
                    if (it.isSuccess) {
                        messages.add(getString(R.string.PROFILE_SUCCESS_MESSAGE_CHANGE_EMAIL))
                    } else {
                        wasError = true
                        messages.add(it.message!!)
                    }
                }
                saveEvent.phoneResult?.let {
                    if (it.isSuccess) {
                        messages.add(getString(R.string.PROFILE_SUCCESS_MESSAGE_CHANGE_PHONE))
                    } else {
                        wasError = true
                        messages.add(it.message!!)
                    }
                }
                saveEvent.addressResult?.let {
                    if (it.isSuccess) {
                        messages.add(getString(R.string.PROFILE_SUCCESS_MESSAGE_CHANGE_ADDRESS))
                    } else {
                        wasError = true
                        messages.add(it.message!!)
                    }
                }
                var message = messages.removeAt(0)
                for (m : String in messages) {
                    message += "\n\n" + m
                }
                showDialog(InformationDialogFragment.newLotusInstance(title = if (wasError) getString(R.string.PROFILE_ERROR_TITLE) else getString(R.string.PROFILE_SUCCESS_TITLE),
                        message = message,
                        positiveButton = getString(R.string.PROFILE_SUCCESS_MESSAGE_OK)))
            }
        })
        profileViewModel.emailValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.EmailInputValidationError.NONE -> {
                    binding.emailAddressInput.setErrorTexts(null)
                }
                ProfileViewModel.EmailInputValidationError.EMPTY -> {
                    binding.emailAddressInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
                }
                ProfileViewModel.EmailInputValidationError.AT_REQUIRED -> {
                    binding.emailAddressInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMAIL_AT_REQUIRED)))
                }
            }
        })
        profileViewModel.phoneValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.PhoneInputValidationError.NONE -> {
                    binding.phoneNumberInput.setErrorTexts(null)
                }
                ProfileViewModel.PhoneInputValidationError.EMPTY -> {
                    binding.phoneNumberInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
                }
                ProfileViewModel.PhoneInputValidationError.NOT_TEN -> {
                    binding.phoneNumberInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_PHONE_NOT_TEN)))
                }
            }
        })
        profileViewModel.streetValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.InputValidationError.NONE -> {
                    binding.streetAddressInput.setErrorTexts(null)
                }
                ProfileViewModel.InputValidationError.EMPTY -> {
                    binding.streetAddressInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
                }
            }
        })
        profileViewModel.cityValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.InputValidationError.NONE -> {
                    binding.cityInput.setErrorTexts(null)
                }
                ProfileViewModel.InputValidationError.EMPTY -> {
                    binding.cityInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
                }
            }
        })
        profileViewModel.stateValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.InputValidationError.NONE -> {
                    binding.stateInput.setErrorTexts(null)
                }
                ProfileViewModel.InputValidationError.EMPTY -> {
                    binding.stateInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
                }
            }
        })
        profileViewModel.zipValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.ZipInputValidationError.NONE -> {
                    binding.zipcodeInput.setErrorTexts(null)
                }
                ProfileViewModel.ZipInputValidationError.EMPTY -> {
                    binding.zipcodeInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
                }
                ProfileViewModel.ZipInputValidationError.NOT_FIVE -> {
                    binding.zipcodeInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_ZIP_NOT_FIVE)))
                }
            }
        })
        profileViewModel.saveButtonStateObservable.observe(this, Observer { saveButtonState ->
            when (saveButtonState) {
                ProfileViewModel.SaveButtonState.GONE -> {
                    binding.saveButton.visibility = View.GONE
                }
                ProfileViewModel.SaveButtonState.VISIBLE_DISABLED -> {
                    binding.saveButton.visibility = View.VISIBLE
                    binding.saveButton.isEnabled = false
                }
                ProfileViewModel.SaveButtonState.VISIBLE_ENABLED -> {
                    binding.saveButton.visibility = View.VISIBLE
                    binding.saveButton.isEnabled = true
                }
            }
        })
        binding.emailAddressInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateEmail(false)
            }
        })
        binding.phoneNumberInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validatePhone(false)
            }
        })
        binding.streetAddressInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateStreet(false)
            }
        })
        binding.cityInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateCity(false)
            }
        })
        binding.stateInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateState(false)
            }
        })
        binding.zipcodeInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateZipCode(false)
            }
        })

        return binding.root
    }
}