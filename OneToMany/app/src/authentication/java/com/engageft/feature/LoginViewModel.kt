package com.engageft.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CreateDemoAccountRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.onetomany.HeapUtils
import com.engageft.onetomany.config.EngageAppConfig
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

    enum class UsernameValidationError {
        NONE,
        INVALID_CREDENTIALS, // Generic username/password not valid error type.
    }

    enum class PasswordValidationError {
        NONE,
        INVALID_CREDENTIALS // Generic username/password not valid error type.
    }

    enum class ButtonState {
        SHOW,
        HIDE
    }

    enum class LoadingOverlayDialog {
        CREATING_DEMO_ACCOUNT,
        DISMISS_DIALOG
    }

    private val compositeDisposable = CompositeDisposable()

    val navigationObservable = MutableLiveData<LoginNavigationEvent>()

    val username : ObservableField<String> = ObservableField("")
    var usernameError : MutableLiveData<UsernameValidationError> = MutableLiveData()

    var password : ObservableField<String> = ObservableField("")
    var passwordError : MutableLiveData<PasswordValidationError> = MutableLiveData()

    val rememberMe: ObservableField<Boolean> = ObservableField(false)

    val loginButtonState: MutableLiveData<ButtonState> = MutableLiveData()

    val demoAccountButtonState: MutableLiveData<ButtonState> = MutableLiveData()

    val testMode: ObservableField<Boolean> = ObservableField(false)

    val dialogInfoObservable: MutableLiveData<LoginDialogInfo> = MutableLiveData()

    val loadingOverlayDialogObservable: MutableLiveData<LoadingOverlayDialog> = MutableLiveData()

    init {
        loginButtonState.value = ButtonState.HIDE
        demoAccountButtonState.value = ButtonState.HIDE

        username.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
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
        synchronizeTestMode(AuthenticationSharedPreferencesRepo.isUsingDemoServer())
        updatePrefilledUsernameAndRememberMe()
        updateDemoAccountButtonState()
        testMode.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val useTestMode = testMode.get()!!
                EngageService.getInstance().engageConfig.isUsingProdEnvironment = !useTestMode
                AuthenticationSharedPreferencesRepo.applyUsingDemoServer(useTestMode)
                synchronizeTestMode(useTestMode)

                updateButtonState()
                updateDemoAccountButtonState()
                updatePrefilledUsernameAndRememberMe()
                // TODO(jhutchins): SHOW_268: Fingerprint auth.
                //showFingerprintAuthIfEnrolled()
            }
        })
        if (AuthenticationSharedPreferencesRepo.isFirstUse()) {
            rememberMe.set(true)
            AuthenticationSharedPreferencesRepo.clearFirstUse()
        }
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
        login(username.get()!!, password.get()!!)
    }

    fun createDemoAccount() {
        val refCode = EngageService.getInstance().engageConfig.refCode

        var isAllowedDemoAccountCreation = false

        if (EngageAppConfig.isUsingProdEnvironment && AuthenticationConfig.shouldAllowDemoAccountCreationInProd && AuthenticationConfig.demoAccountAvailable) {
            isAllowedDemoAccountCreation = true
        } else if (!EngageAppConfig.isUsingProdEnvironment && AuthenticationConfig.demoAccountAvailable) {
            isAllowedDemoAccountCreation = true
        }

        if (isAllowedDemoAccountCreation) {
            loadingOverlayDialogObservable.value = LoadingOverlayDialog.CREATING_DEMO_ACCOUNT
            compositeDisposable.add(
                    EngageService.getInstance().engageApiInterface.postDemoCreate(CreateDemoAccountRequest(refCode).fieldMap)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                if (response.isSuccess) {
                                    username.set(response.message)
                                    EngageService.getInstance().storageManager.isDemoMode = true
                                    EngageService.getInstance().storageManager.username = username.get()!!
                                    rememberMe.set(true)
                                    login(response.message, createTempPassword())
                                } else {
                                    loadingOverlayDialogObservable.value = LoadingOverlayDialog.DISMISS_DIALOG
                                    dialogInfoObservable.value = LoginDialogInfo(dialogType = LoginDialogInfo.DialogType.SERVER_ERROR)
                                }
                            }, { e ->
                                loadingOverlayDialogObservable.value = LoadingOverlayDialog.DISMISS_DIALOG
                                // TODO(aHahsimi) Proper error handling handle throwable and/or show dialog? https://engageft.atlassian.net/browse/SHOW-364
                                dialogInfoObservable.value = LoginDialogInfo()
                            })
            )
        }
    }

    private fun login(username: String, password: String) {
        // Make sure there's no stale data. Might want to keep some around, but for now, just wipe it all out.
        EngageService.getInstance().authManager.logout()

        // let's clear previous credentials error messages if applicable
        clearErrorTexts()

        progressOverlayShownObservable.value = true

        compositeDisposable.add(
                EngageService.getInstance().loginObservable(username, password, null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    progressOverlayShownObservable.value = false
                                    if (response.isSuccess && response is LoginResponse) {
                                        handleSuccessfulLoginResponse(response)
                                    } else if (response is DeviceFailResponse) {
                                        navigationObservable.value = LoginNavigationEvent.TWO_FACTOR_AUTHENTICATION
                                    } else {
                                        // we’re not yet truly parsing error types, and instead assume any error means invalid credentials.
                                        // so set backend error response message as "invalid credentials" for now until true error handling has been implemented.
                                        // https://engageft.atlassian.net/browse/SHOW-364
                                        usernameError.value = UsernameValidationError.INVALID_CREDENTIALS
                                        passwordError.value = PasswordValidationError.INVALID_CREDENTIALS
                                    }
                                }, { _ ->
                            progressOverlayShownObservable.value = false
                            // TODO(aHahsimi) Proper error handling handle throwable and/or show dialog? https://engageft.atlassian.net/browse/SHOW-364
                            dialogInfoObservable.value = LoginDialogInfo()
                        })
        )
    }

    private fun clearErrorTexts() {
        usernameError.value?.let {
            if (it == UsernameValidationError.INVALID_CREDENTIALS) {
                usernameError.value = UsernameValidationError.NONE
            }
        }
        passwordError.value?.let {
            if (it == PasswordValidationError.INVALID_CREDENTIALS) {
                passwordError.value = PasswordValidationError.NONE
            }
        }
    }

    private fun handleSuccessfulLoginResponse(loginResponse: LoginResponse) {
        this.loginResponse = loginResponse

        // Setup unique user identifier for Heap analytics
        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        if (accountInfo != null && accountInfo.accountId != 0L) {
            HeapUtils.identifyUser(accountInfo.accountId.toString())
        }

        conditionallySaveUsername()

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
        updateDemoAccountButtonState()
    }

    private fun validatePassword() {
        updateButtonState()
        updateDemoAccountButtonState()
    }

    private fun updateDemoAccountButtonState() {
        val testModeToggled = testMode.get()!!

        if (AuthenticationConfig.demoAccountAvailable
                && testModeToggled && username.get().isNullOrEmpty() && password.get().isNullOrEmpty()) {
            demoAccountButtonState.value = ButtonState.SHOW
        } else {
            demoAccountButtonState.value = ButtonState.HIDE
        }
    }

    /**
     * TODO(jhutchins): Update the button state based on whether or not there is text in both username and
     * password. We should update this probably based on smarter validation.
     */
    private fun updateButtonState() {
        val usernameText = username.get()
        val passwordText = password.get()
        val currentState = loginButtonState.value

        if (!usernameText.isNullOrEmpty() && !passwordText.isNullOrEmpty() && (currentState == ButtonState.HIDE)) {
            loginButtonState.value = ButtonState.SHOW
        } else if (usernameText.isNullOrEmpty() || passwordText.isNullOrEmpty() &&currentState == ButtonState.SHOW) {
            loginButtonState.value = ButtonState.HIDE
        }
    }

    /*
    Synchronize the test mode switch with the useTestMode setting.
     */
    private fun synchronizeTestMode(useTestMode: Boolean) {
        testMode.set(useTestMode)
        EngageService.getInstance().engageConfig.isUsingProdEnvironment = !useTestMode
    }

    private fun updatePrefilledUsernameAndRememberMe() {
        val useTestMode = testMode.get()!!
        if (useTestMode) {
            val rememberMeEnabled = AuthenticationSharedPreferencesRepo.getDemoSavedUsername().isNotEmpty()
            username.set(AuthenticationSharedPreferencesRepo.getDemoSavedUsername())
            rememberMe.set(rememberMeEnabled)
        } else {
            val rememberMeEnabled = AuthenticationSharedPreferencesRepo.getSavedUsername().isNotEmpty()
            username.set(AuthenticationSharedPreferencesRepo.getSavedUsername())
            rememberMe.set(rememberMeEnabled)
        }
    }

    private fun conditionallySaveUsername() {
        val usernameToSave = username.get()!!
        val usingTestMode = testMode.get()!!
        val saveUsername = rememberMe.get()!!
        if (usingTestMode) {
            if (saveUsername) {
                AuthenticationSharedPreferencesRepo.applyDemoSavedUsername(usernameToSave)
            } else {
                AuthenticationSharedPreferencesRepo.applyDemoSavedUsername("")
            }
        } else {
            if (saveUsername) {
                AuthenticationSharedPreferencesRepo.applySavedUsername(usernameToSave)
            } else {
                AuthenticationSharedPreferencesRepo.applySavedUsername("")
            }
        }
    }

    private fun createTempPassword(): String {
        return String.format("%c%c%c%c%d%d%d%d", '\u0064', '\u0065', '\u006D', '\u006F', 1, 0, 0, 0)
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
