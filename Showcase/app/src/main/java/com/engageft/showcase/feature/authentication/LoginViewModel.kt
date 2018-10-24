package com.engageft.showcase.feature.authentication

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.DialogInfo
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.showcase.HeapUtils
import com.engageft.showcase.config.AuthenticationConfig
import com.ob.ws.dom.DeviceFailResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * LoginViewModel
 * <p>
 * ViewModel for Login screen.
 * </p>
 * Created by joeyhutchins on 10/15/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginViewModel : BaseViewModel() {
    private var loginResponse: LoginResponse? = null

    enum class LoginNavigationEvent {
        AUTHENTICATED_ACTIVITY,
        ISSUER_STATEMENT,
        DISCLOSURES,
        TWO_FACTOR_AUTHENTICATION,
        ACCEPT_TERMS
    }

    enum class EmailValidationError {
        NONE,
        INVALID_CREDENTIALS, // Generic username/password not valid error type.
    }

    enum class PasswordValidationError {
        NONE,
        INVALID_CREDENTIALS // Generic username/password not valid error type.
    }

    enum class LoginButtonState {
        SHOW,
        HIDE
    }

    private val compositeDisposable = CompositeDisposable()

    val navigationObservable = MutableLiveData<LoginNavigationEvent>()

    val email : ObservableField<String> = ObservableField("")
    var emailError : MutableLiveData<EmailValidationError> = MutableLiveData()

    var password : ObservableField<String> = ObservableField("")
    var passwordError : MutableLiveData<PasswordValidationError> = MutableLiveData()

    val rememberMe: ObservableField<Boolean> = ObservableField(false)

    val loginButtonState: MutableLiveData<LoginButtonState> = MutableLiveData()

    val dialogInfoObservable: MutableLiveData<LoginDialogInfo> = MutableLiveData()

    init {
        loginButtonState.value = LoginButtonState.HIDE
        email.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateEmail()
            }
        })
        password.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validatePassword()
            }
        })
        rememberMe.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                // TODO(jhutchins): Maybe we don't need to do anything every time this value is
                // changed?
            }
        })
    }

    fun issuerStatementClicked() {
        // TODO(jhutchins): Should we directly databind this?
        navigationObservable.value = LoginNavigationEvent.ISSUER_STATEMENT
    }

    fun disclosuresClicked() {
        // TODO(jhutchins): Should we directly databind this?
        navigationObservable.value = LoginNavigationEvent.DISCLOSURES
    }

    fun forgotPasswordClicked() {
        // TODO(jhutchins): Should we directly databind this?
        // TODO(jhutchins): Launch a dialog somehow.
    }

    fun loginClicked() {
        // Make sure there's no stale data. Might want to keep some around, but for now, just wipe it all out.
        EngageService.getInstance().authManager.logout()

        // let's clear previous credentials error messages if applicable
        clearPreviousErrorMessages()

        //TODO(aHashimi): temp value, must be changed when working on https://engageft.atlassian.net/browse/SHOW-322 RememberMe implementation
        val rememberMe = false
        progressOverlayShownObservable.value = true
        compositeDisposable.add(
                EngageService.getInstance().loginObservable(email.get()!!, password.get()!!, null, rememberMe)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    progressOverlayShownObservable.value = false
                                    if (response.isSuccess && response is LoginResponse) {
                                        handleLoginResponse(response)
                                    } else if (response is DeviceFailResponse) {
                                        navigationObservable.value = LoginNavigationEvent.TWO_FACTOR_AUTHENTICATION
                                    } else {
                                        // we’re not yet truly parsing error types, and instead assume any error means invalid credentials.
                                        // so set backend error response message as "invalid credentials" for now until true error handling has been implemented.
                                        // https://engageft.atlassian.net/browse/SHOW-364
                                        emailError.value = EmailValidationError.INVALID_CREDENTIALS
                                        passwordError.value = PasswordValidationError.INVALID_CREDENTIALS
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
                            // TODO(aHahsimi) Proper error handling handle throwable and/or show dialog? https://engageft.atlassian.net/browse/SHOW-364
                            dialogInfoObservable.value = LoginDialogInfo()
                        })
        )
    }

    private fun clearPreviousErrorMessages() {
        emailError.value?.let {
            if (it == EmailValidationError.INVALID_CREDENTIALS) {
                emailError.value = EmailValidationError.NONE
            }
        }
        passwordError.value?.let {
            if (it == PasswordValidationError.INVALID_CREDENTIALS) {
                passwordError.value = PasswordValidationError.NONE
            }
        }
    }

    private fun handleLoginResponse(loginResponse: LoginResponse) {
        this.loginResponse = loginResponse

        // Setup unique user identifier for Heap analytics
        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        if (accountInfo != null && accountInfo.accountId != 0L) {
            HeapUtils.identifyUser(accountInfo.accountId.toString())
        }

        //TODO(aHashimi): Does it still make sense to keep this here? Must consider when working on RememberMe implementation SHOW-322
        // This is exclusively used to enable defaulting rememberMeCheckbox to on for first use,
        // and then tracking it later by whether there's a saved username, which was original logic. See
        // updateSavedUsernameAndRememberMe().
        EngageService.getInstance().storageManager.setUsedFirstTime()

        if (AuthenticationConfig.requireEmailConfirmation && LoginResponseUtils.requireEmailVerification(loginResponse)) {
            dialogInfoObservable.value = LoginDialogInfo(dialogType = LoginDialogInfo.DialogType.EMAIL_VERIFICATION)
        } else if (loginResponse.isRequireAcceptTerms) {
            navigationObservable.value = LoginNavigationEvent.ACCEPT_TERMS
        } else {
            navigationObservable.value = LoginNavigationEvent.AUTHENTICATED_ACTIVITY
        }
    }

    private fun validateEmail() {
        updateButtonState()
    }

    private fun validatePassword() {
        updateButtonState()
    }

    /**
     * TODO(jhutchins): Update the button state based on whether or not there is text in both email and
     * password. We should update this probably based on smarter validation.
     */
    private fun updateButtonState() {
        val emailText = email.get()
        val passwordText = password.get()
        val currentState = loginButtonState.value

        if (!emailText.isNullOrEmpty() && !passwordText.isNullOrEmpty() && (currentState == LoginButtonState.HIDE)) {
            loginButtonState.value = LoginButtonState.SHOW
        } else if (emailText.isNullOrEmpty() || passwordText.isNullOrEmpty() &&currentState == LoginButtonState.SHOW) {
            loginButtonState.value = LoginButtonState.HIDE
        }
    }
}

class LoginDialogInfo(title: String? = null,
                      message: String? = null,
                      tag: String? = null,
                      val dialogType: DialogType = DialogType.GENERIC_ERROR) : DialogInfo(title, message, tag) {
    enum class DialogType {
        GENERIC_ERROR,
        SERVER_ERROR,
        EMAIL_VERIFICATION
    }
}



