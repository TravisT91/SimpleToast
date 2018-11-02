package com.engageft.feature

import android.os.Handler
import androidx.lifecycle.LiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.EngageService
import com.engageft.onetomany.feature.enrollment.EnrollmentSharedPreferencesRepo
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * SplashScreenViewModel
 * <p>
 * ViewModel for the Splash Fragment. Here, we check where to navigate the user after the Splash sequence
 * completes.
 * </p>
 * Created by joeyhutchins on 10/1/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashScreenViewModel : BaseViewModel() {
    companion object {
        private const val SPLASH_SCREEN_MINIMUM_MS = 1000L
    }
    enum class SplashNavigationEvent {
        LOGGED_IN,
        NOT_LOGGED_IN,
        FIRST_TIME
    }

    private val compositeDisposable = CompositeDisposable()
    private val handler = Handler()

    val navigationObservable = SplashNavigationLiveData()

    inner class SplashNavigationLiveData : LiveData<SplashNavigationEvent>() {
        override fun onActive() {
            super.onActive()
            doSplashInitialize()
        }

        override fun onInactive() {
            super.onInactive()
            handler.removeCallbacksAndMessages(null)
            compositeDisposable.clear()
        }

        private fun doSplashInitialize() {
            handler.postDelayed({
//                if (!EnrollmentSharedPreferencesRepo.hasSeenGetStarted()) {
                if (true) {
                    value = SplashNavigationEvent.FIRST_TIME
                    EnrollmentSharedPreferencesRepo.applyHasSeenGetStarted(true)
                } else if (EngageService.getInstance().authManager.isLoggedIn) {
                    compositeDisposable.add(
                            EngageService.getInstance().loginResponseAsObservable
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ response ->
                                        value = if (response.isSuccess && response is LoginResponse) {
                                            SplashNavigationEvent.LOGGED_IN
                                        } else {
                                            EngageService.getInstance().authManager.logout()
                                            SplashNavigationEvent.NOT_LOGGED_IN
                                        }
                                    }, { _ ->
                                        EngageService.getInstance().authManager.logout()
                                        value = SplashNavigationEvent.NOT_LOGGED_IN
                                    })
                    )
                } else {
                    value = SplashNavigationEvent.NOT_LOGGED_IN
                }
            }, SPLASH_SCREEN_MINIMUM_MS)
        }
    }
}