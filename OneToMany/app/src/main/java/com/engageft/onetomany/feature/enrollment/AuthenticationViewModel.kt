package com.engageft.onetomany.feature.enrollment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.engageft.engagekit.EngageService

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/30/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthenticationViewModel : ViewModel() {
    enum class AuthNavigationEvent {
        PROMPT_PASSWORD,
        PROMPT_PASSCODE,
        PROMPT_FINGERPRINT,
        PROMPT_NONE
    }

    val authNavigationObservable = MutableLiveData<AuthNavigationEvent>()
    private val authManager = EngageService.getInstance().authManager

    private val authenticationObserver = Observer<Boolean> { t ->
        Log.e("Joey", "viewModelObserver")
        if (!t!!) {
            Log.e("Joey", "viewModelObserver: logged out")
            // For now, prompt password, but eventually deduce user settings.
            authNavigationObservable.value = AuthNavigationEvent.PROMPT_PASSWORD
        } else {
            Log.e("Joey", "viewModelObserver: logged in")
            authNavigationObservable.value = AuthNavigationEvent.PROMPT_NONE
        }
    }

    init {
        authNavigationObservable.value = AuthNavigationEvent.PROMPT_NONE
        authManager.authenticationStateObservable.observeForever(this.authenticationObserver)
    }

    override fun onCleared() {
        super.onCleared()
        authManager.authenticationStateObservable.removeObserver(this.authenticationObserver)
    }

    fun onUserInteraction() {
        authManager.onUserInteraction()
    }
}