package com.engageft.onetomany

import android.content.Context
import com.engageft.onetomany.config.EngageAppConfig
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
    private val heapAppId: String = EngageAppConfig.heapAppId
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