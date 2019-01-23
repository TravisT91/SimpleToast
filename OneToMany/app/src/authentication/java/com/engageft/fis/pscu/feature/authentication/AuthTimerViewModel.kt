package com.engageft.fis.pscu.feature.authentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.auth.AuthState

/**
 * AuthTimerViewModel
 * <p>
 * This is an activity level ViewModel to be used only by BaserAuthenticatedActivity. This viewmodel
 * managers an observer to authentication state and instructs the activity to interrupt navigation
 * if and when authentication timers expire.
 * </p>
 * Created by joeyhutchins on 10/30/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthTimerViewModel : ViewModel() {
    enum class AuthNavigationEvent {
        PROMPT_PASSWORD,
        PROMPT_PASSCODE,
        PROMPT_FINGERPRINT,
        PROMPT_NONE
    }

    val expiredAuthNavigationObservable = MutableLiveData<AuthNavigationEvent>()
    val finishObservable = SingleLiveEvent<Unit>()
    private val authManager = EngageService.getInstance().authManager

    private val authenticationObserver = Observer<AuthState> { authState ->
        when (authState) {
            is AuthState.Expired -> {
                // For now, prompt password, but eventually deduce user settings.
                expiredAuthNavigationObservable.value = AuthNavigationEvent.PROMPT_PASSWORD
            }
            is AuthState.LoggedIn -> {
                expiredAuthNavigationObservable.value = AuthNavigationEvent.PROMPT_NONE
            }
            is AuthState.LoggedOut -> {
                finishObservable.call()
            }
        }
    }

    init {
        expiredAuthNavigationObservable.value = AuthNavigationEvent.PROMPT_NONE
        authManager.authExpirationUIObservable.observeForever(this.authenticationObserver)
    }

    override fun onCleared() {
        super.onCleared()
        authManager.authExpirationUIObservable.removeObserver(this.authenticationObserver)
    }

    fun onUserInteraction() {
        authManager.onUserInteraction()
    }
}