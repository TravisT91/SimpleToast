package com.engageft.fis.pscu.feature.secondaryusers

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
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.DateInputWithLabel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAddSecondaryUserBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles
import com.redmadrobot.inputmask.MaskedTextChangedListener

/**
 * Created by joeyhutchins on 2/5/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class AddSecondaryUserFragment : BaseEngagePageFragment() {
    private lateinit var addSecondaryViewModel: AddSecondaryUserViewModel
    private lateinit var binding: FragmentAddSecondaryUserBinding

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return if (addSecondaryViewModel.hasUnsavedChanges()) {
                val dialog = infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener)
                dialog.applyPaletteStyles(activity!!)
                fragmentDelegate.showDialog(dialog)
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

    override fun createViewModel(): BaseViewModel? {
        addSecondaryViewModel = ViewModelProviders.of(this).get(AddSecondaryUserViewModel::class.java)
        return addSecondaryViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddSecondaryUserBinding.inflate(inflater, container, false)

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        binding.apply {
            viewModel = addSecondaryViewModel
            palette = Palette

            // Can't databind this because we want the mask sent to ViewModel as well.
            dobInput.dateFormat = DateInputWithLabel.DateFormat.MM_DD_YYYY
            this.dobInput.addTextChangeListener(object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    addSecondaryViewModel.dob.set(binding.dobInput.getInputTextWithMask().toString())
                }
            })
        }
        addSecondaryViewModel.firstNameValidationObservable.observe(this, Observer { firstNameError ->
            when (firstNameError) {
                AddSecondaryUserViewModel.FirstNameValidationError.NONE -> {
                    binding.firstNameInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.FirstNameValidationError.EMPTY -> {
                    binding.firstNameInput.setErrorTexts(null)
                }
            }
        })
        addSecondaryViewModel.lastNameValidationObservable.observe(this, Observer { lastNameError ->
            when (lastNameError) {
                AddSecondaryUserViewModel.LastNameValidationError.NONE -> {
                    binding.lastNameInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.LastNameValidationError.EMPTY -> {
                    binding.lastNameInput.setErrorTexts(null)
                }
            }
        })
        addSecondaryViewModel.phoneNumberValidationObservable.observe(this, Observer { phoneNumberError ->
            when (phoneNumberError) {
                AddSecondaryUserViewModel.PhoneNumberValidationError.NONE -> {
                    binding.phoneNumberInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.PhoneNumberValidationError.EMPTY -> {
                    binding.phoneNumberInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.PhoneNumberValidationError.NOT_TEN -> {
                    binding.phoneNumberInput.setErrorTexts(listOf(getString(R.string.secondary_users_add_error_phone_not_ten)))
                }
            }
        })
        addSecondaryViewModel.dobValidationObservable.observe(this, Observer { dobError ->
            when (dobError) {
                AddSecondaryUserViewModel.DOBValidationError.NONE -> {
                    binding.dobInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.DOBValidationError.EMPTY -> {
                    binding.dobInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.DOBValidationError.INVALID -> {
                    binding.dobInput.setErrorTexts(listOf(getString(R.string.secondary_users_add_error_dob_invalid)))
                }
                AddSecondaryUserViewModel.DOBValidationError.UNDER_13 -> {
                    binding.dobInput.setErrorTexts(listOf(getString(R.string.secondary_users_add_error_dob_under_13)))
                }
            }
        })
        addSecondaryViewModel.ssnValidationObservable.observe(this, Observer { ssnError ->
            when (ssnError) {
                AddSecondaryUserViewModel.SSNValidationError.NONE -> {
                    binding.ssnInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.SSNValidationError.EMPTY -> {
                    binding.ssnInput.setErrorTexts(null)
                }
                AddSecondaryUserViewModel.SSNValidationError.INVALID -> {
                    binding.dobInput.setErrorTexts(listOf(getString(R.string.secondary_users_add_error_ssn_invalid)))
                }
            }
        })

        addSecondaryViewModel.addButtonStateObservable.observe(this, Observer { addButtonState ->
            when (addButtonState) {
                AddSecondaryUserViewModel.AddButtonState.GONE -> {
                    binding.addButton.visibility = View.GONE
                    activity?.invalidateOptionsMenu()
                }
                AddSecondaryUserViewModel.AddButtonState.VISIBLE_ENABLED -> {
                    binding.addButton.visibility = View.VISIBLE
                    binding.addButton.isEnabled = true
                    activity?.invalidateOptionsMenu()
                }
            }
        })
        addSecondaryViewModel.ssnVisibilityObservable.observe(this, Observer { show ->
            if (show) {
                binding.ssnInput.visibility = View.VISIBLE
                binding.dobInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
                binding.ssnInput.setImeOptions(EditorInfo.IME_ACTION_GO)
                binding.ssnInput.onImeAction(EditorInfo.IME_ACTION_GO) { addSecondaryViewModel.onAddClicked() }
            } else {
                binding.ssnInput.visibility = View.GONE
                binding.dobInput.setImeOptions(EditorInfo.IME_ACTION_GO)
                binding.dobInput.onImeAction(EditorInfo.IME_ACTION_GO) { addSecondaryViewModel.onAddClicked() }
            }
        })

        addSecondaryViewModel.navigationObservable.observe(this, Observer { navigationEvent ->
            when (navigationEvent) {
                AddSecondaryUserViewModel.NavigationEvent.SUCCESS -> {
                    findNavController().navigate(R.id.action_addSecondaryUserFragment_to_addSecondarySuccessFragment)
                }
                AddSecondaryUserViewModel.NavigationEvent.ERROR -> {
                    findNavController().navigate(R.id.action_addSecondaryUserFragment_to_addSecondaryErrorFragment)
                }
            }
        })

        binding.firstNameInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                addSecondaryViewModel.validateFirstName(false)
            }
        })
        binding.firstNameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.lastNameInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                addSecondaryViewModel.validateLastName(false)
            }
        })
        binding.lastNameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.phoneNumberInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                addSecondaryViewModel.validatePhoneNumber(false)
            }
        })
        binding.phoneNumberInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.dobInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                addSecondaryViewModel.validateDOB(false)
            }
        })
        binding.ssnInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                addSecondaryViewModel.validateSSN(false)
            }
        })
        // IME settings for last two widgets are handled in the SSN visibility observable.

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.add_secondary_user_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.add)
        saveMenuItem.isVisible = addSecondaryViewModel.addButtonStateObservable.value == AddSecondaryUserViewModel.AddButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.next -> {
                addSecondaryViewModel.onAddClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}