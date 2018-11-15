package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentProfileBinding



/**
 * ChangeSecurityQuestionsFragment
 * <p>
 * Fragment for changing/setting a user's security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ProfileFragment : LotusFullScreenFragment() {
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
        profileViewModel.navigationObservable.observe(this, Observer { navigationEvent ->
            when (navigationEvent) {
            }
        })
        profileViewModel.emailValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.InputValidationError.NONE -> {
                    binding.emailAddressInput.setErrorTexts(null)
                }
                ProfileViewModel.InputValidationError.EMPTY -> {
                    binding.emailAddressInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMTPY)))
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