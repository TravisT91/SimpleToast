package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.isValidPassword
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * ChangePasswordViewModel
 * <p>
 * ViewModel for changing password Screen.
 * </p>
 * Created by Atia Hashimi on 11/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangePasswordViewModel: BaseEngageViewModel() {
    enum class UpdateButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    enum class PasswordValidationError {
        MISMATCH,
        INVALID,
        VALID
    }

    var currentPassword: ObservableField<String> = ObservableField("")
    var newPassword: ObservableField<String> = ObservableField("")
    var confirmPassword: ObservableField<String> = ObservableField("")

//    var currentPasswordError = MutableLiveData<Boolean>()
    var newPasswordError = MutableLiveData<PasswordValidationError>()
    var confirmPasswordError = MutableLiveData<PasswordValidationError>()

    var updateButtonStateObservable = MutableLiveData<UpdateButtonState>()
    var passwordValidationErrorObservable = MutableLiveData<PasswordValidationError>()

    init {
        currentPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateUpdateButtonState()
            }
        })
        newPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateUpdateButtonState()
            }
        })
        confirmPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateUpdateButtonState()
            }
        })
    }

    // todo rename
    fun isPasswordValid(): Boolean {
        if (newPassword.get() == confirmPassword.get()) {
            if (!isValidPassword(newPassword.get()!!)) {
                passwordValidationErrorObservable.value = PasswordValidationError.INVALID
                return false
            }
            // reset validation error when passwords are valid
            passwordValidationErrorObservable.value = PasswordValidationError.VALID

            return true
        } else {
            passwordValidationErrorObservable.value = PasswordValidationError.MISMATCH
        }
        return false
    }

    fun updatePassword() {
        if (isPasswordValid()) {
            if (EngageService.getInstance().authManager.isLoggedIn && !EngageService.getInstance().authManager.checkSecuritySession()) {
                progressOverlayShownObservable.value = true

                compositeDisposable.add(
                        EngageService.getInstance().getUpdatePasswordObservable(EngageService.getInstance().authManager.authToken, newPassword.get()!!, currentPassword.get()!!)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ response ->
                                    progressOverlayShownObservable.value = false
                                    if (response.isSuccess) {
                                        dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_SUCCESS)
                                    } else {
                                        dialogInfoObservable.value = DialogInfo(response.message, dialogType = DialogInfo.DialogType.SERVER_ERROR)
                                    }
                                }, { e ->
                                    progressOverlayShownObservable.value = false
                                    handleThrowable(e)
                                })
                )
            } else {
                EngageService.getInstance().authManager.logout()
            }
        }
    }

    private fun validateUpdateButtonState() {
        if (currentPassword.get()!!.isNotEmpty() && newPassword.get()!!.isNotEmpty() && confirmPassword.get()!!.isNotEmpty()) {
            updateButtonStateObservable.value = UpdateButtonState.VISIBLE_ENABLED
        } else {
            updateButtonStateObservable.value = UpdateButtonState.GONE
        }
    }
}