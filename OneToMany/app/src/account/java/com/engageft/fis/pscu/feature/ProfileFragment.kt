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
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentProfileBinding
import com.engageft.fis.pscu.feature.utils.StringUtils


/**
 * ProfileFragment
 * <p>
 * Fragment for changing a user's profile settings. This includes email, phone number, and address.
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
                var message = StringUtils.concatenateFromList(messages, "\n\n")
                showDialog(InformationDialogFragment.newLotusInstance(title = if (wasError) getString(R.string.PROFILE_ERROR_TITLE) else getString(R.string.PROFILE_SUCCESS_TITLE),
                        message = message,
                        buttonPositiveText = getString(R.string.PROFILE_SUCCESS_MESSAGE_OK)))
            }
        })
        profileViewModel.emailValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.EmailInputValidationError.NONE -> {
                    binding.emailAddressInput.setErrorTexts(null)
                }
                ProfileViewModel.EmailInputValidationError.EMPTY -> {
                    binding.emailAddressInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMPTY)))
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
                    binding.phoneNumberInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMPTY)))
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
                    binding.streetAddressInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMPTY)))
                }
            }
        })
        profileViewModel.cityValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.InputValidationError.NONE -> {
                    binding.cityInput.setErrorTexts(null)
                }
                ProfileViewModel.InputValidationError.EMPTY -> {
                    binding.cityInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMPTY)))
                }
            }
        })
        profileViewModel.stateValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.InputValidationError.NONE -> {
                    binding.stateInput.setErrorTexts(null)
                }
                ProfileViewModel.InputValidationError.EMPTY -> {
                    binding.stateInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMPTY)))
                }
            }
        })
        profileViewModel.zipValidationObservable.observe(this, Observer { error ->
            when (error) {
                ProfileViewModel.ZipInputValidationError.NONE -> {
                    binding.zipcodeInput.setErrorTexts(null)
                }
                ProfileViewModel.ZipInputValidationError.EMPTY -> {
                    binding.zipcodeInput.setErrorTexts(arrayListOf(getString(R.string.PROFILE_INPUT_ERROR_EMPTY)))
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
                    activity?.invalidateOptionsMenu()
                }
                ProfileViewModel.SaveButtonState.VISIBLE_ENABLED -> {
                    binding.saveButton.visibility = View.VISIBLE
                    binding.saveButton.isEnabled = true
                    activity?.invalidateOptionsMenu()
                }
            }
        })
        profileViewModel.addressEditableObservable.observe(this, Observer {addressEditableState ->
            when (addressEditableState) {
                ProfileViewModel.AddressEditableState.NOT_EDITABLE -> {
                    binding.streetAddressInput.setEnable(false)
                    binding.aptSuiteInput.setEnable(false)
                    binding.cityInput.setEnable(false)
                    binding.stateInput.setEnable(false)
                    binding.zipcodeInput.setEnable(false)
                }
                ProfileViewModel.AddressEditableState.EDITABLE -> {
                    binding.streetAddressInput.setEnable(true)
                    binding.aptSuiteInput.setEnable(true)
                    binding.cityInput.setEnable(true)
                    binding.stateInput.setEnable(true)
                    binding.zipcodeInput.setEnable(true)
                }
            }
        })
        binding.emailAddressInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateEmail(false)
            }
        })
        binding.emailAddressInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.phoneNumberInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validatePhone(false)
            }
        })
        binding.phoneNumberInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.streetAddressInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateStreet(false)
            }
        })
        binding.streetAddressInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.aptSuiteInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.cityInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateCity(false)
            }
        })
        binding.cityInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.stateInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateState(false)
            }
        })
        binding.stateInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.zipcodeInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                profileViewModel.validateZipCode(false)
            }
        })
        binding.zipcodeInput.setImeOptions(EditorInfo.IME_ACTION_GO)
        binding.zipcodeInput.onImeAction(EditorInfo.IME_ACTION_DONE) { profileViewModel.onSaveClicked() }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.profile_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.save)
        saveMenuItem.isVisible = profileViewModel.saveButtonStateObservable.value == ProfileViewModel.SaveButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.save -> run {
                profileViewModel.onSaveClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}