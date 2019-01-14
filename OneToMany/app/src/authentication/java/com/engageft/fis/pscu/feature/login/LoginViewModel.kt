package com.engageft.fis.pscu.feature.login

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CreateDemoAccountRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.HeapUtils
import com.engageft.fis.pscu.MoEngageUtils
import com.engageft.fis.pscu.OneToManyApplication
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.WelcomeSharedPreferencesRepo
import com.engageft.fis.pscu.feature.authentication.AuthenticationConfig
import com.engageft.fis.pscu.feature.authentication.AuthenticationSharedPreferencesRepo
import com.engageft.fis.pscu.feature.branding.BrandingManager
import com.engageft.fis.pscu.feature.gatekeeping.GateKeeperListener
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.LoginGateKeeper
import com.engageft.fis.pscu.feature.gatekeeping.items.RequireAcceptTermsGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.RequireEmailConfirmationGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.SecurityQuestionsGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TwoFactorAuthGatedItem
import com.engageft.fis.pscu.feature.subscribeWithDefaultProgressAndErrorHandling
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.BrandingInfoResponse
import com.ob.ws.dom.DeviceFailResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * LoginViewModel
 * <p>
 * ViewModel for Login screen.
 * </p>
 * Created by joeyhutchins on 10/15/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginViewModel : BaseEngageViewModel(), GateKeeperListener {

    enum class LoginNavigationEvent {
        AUTHENTICATED_ACTIVITY,
        ISSUER_STATEMENT,
        DISCLOSURES,
        TWO_FACTOR_AUTHENTICATION,
        ACCEPT_TERMS,
        SECURITY_QUESTIONS
    }

    enum class ButtonState {
        SHOW,
        HIDE
    }

    enum class LoadingOverlayDialog {
        CREATING_DEMO_ACCOUNT,
        DISMISS_DIALOG
    }

    private lateinit var token: String

    val navigationObservable = MutableLiveData<LoginNavigationEvent>()

    val username : ObservableField<String> = ObservableField("")

    var password : ObservableField<String> = ObservableField("")

    val rememberMe: ObservableField<Boolean> = ObservableField(false)

    val loginButtonState: MutableLiveData<ButtonState> = MutableLiveData()

    val demoAccountButtonState: MutableLiveData<ButtonState> = MutableLiveData()

    val testMode: ObservableField<Boolean> = ObservableField(false)

    val loadingOverlayDialogObservable: MutableLiveData<LoadingOverlayDialog> = MutableLiveData()

    private val loginGateKeeper = LoginGateKeeper(compositeDisposable, this)

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

    override fun onGateOpen() {
        progressOverlayShownObservable.value = false
        BrandingManager.getBrandingWithToken(EngageService.getInstance().authManager.authToken)
                .subscribeWithDefaultProgressAndErrorHandling<BrandingInfoResponse>(
                        this, { navigationObservable.value = LoginNavigationEvent.AUTHENTICATED_ACTIVITY })
    }

    override fun onGatedItemFailed(item: GatedItem) {
        progressOverlayShownObservable.value = false
        when (item) {
            is TwoFactorAuthGatedItem -> {
                navigationObservable.value = LoginNavigationEvent.TWO_FACTOR_AUTHENTICATION
            }
            is RequireEmailConfirmationGatedItem -> {
                dialogInfoObservable.value = LoginDialogInfo(dialogType = DialogInfo.DialogType.OTHER,
                        loginDialogType = LoginDialogInfo.LoginDialogType.EMAIL_VERIFICATION_PROMPT)
            }
            is RequireAcceptTermsGatedItem -> {
                navigationObservable.value = LoginNavigationEvent.ACCEPT_TERMS
            }
            is SecurityQuestionsGatedItem -> {
                navigationObservable.value = LoginNavigationEvent.SECURITY_QUESTIONS
            }
        }
    }

    override fun onItemError(item: GatedItem, e: Throwable?, message: String?) {
        progressOverlayShownObservable.value = false
        message?.let{
            handleUnexpectedErrorResponse(BasicResponse(false, it))
        }
        e?.let {
            handleThrowable(it)
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
                                    dialogInfoObservable.value = LoginDialogInfo(
                                            message = response.message,
                                            dialogType = DialogInfo.DialogType.SERVER_ERROR)
                                }
                            }, { e ->
                                loadingOverlayDialogObservable.value = LoadingOverlayDialog.DISMISS_DIALOG
                                handleThrowable(e)
                            })
            )
        }
    }

    fun onConfirmEmail() {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().verificationEmailObservable(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false

                    if (response.isSuccess) {
                        dialogInfoObservable.value = LoginDialogInfo(dialogType = DialogInfo.DialogType.OTHER,
                                loginDialogType = LoginDialogInfo.LoginDialogType.EMAIL_VERIFICATION_SUCCESS)
                    } else {
                        dialogInfoObservable.value = LoginDialogInfo(
                                message = response.message,
                                dialogType = DialogInfo.DialogType.SERVER_ERROR)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    logout()
                    handleThrowable(e)
                })
        )
    }

    fun logout() {
        EngageService.getInstance().authManager.logout()
        MoEngageUtils.logout()
    }

    private fun login(username: String, password: String) {
        // Make sure there's no stale data. Might want to keep some around, but for now, just wipe it all out.
        EngageService.getInstance().authManager.logout()
        MoEngageUtils.logout()

        progressOverlayShownObservable.value = true

        compositeDisposable.add(
                EngageService.getInstance().loginObservable(username, password, null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    if (response.isSuccess && response is LoginResponse) {
                                        handleSuccessfulLoginResponse(response)
                                        loginGateKeeper.run()
                                    } else if (response is DeviceFailResponse) {
                                        // This causes the loginResponse to be fetched twice by the TwoFactorAuthGatedItem
                                        // - a minor inconvenience.
                                        // TODO(jhtuchins): The DeviceFailResponse should be cached locally
                                        // so the fetch doesn't need to happen twice when the GateKeeper runs.
                                        loginGateKeeper.run()
                                    } else {
                                        progressOverlayShownObservable.value = false
                                        dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.message)
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }

    private fun handleSuccessfulLoginResponse(loginResponse: LoginResponse) {
        token = loginResponse.token
        // set the Get started flag to true after the successful login, so the Welcome screen doesn't get displayed again
        WelcomeSharedPreferencesRepo.applyHasSeenGetStarted(true)

        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        if (accountInfo != null && accountInfo.accountId != 0L) {
            // Setup unique user identifier for Heap analytics
            HeapUtils.identifyUser(accountInfo.accountId.toString())
            // Setup user attributes for MoEngage
            MoEngageUtils.setUserAttributes(accountInfo)
        }

        conditionallySaveUsername()
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
                      dialogType: DialogType = DialogType.GENERIC_ERROR,
                      var loginDialogType: LoginDialogInfo.LoginDialogType = LoginDialogType.EMAIL_VERIFICATION_PROMPT) : DialogInfo(title, message, tag, dialogType) {
    enum class LoginDialogType {
        EMAIL_VERIFICATION_PROMPT,
        EMAIL_VERIFICATION_SUCCESS
    }
}
