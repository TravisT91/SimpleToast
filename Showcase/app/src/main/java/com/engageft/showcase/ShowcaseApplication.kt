package com.engageft.showcase

import com.engageft.apptoolbox.LotusApplication
import com.engageft.engagekit.EngageService
import com.heapanalytics.android.Heap

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/21/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ShowcaseApplication : LotusApplication() {
    companion object {
        lateinit var sInstance: ShowcaseApplication
    }
    override val navigationType: NavigationType = NavigationType.SIDE

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        Heap.init(this, "TEMP ID")
        EngageService.initService(BuildConfig.VERSION_CODE.toString(), this)
    }
}