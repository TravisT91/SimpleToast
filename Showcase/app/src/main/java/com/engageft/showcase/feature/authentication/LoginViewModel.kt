package com.engageft.showcase.feature.authentication

import android.os.Handler
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * LoginViewModel
 * <p>
 * ViewModel for Login screen.
 * </p>
 * Created by joeyhutchins on 10/15/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginViewModel : BaseViewModel() {

    enum class LoginNavigationEvent {
        AUTHENTICATED_ACTIVITY,
        ISSUER_STATEMENT,
        DISCLOSURES
    }

    enum class EmailValidationError {
        NONE,
        INVALID_CREDENTIALS, // Generic username/password not valid error type.
        INVALID_EMAIL // TODO(jhutchins): Error type for as-you-type formatting?
    }

    enum class PasswordValidationError {
        NONE,
        INVALID_CREDENTIALS // Generic username/password not valid error type.
    }

    enum class LoginButtonState {
        SHOW,
        HIDE
    }

    private val compositeDisposable = CompositeDisposable()
    private val handler = Handler()

    val navigationObservable = MutableLiveData<LoginNavigationEvent>()

    val email : ObservableField<String> = ObservableField("")
    var emailError : MutableLiveData<EmailValidationError> = MutableLiveData()

    var password : ObservableField<String> = ObservableField("")
    var passwordError : MutableLiveData<PasswordValidationError> = MutableLiveData()

    val rememberMe: ObservableField<Boolean> = ObservableField(false)

    val loginButtonState: MutableLiveData<LoginButtonState> = MutableLiveData()

    init {
        loginButtonState.value = LoginButtonState.HIDE
        email.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
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
    }

    private fun validateEmail() {
        // TODO(jhutchins): Real validation.
        if (email.get()!!.isNotEmpty()) {
            emailError.value = EmailValidationError.INVALID_CREDENTIALS
        } else {
            emailError.value = EmailValidationError.NONE
        }
        updateButtonState()
    }

    private fun validatePassword() {
        // TODO(jhutchins): Real validation.
        if (password.get()!!.isNotEmpty()) {
            passwordError.value = PasswordValidationError.INVALID_CREDENTIALS
        } else {
            passwordError.value = PasswordValidationError.NONE
        }
        updateButtonState()
    }

    /**
     * TODO(jhutchins): Update the button state based on whether or not there is text in both email and
     * password. We should update this probably based on smarter validation.
     */
    private fun updateButtonState() {
        val emailText = email.get()!!
        val passwordText = password.get()!!
        val currentState = loginButtonState.value

        if (emailText.isNotEmpty() && passwordText.isNotEmpty() && (currentState == LoginButtonState.HIDE)) {
            loginButtonState.value = LoginButtonState.SHOW
        } else if (currentState == LoginButtonState.SHOW) {
            loginButtonState.value = LoginButtonState.HIDE
        }
    }
}