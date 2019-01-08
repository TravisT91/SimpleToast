package com.engageft.fis.pscu.feature

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.engageft.apptoolbox.BaseFragmentDelegate
import com.engageft.apptoolbox.LotusDialogFragment

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 1/8/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
abstract class BaseEngageDialogFragment : LotusDialogFragment(), BaseEngageFragmentIm {
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