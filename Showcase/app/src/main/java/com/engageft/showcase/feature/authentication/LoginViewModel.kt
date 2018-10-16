package com.engageft.showcase.feature.authentication

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/15/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginViewModel : ViewModel() {

    enum class LoginNavigationEvent {
        AUTHENTICATED_ACTIVITY,
        ISSUER_STATEMENT,
        DISCLOSURES
    }

    private val compositeDisposable = CompositeDisposable()
    private val handler = Handler()

    val navigationObservable = MutableLiveData<LoginNavigationEvent>()

    var email : String = ""
        set(value) {
            field = value
            validateEmail()
        }
        get() {
            return field
        }
    var emailError : MutableLiveData<String> = MutableLiveData()

    var password : CharSequence = ""
        set(value) {
            field = value
            validatePassword()
        }
    var passwordError : MutableLiveData<String> = MutableLiveData()

    private fun validateEmail() {
        val error = if (email.isNotEmpty()) "I'm not ready yet!" else ""
        emailError.value = error
    }

    private fun validatePassword() {
        val error = if (password.isNotEmpty()) "I'm not ready yet!" else ""
        passwordError.value = error
    }
}