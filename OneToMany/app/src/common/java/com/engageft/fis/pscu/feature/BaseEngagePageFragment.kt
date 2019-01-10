package com.engageft.fis.pscu.feature

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.engageft.apptoolbox.BaseFragmentDelegate
import com.engageft.apptoolbox.LotusPageFragment

/**
 * BaseEngagePageFragment
 * <p>
 * Defines additional abstraction for a PageFragment that handles shared features with the BaseEngageDialogFragment
 * and BaseEngageSubFragment.
 *
 * These features are defined in the BaseEngageFragmentDelegate.
 * </p>
 * Created by joeyhutchins on 11/15/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseEngagePageFragment : LotusPageFragment(), BaseEngageFragmentIm {
    protected val engageFragmentDelegate by lazy {
        BaseEngageFragmentDelegate(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        engageFragmentDelegate
    }

    override fun getAndroidContext(): Context {
        return context!!
    }

    override fun getBaseFragmentDelegate(): BaseFragmentDelegate {
        return fragmentDelegate
    }

    override fun getAndroidLifecycleOwner(): LifecycleOwner {
        return this
    }

    override fun getAndroidLifecycle(): Lifecycle {
        return lifecycle
    }
}