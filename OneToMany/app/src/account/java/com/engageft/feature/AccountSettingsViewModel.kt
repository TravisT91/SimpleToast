package com.engageft.feature

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
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

    fun onLogoutClicked(v: View) {
        EngageService.getInstance().authManager.logout()
        navigationObservable.value = AccountSettingsNavigation.LOGOUT
    }
}