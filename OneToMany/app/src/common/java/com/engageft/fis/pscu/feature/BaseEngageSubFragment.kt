package com.engageft.fis.pscu.feature

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.engageft.apptoolbox.BaseFragmentDelegate
import com.engageft.apptoolbox.LotusSubFragment
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * BaseEngageSubFragment
 * <p>
 * Defines additional abstraction for a SubFragment that handles shared features with the BaseEngagePageFragment
 * and BaseEngageDialogFragment.
 *
 * These features are defined in the BaseEngageFragmentDelegate.
 * </p>
 * Created by joeyhutchins on 1/8/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
abstract class BaseEngageSubFragment : LotusSubFragment(), BaseEngageFragmentIm {
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