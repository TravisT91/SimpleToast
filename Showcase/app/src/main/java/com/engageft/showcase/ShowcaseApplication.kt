package com.engageft.showcase

import com.engageft.apptoolbox.LotusApplication
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
    override val navigationType: NavigationType = NavigationType.SIDE

    override fun onCreate() {
        super.onCreate()

        Heap.init(this, "TEMP ID")
    }
}