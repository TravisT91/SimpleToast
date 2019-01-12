package com.engageft.fis.pscu

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.LotusApplication
import com.engageft.apptoolbox.R
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.branding.Palette
import io.fabric.sdk.android.Fabric

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

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        NotificationUtils.createNotificationChannels(this)

        HeapUtils.initHeap(this)
        MoEngageUtils.initMoEngage()
        EngageService.initService(BuildConfig.VERSION_CODE.toString(), this, EngageAppConfig.engageKitConfig)

        initPalette()

        Fabric.with(this, Crashlytics())
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

}