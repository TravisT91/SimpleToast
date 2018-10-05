package com.engageft.showcase.feature

import android.content.Context
import com.engageft.engagekit.utils.DeviceUtils
import com.engageft.showcase.BuildConfig
import com.heapanalytics.android.Heap

/**
 *  HeapUtils
 *  </p>
 *  Provides helper functions for initializing Heap analytics and managing user identity
 *  </p>
 *  Created by Kurt Mueller on 7/25/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
object HeapUtils {
    private val heapAppId: String = if (BuildConfig.DEBUG || DeviceUtils.isEmulator()) "" else ""
    private val isHeapEnabled = heapAppId.isNotBlank()

    fun initHeap(context: Context) {
        if (isHeapEnabled) {
            Heap.init(context, heapAppId)
        }
    }

    fun identifyUser(userIdentifier: String) {
        if (isHeapEnabled && !userIdentifier.isBlank()) {
            Heap.identify(userIdentifier)
        }
    }
}