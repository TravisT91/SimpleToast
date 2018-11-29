package com.engageft.fis.pscu.feature.authentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.tools.AuthManager

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

    val authNavigationObservable = MutableLiveData<AuthNavigationEvent>()
    private val authManager = EngageService.getInstance().authManager

    private val authenticationObserver = Observer<AuthManager.AuthTimerState> { authTimerState ->
        when (authTimerState) {
            AuthManager.AuthTimerState.LOGGED_IN_EXPIRED -> {
                // For now, prompt password, but eventually deduce user settings.
                authNavigationObservable.value = AuthNavigationEvent.PROMPT_PASSWORD
            }
            AuthManager.AuthTimerState.LOGGED_IN_NOT_EXPIRED -> {
                authNavigationObservable.value = AuthNavigationEvent.PROMPT_NONE
            }
            AuthManager.AuthTimerState.NOT_LOGGED_IN -> {
                // Don't handle this case. An explicit call to logout was made, so there's no reason
                // to show a dialog to user.
            }
        }
    }

    init {
        authNavigationObservable.value = AuthNavigationEvent.PROMPT_NONE
        authManager.authExpirationObservable.observeForever(this.authenticationObserver)
    }

    override fun onCleared() {
        super.onCleared()
        authManager.authExpirationObservable.removeObserver(this.authenticationObserver)
    }

    fun onUserInteraction() {
        authManager.onUserInteraction()
    }
}