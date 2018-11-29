package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
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
    var currentPassword: ObservableField<String> = ObservableField("")
    var newPassword: ObservableField<String> = ObservableField("")
    var confirmPassword: ObservableField<String> = ObservableField("")

    init {
        currentPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validatePassword()
            }
        })
        newPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validatePassword()
            }
        })
        confirmPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validatePassword()
            }
        })
    }

    fun updatePassword() {
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

    private fun validatePassword() {
        if (isValidPassword(newPassword.get()!!) && newPassword.get().equals(confirmPassword.get())
                && currentPassword.get()!!.isNotEmpty()) {
            buttonState.value = ButtonState.SHOW
        } else {
            buttonState.value = ButtonState.HIDE
        }
    }
}