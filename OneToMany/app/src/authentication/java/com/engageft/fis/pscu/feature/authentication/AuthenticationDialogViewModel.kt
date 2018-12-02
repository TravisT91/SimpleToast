package com.engageft.fis.pscu.feature.authentication

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
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

    val usernameObservable = MutableLiveData<String>()

    enum class AuthMethod {
        BIOMETRIC,
        PASSCODE,
        PASSWORD
    }
    val authMethodObservable = MutableLiveData<AuthMethod>()

    val continueButtonEnabledObservable = MutableLiveData<Boolean>()

    enum class AuthEvent {
        SUCCESS,
        FAILURE
    }
    val authEventObservable = MutableLiveData<AuthEvent>()

    val errorMessageObservable = MutableLiveData<String>()

    init {
        usernameObservable.value = EngageService.getInstance().storageManager.username

        authMethodObservable.value =
                when {
                    EngageService.getInstance().authManager.isFingerprintAuthEnrolled -> AuthMethod.BIOMETRIC
                    EngageService.getInstance().authManager.isPasscodeEnrolled -> AuthMethod.PASSCODE
                    else -> AuthMethod.PASSWORD
                }

        continueButtonEnabledObservable.value = false
    }

    fun authenticateBiometric() {
        // placeholder
    }

    fun authenticatePasscode(passcode: String) {
        // placeholder
    }

    fun authenticatePassword(password: String) {
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
                                        authEventObservable.value = AuthEvent.FAILURE
                                        errorMessageObservable.value = response.message
                                    }
                                }, { e ->
                            // TODO(kurt): maybe use authEventObservable and errorMessageObservable rather than handleThrowable, since this is a dialog already?
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }
}