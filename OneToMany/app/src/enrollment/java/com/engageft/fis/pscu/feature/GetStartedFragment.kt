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
import com.engageft.apptoolbox.view.DateInputWithLabel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGetStartedBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.redmadrobot.inputmask.MaskedTextChangedListener

/**
 * GetStartedFragment
 * <p>
 * First screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GetStartedFragment : BaseEngageFullscreenFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var getStartedViewModel: GetStartedDelegate
    private lateinit var binding: FragmentGetStartedBinding
    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        getStartedViewModel = enrollmentViewModel.getStartedDelegate
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false).apply {
            this.viewModel = enrollmentViewModel.getStartedDelegate
            this.palette = Palette
            this.cardNumberInput.addTextChangeListener(object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    val productCardModel = ProductCardModel()
                    productCardModel.cardNumberFull = cardNumberInput.getInputTextWithMask().toString()
                    binding.cardView.updateWithProductCardModel(productCardModel)
                }
            })
            // Can't databind this because we want the mask sent to ViewModel as well.
            dobInput.dateFormat = DateInputWithLabel.DateFormat.MM_DD_YYYY
            this.dobInput.addTextChangeListener(object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    getStartedViewModel.dateOfBirth.set(binding.dobInput.getInputTextWithMask().toString())
                }
            })
        }
        getStartedViewModel.cardNumberValidationObservable.observe(this, Observer { cardNumberError ->
            when (cardNumberError) {
                GetStartedDelegate.CardInputValidationError.NONE -> {
                    binding.cardNumberInput.setErrorTexts(null)
                }
                GetStartedDelegate.CardInputValidationError.NOT_SIXTEEN -> {
                    binding.cardNumberInput.setErrorTexts(listOf(getString(R.string.ENROLLMENT_CARD_NUMBER_INVALID)))
                }
                GetStartedDelegate.CardInputValidationError.EMPTY -> {
                    // This shouldn't show an error text.
                    binding.cardNumberInput.setErrorTexts(null)
                }
            }
        })
        getStartedViewModel.dateOfBirthValidationObservable.observe(this, Observer { dateOfBirthError ->
            when (dateOfBirthError) {
                GetStartedDelegate.DOBInputValidationError.NONE -> {
                    binding.dobInput.setErrorTexts(null)
                }
                GetStartedDelegate.DOBInputValidationError.INVALID -> {
                    binding.dobInput.setErrorTexts(listOf(getString(R.string.ENROLLMENT_CARD_DOB_INVALID)))
                }
                GetStartedDelegate.DOBInputValidationError.EMPTY -> {
                    // This shouldn't show an error text.
                    binding.dobInput.setErrorTexts(null)
                }
                GetStartedDelegate.DOBInputValidationError.UNDER_13 -> {
                    binding.dobInput.setErrorTexts(listOf(getString(R.string.ENROLLMENT_CARD_DOB_UNDER_13)))
                }
            }
        })

        getStartedViewModel.nextButtonStateObservable.observe(this, Observer { saveButtonState ->
            when (saveButtonState) {
                GetStartedDelegate.NextButtonState.GONE -> {
                    binding.nextButton.visibility = View.GONE
                    activity?.invalidateOptionsMenu()
                }
                GetStartedDelegate.NextButtonState.VISIBLE_ENABLED -> {
                    binding.nextButton.visibility = View.VISIBLE
                    binding.nextButton.isEnabled = true
                    activity?.invalidateOptionsMenu()
                }
            }
        })
        getStartedViewModel.dialogObservable.observe(this, Observer { dialogEvent ->
            when (dialogEvent) {
                GetStartedDelegate.GetStartedDialog.NONE -> {
                    // Do nothing.
                }
                GetStartedDelegate.GetStartedDialog.UNDER_18 -> {
                    val listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogFragmentNegativeButtonClicked() {
                            // User clicked "no". Keep them on this screen.
                        }
                        override fun onDialogFragmentPositiveButtonClicked() {
                            getStartedViewModel.onLegalGuardianYesClicked()
                        }
                        override fun onDialogCancelled() {
                            // User exited dialog, keep them on this screen.
                        }
                    }
                    showDialog(InformationDialogFragment.newLotusInstance(
                            title = getString(R.string.ENROLLMENT_GET_STARTED_UNDER_18_TITLE),
                            message = getString(R.string.ENROLLMENT_GET_STARTED_UNDER_18_MESSAGE),
                            buttonPositiveText = getString(R.string.ENROLLMENT_GET_STARTED_UNDER_18_POSITIVE),
                            buttonNegativeText = getString(R.string.ENROLLMENT_GET_STARTED_UNDER_18_NEGATIVE),
                            layoutType = InformationDialogFragment.LayoutType.BUTTONS_SIDE_BY_SIDE,
                            listener = listener))
                }
            }
        })

        binding.cardNumberInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                getStartedViewModel.validateCardNumber(false)
            }
        })
        binding.cardNumberInput.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        binding.dobInput.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                getStartedViewModel.validateDOB(false)
            }
        })
        binding.dobInput.setImeOptions(EditorInfo.IME_ACTION_GO)
        binding.dobInput.onImeAction(EditorInfo.IME_ACTION_DONE) { getStartedViewModel.onNextClicked() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.get_started_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.next)
        saveMenuItem.isVisible = getStartedViewModel.nextButtonStateObservable.value == GetStartedDelegate.NextButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.save -> run {
                getStartedViewModel.onNextClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}