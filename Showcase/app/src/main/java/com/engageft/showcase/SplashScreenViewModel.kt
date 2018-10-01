package com.engageft.showcase

import android.app.Application
import android.os.Handler
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/1/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashScreenViewModel(application: Application) : AndroidViewModel(application) {

    enum class SplashNavigationEvent {
        LOGGED_IN,
        FIRST_USE,
        NOT_LOGGED_IN
    }

    val compositeDisposable = CompositeDisposable()

    val navigationObservable = MutableLiveData<SplashNavigationEvent>()

    inner class SplashInitializeLiveData : LiveData<SplashNavigationEvent>() {
        override fun onActive() {
            super.onActive()
        }

        override fun onInactive() {
            super.onInactive()
        }

        private fun doSplashInitialize() {
            EngageService.initService(BuildConfig.VERSION_CODE.toString())

            Handler().postDelayed({

                val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication())

                if (!defaultSharedPrefs.getBoolean("SHARED_PREFS_VIEW_GET_STARTED_KEY", false)) {
                    value = SplashNavigationEvent.FIRST_USE
                } else {
                    if (EngageService.getInstance().authManager.isLoggedIn) {
                        compositeDisposable.add(
                                EngageService.getInstance().loginResponseAsObservable
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ response ->
                                            if (response.isSuccess && response is LoginResponse) {
                                                value = SplashNavigationEvent.LOGGED_IN
                                            } else {
                                                value = SplashNavigationEvent.NOT_LOGGED_IN
                                            }
//                                            if (response.isSuccess() && response.getClass().equals(LoginResponse::class.java)) {
//                                                startActivity(OverviewActivity::class.java)
//                                            } else {
//                                                EngageService.getInstance().authManager.logout()
//                                                startActivity(LoginActivity::class.java)
//                                            }
                                        }, { e ->
//                                            EngageService.getInstance().authManager.logout()
//                                            startActivity(LoginActivity::class.java)
                                        })
                        )
                    } else {
                        value = SplashNavigationEvent.NOT_LOGGED_IN
                    }
                }
            }, 2000)
        }
    }
}