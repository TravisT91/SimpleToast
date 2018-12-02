package com.engageft.fis.pscu.feature.authentication

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StyleRes
import com.engageft.apptoolbox.R
import com.engageft.apptoolbox.view.BaseTitleMessageDialogFragment
import java.lang.ref.WeakReference

/**
 * AuthenticationDialogFragment
 * <p>
 * Authentication dialog fragment that can prompt for fingerprint auth, passcode, or password.
 *
 * </p>
 * Created by kurteous on 12/1/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthenticationDialogFragment : BaseTitleMessageDialogFragment() {

    private var listener: WeakReference<AuthenticationDialogFragmentListener>? = null

    private lateinit var viewModel: AuthenticationDialogViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        arguments?.apply {

        }

        buttonNegative.setOnClickListener {
            dismiss()
            listener?.get()?.onDialogFragmentNegativeButtonClicked()
        }

        buttonNeutral.setOnClickListener {
            dismiss()
            listener?.get()?.onDialogFragmentNeutralButtonClicked()
        }

        buttonPositive.setOnClickListener {
            when (viewModel.authMethodObservable.value) {
                AuthenticationDialogViewModel.AuthMethod.BIOMETRIC -> viewModel.authenticateBiometric()
                AuthenticationDialogViewModel.AuthMethod.PASSCODE -> viewModel.authenticatePasscode("test")
                AuthenticationDialogViewModel.AuthMethod.PASSWORD -> 
            }
        }

        return dialog
    }


    private fun setListener(listener: AuthenticationDialogFragment.AuthenticationDialogFragmentListener?) {
        listener?.let {
            this.listener = WeakReference(it)
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        listener?.get()?.onDialogCancelled()
    }

    interface AuthenticationDialogFragmentListener {
        fun onAuthenticationSuccess()
        fun onDialogFragmentNeutralButtonClicked()
        fun onDialogFragmentNegativeButtonClicked()
        fun onDialogCancelled()
    }

    companion object {

        fun newInstance(listener: AuthenticationDialogFragmentListener): AuthenticationDialogFragment {

            val dialogFragment = AuthenticationDialogFragment()

            val args = Bundle()
            args.putInt(ARG_STYLE_RES, R.style.DialogAuthenticationStyle)
            args.putInt(ARG_DIALOG_WIDTH_STYLE_RES, R.style.DialogAuthenticationWidthStyle)
            dialogFragment.arguments = args

            dialogFragment.setListener(listener)

            return dialogFragment
        }
    }
}