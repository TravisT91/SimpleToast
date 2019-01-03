package com.engageft.fis.pscu.feature.authentication

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.BuildConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.exception.NoConnectivityException
import com.engageft.fis.pscu.MoEngageUtils
import com.engageft.fis.pscu.feature.DialogInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * AuthExpiredViewModel
 * <p>
 * ViewModel used for the PasswordAuthExpiredFragment.
 * </p>
 * Created by joeyhutchins on 11/6/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthExpiredViewModel : BaseViewModel() {
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

    val dialogInfoObservable: MutableLiveData<DialogInfo> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

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
        MoEngageUtils.logout()
        navigationObservable.value = AuthExpiredNavigationEvent.LOGOUT
    }

    fun onSignInClicked() {
        // NOTE: This will only happen if signInEnabled was set to true.
        progressOverlayShownObservable.value = true
        val passwordText = password.get()!!
        compositeDisposable.add(
                EngageService.getInstance().validateLoginObservable(EngageService.getInstance().storageManager.username, passwordText)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    progressOverlayShownObservable.value = false
                                    if (response.isSuccess) {
                                        navigationObservable.value = AuthExpiredNavigationEvent.LOGIN_SUCCESS
                                    } else {
                                        dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.message)
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
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

    /**
     * This is duplicated from BaseEngageViewModel because this class CANNOT inherit from
     * that class. This means this entire set of functionality should be refactored to a delegate pattern
     * so we will do that eventually as a TODO
     */

    fun handleThrowable(e: Throwable)  {
        when (e) {
            is UnknownHostException -> {
                dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION))
            }
            is NoConnectivityException -> {
                dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION))
            }
            is SocketTimeoutException -> {
                dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.CONNECTION_TIMEOUT))
            }
            // Add more specific exceptions here, if needed
            else -> {
                // This is a catch-all for anything else. Anything caught here is a BUG and should
                // be reported as such. In Debug builds, we can just blow up in the user's face but
                // on production, we need to fail gracefully and report the error so we can fix it
                // later.
                if (BuildConfig.DEBUG) {
                    dialogInfoObservable.postValue(DialogInfo(message = e.message))
                    e.printStackTrace()
                } else {
                    dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_ERROR))
                }
                Crashlytics.logException(e)
            }
        }
    }
}