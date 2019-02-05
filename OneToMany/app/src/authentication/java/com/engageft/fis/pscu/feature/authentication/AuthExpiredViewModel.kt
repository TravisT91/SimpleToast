package com.engageft.fis.pscu.feature.authentication

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * AuthExpiredViewModel
 * <p>
 * ViewModel used for the PasswordAuthExpiredFragment.
 * </p>
 * Created by joeyhutchins on 11/6/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthExpiredViewModel : BaseEngageViewModel() {
    enum class AuthExpiredNavigationEvent {
        LOGOUT,
        LOGIN_SUCCESS,
        FORGOT_PASSWORD,
        NONE
    }

    enum class LoginButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    val navigationObservable = MutableLiveData<AuthExpiredNavigationEvent>()
    val loginButtonStateObservable = MutableLiveData<LoginButtonState>()

    var password : ObservableField<String> = ObservableField("")

    init {
        navigationObservable.value = AuthExpiredNavigationEvent.NONE
        loginButtonStateObservable.value = LoginButtonState.GONE
        password.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validatePassword()
            }
        })
    }

    fun onLogoutClicked() {
        EngageService.getInstance().authManager.logout()
        navigationObservable.value = AuthExpiredNavigationEvent.LOGOUT
    }

    fun onSignInClicked() {
        // NOTE: This will only happen if signInEnabled was set to true.
        showProgressOverlayDelayed()
        val passwordText = password.get()!!
        compositeDisposable.add(
                EngageService.getInstance().validateLoginObservable(EngageService.getInstance().storageManager.username, passwordText)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    dismissProgressOverlay()
                                    if (response.isSuccess) {
                                        navigationObservable.value = AuthExpiredNavigationEvent.LOGIN_SUCCESS
                                    } else {
                                        dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.message)
                                    }
                                }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        }))
    }

    fun onForgotPasswordClicked() {
        navigationObservable.value = AuthExpiredNavigationEvent.FORGOT_PASSWORD
    }

    private fun validatePassword() {
        val passwordText = password.get()
        if (!passwordText.isNullOrEmpty()) {
            loginButtonStateObservable.value = LoginButtonState.VISIBLE_ENABLED
        } else {
            loginButtonStateObservable.value = LoginButtonState.GONE
        }
    }
}