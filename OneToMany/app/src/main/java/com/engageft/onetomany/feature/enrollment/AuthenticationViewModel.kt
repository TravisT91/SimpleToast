package com.engageft.onetomany.feature.enrollment

import androidx.lifecycle.MutableLiveData
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
    val authenticationObserver = MutableLiveData<Boolean>()

    init {
        EngageService.getInstance().authManager.disposable
    }
}