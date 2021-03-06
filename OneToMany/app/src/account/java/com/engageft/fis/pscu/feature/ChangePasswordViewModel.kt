package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.MoEngageUtils
import com.engageft.fis.pscu.feature.utils.isValidPassword
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

    enum class ErrorState {
        ERROR_NONE,
        ERROR_SET
    }

    var currentPassword: ObservableField<String> = ObservableField("")
    var newPassword: ObservableField<String> = ObservableField("")
    var confirmPassword: ObservableField<String> = ObservableField("")

    var newPasswordErrorStateObservable = MutableLiveData<ErrorState>()
    var confirmPasswordErrorObservable = MutableLiveData<ErrorState>()

    var updateButtonStateObservable = MutableLiveData<UpdateButtonState>()

    init {
        updateButtonStateObservable.value = UpdateButtonState.GONE

        newPasswordErrorStateObservable.value = ErrorState.ERROR_NONE
        confirmPasswordErrorObservable.value = ErrorState.ERROR_NONE

        currentPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateUpdateButtonState()
            }
        })
        newPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                if (newPasswordErrorStateObservable.value == ErrorState.ERROR_SET) {
                    validateNewPassword()
                }
                // must update validation of Confirm Password field
                if (confirmPassword.get()!!.isNotEmpty()) {
                    validatePasswordMatch()
                }
                validateUpdateButtonState()
            }
        })
        confirmPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                if (confirmPasswordErrorObservable.value == ErrorState.ERROR_SET) {
                    validatePasswordMatch()
                }
                validateUpdateButtonState()
            }
        })
    }

    fun validateNewPassword() {
        if (newPassword.get()!!.isNotEmpty() && !newPassword.get()!!.isValidPassword()) {
            newPasswordErrorStateObservable.value = ErrorState.ERROR_SET
        } else {
            newPasswordErrorStateObservable.value = ErrorState.ERROR_NONE
        }
    }

    fun validatePasswordMatch() {
        if (confirmPassword.get()!! == newPassword.get()!!) {
            confirmPasswordErrorObservable.value = ErrorState.ERROR_NONE
        } else {
            confirmPasswordErrorObservable.value = ErrorState.ERROR_SET
        }
    }

    fun onUpdateClicked() {
        if (isFormValid()) {
            if (EngageService.getInstance().authManager.isLoggedIn) {
                progressOverlayShownObservable.value = true

                compositeDisposable.add(
                        EngageService.getInstance().getUpdatePasswordObservable(newPassword.get()!!, currentPassword.get()!!)
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

    fun hasUnsavedChanges(): Boolean {
        return (currentPassword.get()!!.isNotEmpty() || newPassword.get()!!.isNotEmpty() || confirmPassword.get()!!.isNotEmpty())
    }

    private fun isFormValid(): Boolean {
        if (newPasswordErrorStateObservable.value == ErrorState.ERROR_NONE
                && confirmPasswordErrorObservable.value == ErrorState.ERROR_NONE
                && currentPassword.get()!!.isNotEmpty()) { // last check is redundant technically
            return true
        }
        return false
    }

    private fun validateUpdateButtonState() {
        if (currentPassword.get()!!.isNotEmpty() && newPassword.get()!!.isNotEmpty() && confirmPassword.get()!!.isNotEmpty()) {
            updateButtonStateObservable.value = UpdateButtonState.VISIBLE_ENABLED
        } else {
            updateButtonStateObservable.value = UpdateButtonState.GONE
        }
    }
}