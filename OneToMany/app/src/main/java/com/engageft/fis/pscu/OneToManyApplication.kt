package com.engageft.fis.pscu

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.LotusApplication
import com.engageft.apptoolbox.R
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.auth.AuthState
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.WelcomeSharedPreferencesRepo
import com.engageft.fis.pscu.feature.branding.BrandingManager
import com.engageft.fis.pscu.feature.branding.Palette
import com.ob.ws.dom.LoginResponse
import io.fabric.sdk.android.Fabric
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * OneToManyApplication
 * <p>
 * Application for My Card Manager.
 * </p>
 * Created by joeyhutchins on 8/21/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class OneToManyApplication : LotusApplication() {
    companion object {
        lateinit var sInstance: OneToManyApplication
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        NotificationUtils.createNotificationChannels(this)

        HeapUtils.initHeap(this)
        MoEngageUtils.initMoEngage()
        EngageService.initService(BuildConfig.VERSION_CODE.toString(), this, EngageAppConfig.engageKitConfig, isTestBuild())
        Log.i("OneToManyApplication", "isTestBuild? ${isTestBuild()}")

        initPalette()

        Fabric.with(this, Crashlytics())

        EngageService.getInstance().authManager.authStateObservable.observeForever { authState ->
            when (authState) {
                is AuthState.LoggedIn -> {
                    // set the Get started flag to true after the successful login, so the Welcome screen doesn't get displayed again
                    WelcomeSharedPreferencesRepo.applyHasSeenGetStarted(true)

                    initAnalytics()
                }
                is AuthState.LoggedOut -> {
                    MoEngageUtils.logout()
                    BrandingManager.clearBranding()
                }
                is AuthState.Expired -> {

                }
            }
        }
    }

    private fun initAnalytics() {
        compositeDisposable.add(
            EngageService.getInstance().loginResponseAsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { response ->
                                if (response.isSuccess && response is LoginResponse) {
                                    val accountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                                    if (accountInfo != null && accountInfo.accountId != 0L) {
                                        // Setup unique user identifier for Heap analytics
                                        HeapUtils.identifyUser(accountInfo.accountId.toString())
                                        // Setup user attributes for MoEngage
                                        MoEngageUtils.setUserAttributes(accountInfo)
                                    }
                                } else {
                                    Crashlytics.logException(IllegalStateException("handleUnexpectedErrorResponse: " + response.message))
                                }
                            }, { e ->
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace()
                        }
                        Crashlytics.logException(e)
                    })
        )
    }

    private fun initPalette(){
        Palette.initPaletteColors(
            primaryColor = ContextCompat.getColor(this@OneToManyApplication, R.color.primary),
            secondaryColor = ContextCompat.getColor(this@OneToManyApplication, R.color.secondary),
            successColor = ContextCompat.getColor(this@OneToManyApplication, R.color.success),
            warningColor = ContextCompat.getColor(this@OneToManyApplication, R.color.warning),
            errorColor = ContextCompat.getColor(this@OneToManyApplication, R.color.error),
            infoColor = ContextCompat.getColor(this@OneToManyApplication, R.color.info)
        )

        Palette.initFonts(
            font_regular = ResourcesCompat.getFont(this@OneToManyApplication, R.font.font_regular),
            font_bold = ResourcesCompat.getFont(this@OneToManyApplication, R.font.font_bold),
            font_italic = ResourcesCompat.getFont(this@OneToManyApplication, R.font.font_italic),
            font_light = ResourcesCompat.getFont(this@OneToManyApplication, R.font.font_light),
            font_medium = ResourcesCompat.getFont(this@OneToManyApplication, R.font.font_medium)
        )
    }

    private fun isTestBuild(): Boolean {
        return BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "demo"
    }
}