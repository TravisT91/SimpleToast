package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.MayRegisterRequest
import com.engageft.fis.pscu.feature.gatekeeping.CreateAccountEnrollmentGateKeeper
import com.engageft.fis.pscu.feature.gatekeeping.GateKeeperListener
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.CIPRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TermsRequiredGatedItem
import com.engageft.fis.pscu.feature.utils.isValidPassword
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * CreateAccountDelegate
 * <p>
 * ViewModelDelegate for create account feature in enrollment/card activation.
 * </p>
 * Created by joeyhutchins on 1/10/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CreateAccountDelegate(private val viewModel: EnrollmentViewModel, private val navController: NavController, private val createAccountNavigations: EnrollmentViewModel.EnrollmentNavigations.CreateAccountNavigations) {
    companion object {
        const val TAG = "CreateAccountDelegate"
    }
    private val gateKeeperListener: GateKeeperListener = object : GateKeeperListener {
        override fun onGateOpen() {
            navController.navigate(createAccountNavigations.createAccountToSending)
        }

        override fun onGatedItemFailed(item: GatedItem) {
            when (item) {
                is CIPRequiredGatedItem -> {
                    navController.navigate(createAccountNavigations.createAccountToVerifyIdentity)
                }
                is TermsRequiredGatedItem -> {
                    navController.navigate(createAccountNavigations.createAccountToTerms)
                }
            }
        }

        override fun onItemError(item: GatedItem, e: Throwable?, message: String?) {
            // Intentionally empty and will never be called.
        }
    }
    val emailInput: ObservableField<String> = ObservableField("")
    val passwordInput: ObservableField<String> = ObservableField("")
    val passwordConfirmInput: ObservableField<String> = ObservableField("")

    lateinit var userEmail: String
    lateinit var userPassword: String

    enum class NextButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    enum class EmailValidationError {
        NONE,
        EMPTY,
        AT_REQUIRED
    }

    enum class PasswordValidationError {
        NONE,
        EMPTY,
        INVALID // Show min 6 characters 1 number
    }

    enum class ConfirmPasswordValidationError {
        NONE,
        EMPTY,
        INVALID // Does not match
    }

    private val emailInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateEmailInput(true)
            validateNextButtonState()
        }
    }
    private val passwordInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validatePasswordInput(true)
            validateNextButtonState()
        }
    }
    private val confirmPasswordInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateConfirmPasswordInput(true)
            validateNextButtonState()
        }
    }
    val nextButtonStateObservable = MutableLiveData<NextButtonState>()
    val emailValidationObservable = MutableLiveData<EmailValidationError>()
    val passwordValidationObservable = MutableLiveData<PasswordValidationError>()
    val confirmPasswordValidationObservable = MutableLiveData<ConfirmPasswordValidationError>()

    init {
        nextButtonStateObservable.value = NextButtonState.GONE
        emailValidationObservable.value = EmailValidationError.NONE
        passwordValidationObservable.value = PasswordValidationError.NONE
        confirmPasswordValidationObservable.value = ConfirmPasswordValidationError.NONE
        emailInput.addOnPropertyChangedCallback(emailInputTextWatcher)
        passwordInput.addOnPropertyChangedCallback(passwordInputTextWatcher)
        passwordConfirmInput.addOnPropertyChangedCallback(confirmPasswordInputTextWatcher)
    }

    fun onNextClicked() {
        validateEmailInput(false)
        validatePasswordInput(false)
        validateConfirmPasswordInput(false)
        if (checkAllFieldsValid()) {
            val email = emailInput.get()!!
            val request = MayRegisterRequest(email)
            viewModel.progressOverlayShownObservable.value = true
            viewModel.compositeDisposable.add(
                    EngageService.getInstance().engageApiInterface.postDebitRequestMayRegister(request.fieldMap)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                viewModel.progressOverlayShownObservable.value = false
                                if (response.isSuccess) {
                                    this.userEmail = emailInput.get()!!
                                    this.userPassword = passwordInput.get()!!

                                    val gateKeeper = CreateAccountEnrollmentGateKeeper(viewModel.activationCardInfo, gateKeeperListener)
                                    gateKeeper.run()
                                } else {
                                    // This sucks because "EXPECTED" errors can come
                                    // in here with strings but also UNEXPECTED errors
                                    // can come. There is no way for us to reliably
                                    // distinguish them, therefore we cannot
                                    // track unexpected via Crashlytics.
                                    viewModel.handleBackendErrorForForms(response, "${GetStartedDelegate.TAG} - Unexpected empty error.")
                                    // This is a workaround to essentially "clear" the dialog after an error was shown.
                                    viewModel.dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.OTHER))
                                }
                            }, { e ->
                                viewModel.progressOverlayShownObservable.value = false
                                viewModel.handleThrowable(e)
                            })
            )
        }
    }

    fun validateEmailInput(conditionallyIfError: Boolean) {
        val currentState = emailValidationObservable.value
        if ((conditionallyIfError && currentState != EmailValidationError.NONE && currentState != EmailValidationError.EMPTY) || !conditionallyIfError) {
            val newState = if (emailInput.get()!!.isEmpty()) {
                EmailValidationError.EMPTY
            } else if (!emailInput.get()!!.contains("@") ) {
                EmailValidationError.AT_REQUIRED
            } else {
                EmailValidationError.NONE
            }

            if (currentState != newState) {
                emailValidationObservable.value = newState
                validateNextButtonState()
            }
        }
    }

    fun validatePasswordInput(conditionallyIfError: Boolean) {
        val currentState = passwordValidationObservable.value

        if ((conditionallyIfError && currentState != PasswordValidationError.NONE && currentState != PasswordValidationError.EMPTY) || !conditionallyIfError) {
            val password = passwordInput.get()!!
            val newState = if (password.isEmpty()) {
                PasswordValidationError.EMPTY
            } else {
                if (!password.isValidPassword()) {
                    PasswordValidationError.INVALID
                } else {
                    PasswordValidationError.NONE
                }
            }

            if (currentState != newState) {
                passwordValidationObservable.value = newState
                validateNextButtonState()
            }
        }
    }

    fun validateConfirmPasswordInput(conditionallyIfError: Boolean) {
        val currentState = confirmPasswordValidationObservable.value
        if ((conditionallyIfError && currentState != ConfirmPasswordValidationError.NONE && currentState != ConfirmPasswordValidationError.EMPTY) || !conditionallyIfError) {
            val confirmedPassword = passwordConfirmInput.get()!!
            val newState = if (confirmedPassword.isEmpty()) {
                ConfirmPasswordValidationError.EMPTY
            } else {
                if (confirmedPassword != passwordInput.get()!!) {
                    ConfirmPasswordValidationError.INVALID
                } else {
                    ConfirmPasswordValidationError.NONE
                }
            }

            if (currentState != newState) {
                confirmPasswordValidationObservable.value = newState
                validateNextButtonState()
            }
        }
    }

    private fun validateNextButtonState() {
        val enabled = emailValidationObservable.value == EmailValidationError.NONE &&
                passwordValidationObservable.value == PasswordValidationError.NONE &&
                confirmPasswordValidationObservable.value == ConfirmPasswordValidationError.NONE &&
                emailInput.get()!!.isNotEmpty() && passwordInput.get()!!.isNotEmpty() && passwordConfirmInput.get()!!.isNotEmpty()
        nextButtonStateObservable.value = if (!enabled) {
            NextButtonState.GONE
        } else {
            NextButtonState.VISIBLE_ENABLED
        }
    }

    private fun checkAllFieldsValid(): Boolean {
        val emailValid = emailValidationObservable.value == EmailValidationError.NONE
        val passwordValid = passwordValidationObservable.value == PasswordValidationError.NONE
        val confirmPasswordValid = confirmPasswordValidationObservable.value == ConfirmPasswordValidationError.NONE

        return emailValid && passwordValid && confirmPasswordValid
    }
}