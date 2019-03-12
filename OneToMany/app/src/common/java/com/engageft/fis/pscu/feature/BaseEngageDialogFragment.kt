package com.engageft.fis.pscu.feature

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.engageft.apptoolbox.BaseFragmentDelegate
import com.engageft.apptoolbox.LotusDialogFragment
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * BaseEngageDialogFragment
 * <p>
 * Defines additional abstraction for a DialogFragment that handles shared features with the BaseEngagePageFragment
 * and BaseEngageSubFragment.
 *
 * These features are defined in the BaseEngageFragmentDelegate.
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
        fragmentDelegate.progressOverlay.apply {
            progressBarTint = ColorStateList.valueOf(Palette.infoColor)
        }
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