package com.engageft.fis.pscu.feature.authentication

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * AuthenticationDialogViewModel
 * <p>
 * ViewModel used for the AuthenticationDialogFragment
 * </p>
 * Created by kurteous on 12/1/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthenticationDialogViewModel : BaseEngageViewModel() {
    val username = EngageService.getInstance().storageManager.username

    enum class AuthMethod {
        BIOMETRIC,
        PASSCODE,
        PASSWORD
    }
    val authMethodObservable = MutableLiveData<AuthMethod>()

    enum class AuthEvent {
        SUCCESS,
        RESET_PASSWORD
    }
    val authEventObservable = MutableLiveData<AuthEvent>()

    val errorMessageObservable = MutableLiveData<String>()

    init {
        authMethodObservable.value =
                when {
                    EngageService.getInstance().authManager.isFingerprintAuthEnrolled -> AuthMethod.BIOMETRIC
                    EngageService.getInstance().authManager.isPasscodeEnrolled -> AuthMethod.PASSCODE
                    else -> AuthMethod.PASSWORD
                }
    }

    fun tryNextAuthMethod() {
        when (authMethodObservable.value) {
            AuthMethod.BIOMETRIC -> authMethodObservable.value = AuthMethod.PASSCODE
            AuthMethod.PASSCODE -> authMethodObservable.value = AuthMethod.PASSWORD
            AuthMethod.PASSWORD -> authEventObservable.value = AuthEvent.RESET_PASSWORD
        }

        errorMessageObservable.value = null
    }

    fun authenticateBiometric() {
        // placeholder
    }

    fun authenticatePasscode(passcode: String) {
        errorMessageObservable.value = null
        progressOverlayShownObservable.value = true

        compositeDisposable.add(
                EngageService.getInstance().validateLoginPasscodeObservable(passcode)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    progressOverlayShownObservable.value = false
                                    if (response.isSuccess) {
                                        authEventObservable.value = AuthEvent.SUCCESS
                                    } else {
                                        errorMessageObservable.value = response.message
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }

    fun authenticatePassword(password: String) {
        errorMessageObservable.value = null
        progressOverlayShownObservable.value = true

        compositeDisposable.add(
                EngageService.getInstance().validateLoginObservable(EngageService.getInstance().storageManager.username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    progressOverlayShownObservable.value = false
                                    if (response.isSuccess) {
                                        authEventObservable.value = AuthEvent.SUCCESS
                                    } else {
                                        errorMessageObservable.value = response.message
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }
}