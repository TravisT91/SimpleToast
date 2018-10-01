package com.engageft.showcase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/1/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashScreenViewModel : ViewModel() {

    enum class SplashNavigationEvent {
        LOGGED_IN,
        FIRST_USE,
        NOT_LOGGED_IN
    }

    val navigationObservable = MutableLiveData<SplashNavigationEvent>()

    class SplashInitializeLiveData : LiveData<SplashNavigationEvent>() {
        override fun onActive() {
            super.onActive()
        }

        override fun onInactive() {
            super.onInactive()
        }

        private fun initialize() {
        }
    }
}