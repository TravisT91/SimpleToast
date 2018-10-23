package com.engageft.showcase.feature.authentication

import android.os.Handler
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.tools.MixpanelEvent
import com.engageft.engagekit.utils.DeviceUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.showcase.HeapUtils
import com.engageft.showcase.config.EngageAppConfig
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
    private val TAG = LoginViewModel::class.java.simpleName
    private var loginResponse: LoginResponse? = null

    enum class LoginNavigationEvent {
        AUTHENTICATED_ACTIVITY,
        ISSUER_STATEMENT,
        DISCLOSURES
    }

    enum class EmailValidationError {
        NONE,
        INVALID_CREDENTIALS, // Generic username/password not valid error type.
        INVALID_EMAIL // TODO(jhutchins): Error type for as-you-type formatting?
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
    private val handler = Handler()

    val navigationObservable = MutableLiveData<LoginNavigationEvent>()

    val email : ObservableField<String> = ObservableField("")
    var emailError : MutableLiveData<EmailValidationError> = MutableLiveData()

    var password : ObservableField<String> = ObservableField("")
    var passwordError : MutableLiveData<PasswordValidationError> = MutableLiveData()

    val rememberMe: ObservableField<Boolean> = ObservableField(false)

    val loginButtonState: MutableLiveData<LoginButtonState> = MutableLiveData()

    val shouldShowEmailVerification: MutableLiveData<Pair<Boolean, String>> = MutableLiveData()
    val promptRequireAcceptTerms: MutableLiveData<Boolean> = MutableLiveData()
    val promptTwoFactorAuth: MutableLiveData<DeviceFailResponse> = MutableLiveData()
    val loginErrorFromServer: MutableLiveData<Boolean> = MutableLiveData()

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

    fun login() {
        // Make sure there's no stale data. Might want to keep some around, but for now, just wipe it all out.
        EngageService.getInstance().authManager.logout()

        //TODO(aHashimi): temp value, must be changed when working on SHOW-322 RememberMe implementation
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
                                        promptTwoFactorAuth.value = response
                                    } else {
                                        loginErrorFromServer.value = true
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
                            // TODO(aHahsimi) handle throwable
                            loginErrorFromServer.value = false
                        })
        )
    }

    private fun handleLoginResponse(loginResponse: LoginResponse) {
        // Must set Alias and create People object in order to identify the User with
        // new non-anonymous Distinct ID.
        // https://mixpanel.com/help/reference/android#identify

        this.loginResponse = loginResponse

        val mixpanel = EngageService.getInstance().mixpanel
        //TODO(aHashimi): enable/ask runtime fingerprint permission auth to run otherwise fails to login https://engageft.atlassian.net/browse/SHOW-261
//        if (!DeviceUtils.isEmulator()) {
//            mixpanel.identifyOnLogin(loginResponse)
//        }
        mixpanel.track(MixpanelEvent.mpEventLoggedIn)

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

        if (EngageAppConfig.requiredEmailVerification && LoginResponseUtils.requireEmailVerification(loginResponse)) {
            shouldShowEmailVerification.value = Pair(true, loginResponse.token)
        } else if (loginResponse.isRequireAcceptTerms) {
            promptRequireAcceptTerms.value = true
        } else {
            navigationObservable.value = LoginNavigationEvent.AUTHENTICATED_ACTIVITY
        }
    }

    private fun validateEmail() {
        // TODO(jhutchins): Real validation.
        if (email.get()!!.isNotEmpty()) {
            emailError.value = EmailValidationError.INVALID_CREDENTIALS
        } else {
            emailError.value = EmailValidationError.NONE
        }
        updateButtonState()
    }

    private fun validatePassword() {
        // TODO(jhutchins): Real validation.
        if (password.get()!!.isNotEmpty()) {
            passwordError.value = PasswordValidationError.INVALID_CREDENTIALS
        } else {
            passwordError.value = PasswordValidationError.NONE
        }
        updateButtonState()
    }

    /**
     * TODO(jhutchins): Update the button state based on whether or not there is text in both email and
     * password. We should update this probably based on smarter validation.
     */
    private fun updateButtonState() {
        val emailText = email.get()!!
        val passwordText = password.get()!!
        val currentState = loginButtonState.value

        if (emailText.isNotEmpty() && passwordText.isNotEmpty() && (currentState == LoginButtonState.HIDE)) {
            loginButtonState.value = LoginButtonState.SHOW
        } else if (emailText.isEmpty() || passwordText.isEmpty() &&currentState == LoginButtonState.SHOW) {
            loginButtonState.value = LoginButtonState.HIDE
        }
    }
}