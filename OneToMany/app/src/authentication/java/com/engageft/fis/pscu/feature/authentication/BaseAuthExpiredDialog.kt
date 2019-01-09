package com.engageft.fis.pscu.feature.authentication

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.BaseEngageDialogFragment

/**
 * BaseAuthExpiredDialog
 * <p>
 * Base dialog for Auth expiration fragments to inherit from that contains shared functionality all dialogs
 * must have.
 * </p>
 * Created by joeyhutchins on 11/7/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseAuthExpiredDialog : BaseEngageDialogFragment() {
    private var reauthenticationSucceeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isCancelable = false
        // Use no frame, no title, etc.
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LotusTheme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!reauthenticationSucceeded) {
            EngageService.getInstance().authManager.logout()
        }
    }

    override fun dismiss() {
        if (!reauthenticationSucceeded) {
            throw UnsupportedOperationException("Call reauthenticationSucceeded instead!")
        } else {
            super.dismiss()
        }
    }

    protected fun reauthenticationSucceeded() {
        reauthenticationSucceeded = true
        dismiss()
    }
}