package com.engageft.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService

/**
 * AccountSettingsViewModel
 * <p>
 * ViewModel for Account Settings landing screen.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountSettingsViewModel : BaseEngageViewModel() {
    enum class AccountSettingsNavigation {
        NONE,
        LOGOUT
    }

    val navigationObservable = MutableLiveData<AccountSettingsNavigation>()

    fun onLogoutClicked() {
        EngageService.getInstance().authManager.logout()
        navigationObservable.value = AccountSettingsNavigation.LOGOUT
    }
}