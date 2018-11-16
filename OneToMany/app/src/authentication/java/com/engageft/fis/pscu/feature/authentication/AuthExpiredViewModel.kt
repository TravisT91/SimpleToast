package com.engageft.fis.pscu.feature.authentication

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

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
        LOGIN_ERROR,
        FORGOT_PASSWORD,
        NONE
    }
    private val compositeDisposable = CompositeDisposable()

    val navigationObservable = MutableLiveData<AuthExpiredNavigationEvent>()

    var password : ObservableField<String> = ObservableField("")

    val signInEnabled: ObservableField<Boolean> = ObservableField(false)

    init {
        navigationObservable.value = AuthExpiredNavigationEvent.NONE
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
                                        // TODO(jhutchins): Handle error.
                                        navigationObservable.value = AuthExpiredNavigationEvent.LOGIN_ERROR
                                    }
                                }, { e ->
                            progressOverlayShownObservable.value = false
                            // TODO(jhutchins): Handle error.
                            navigationObservable.value = AuthExpiredNavigationEvent.LOGIN_ERROR
                        }))
    }

    fun onForgotPasswordClicked() {
        navigationObservable.value = AuthExpiredNavigationEvent.FORGOT_PASSWORD
    }

    private fun validatePassword() {
        val passwordText = password.get()
        if (!passwordText.isNullOrEmpty()) {
            signInEnabled.set(true)
        } else {
            signInEnabled.set(false)
        }
    }
}