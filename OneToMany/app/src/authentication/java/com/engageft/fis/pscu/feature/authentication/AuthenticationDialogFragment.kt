package com.engageft.fis.pscu.feature.authentication

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.view.SafeDialogFragment
import com.engageft.fis.pscu.R
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
class AuthenticationDialogFragment : SafeDialogFragment() {

    private var message: String? = null
    private var styleResId: Int = NOT_SET
    private var dialogWidthStyleResId: Int = NOT_SET

    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var passwordEditText: EditText
    private lateinit var errorTextView: TextView
    private lateinit var buttonNegative: Button
    private lateinit var buttonNeutral: Button
    private lateinit var buttonPositive: Button

    private var listener: WeakReference<AuthenticationDialogFragmentListener>? = null
    private var authenticationSuccessHandler: (() -> Unit)? = null

    private lateinit var viewModel: AuthenticationDialogViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.apply {
            message = getString(AuthenticationDialogFragment.ARG_MESSAGE)
            styleResId = getInt(AuthenticationDialogFragment.ARG_STYLE_RES)
            dialogWidthStyleResId = getInt(AuthenticationDialogFragment.ARG_DIALOG_WIDTH_STYLE_RES)
        }

        val builder = AlertDialog.Builder(context!!, dialogWidthStyleResId)
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_fragment_authentication, null)

        view.apply {
            titleTextView = this.findViewById(R.id.titleTextView)
            messageTextView = this.findViewById(R.id.messageTextView)
            passwordEditText = this.findViewById(R.id.passwordEditText)
            errorTextView = this.findViewById(R.id.errorTextView)
            buttonNegative = this.findViewById(R.id.buttonNegative)
            buttonNeutral = this.findViewById(R.id.buttonNeutral)
            buttonPositive = this.findViewById(R.id.buttonPositive)

            builder.setView(this)
        }

        if (!message.isNullOrEmpty()) {
            messageTextView.text = message
        } else {
            messageTextView.visibility = View.GONE
        }

        buttonNegative.setOnClickListener {
            dismiss()
            listener?.get()?.onAuthenticationCancelled()
        }

        buttonNeutral.setOnClickListener {
            //dismiss()
            //listener?.get()?.onDialogFragmentNeutralButtonClicked()
        }

        buttonPositive.setOnClickListener {
            when (viewModel.authMethodObservable.value) {
                AuthenticationDialogViewModel.AuthMethod.BIOMETRIC -> viewModel.authenticateBiometric()
                AuthenticationDialogViewModel.AuthMethod.PASSCODE -> viewModel.authenticatePasscode("test")
                AuthenticationDialogViewModel.AuthMethod.PASSWORD -> viewModel.authenticatePassword(passwordEditText.text.toString())
            }
        }

        viewModel = ViewModelProviders.of(this).get(AuthenticationDialogViewModel::class.java)

        viewModel.authMethodObservable.observe(this, Observer<AuthenticationDialogViewModel.AuthMethod> { authMethod ->
            when (authMethod) {
                AuthenticationDialogViewModel.AuthMethod.BIOMETRIC -> {
                    
                }
                AuthenticationDialogViewModel.AuthMethod.PASSCODE -> {
                    
                }
                AuthenticationDialogViewModel.AuthMethod.PASSWORD -> {
                    titleTextView.text = String.format(getString(R.string.auth_dialog_title_password_passcode_format), getString(R.string.auth_dialog_title_password), viewModel.usernameObservable.value!!)
                    passwordEditText.visibility = View.VISIBLE

                    buttonNeutral.text = getString(R.string.auth_dialog_button_forgot_password)
                }
            }
        })
        
        viewModel.authEventObservable.observe(this, Observer<AuthenticationDialogViewModel.AuthEvent> { authEvent ->
            when (authEvent) {
                AuthenticationDialogViewModel.AuthEvent.SUCCESS -> {
                    authenticationSuccessHandler?.invoke()
                    dismiss()
                }
                AuthenticationDialogViewModel.AuthEvent.FAILURE -> {
                    
                }
            }
        })
        
        viewModel.errorMessageObservable.observe(this, Observer<String> { errorMessage ->
            if (errorMessage.isNullOrBlank()) {
                errorTextView.visibility = View.GONE
                errorTextView.text = "" // just to be safe
            } else {
                errorTextView.text = errorMessage
                errorTextView.visibility = View.VISIBLE
            }
        })

        return builder.create()
    }

    override fun onStart() {
        super.onStart()

        if (styleResId != NOT_SET) {
            applyStyle(styleResId)
        }
    }

    private fun applyStyle(@StyleRes styleResId: Int) {
        val a = context!!.obtainStyledAttributes(styleResId, R.styleable.DialogAuthentication)

        val titleTextAppearance = a?.getResourceId(R.styleable.DialogAuthentication_titleTextAppearance, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET
        val messageTextAppearance = a?.getResourceId(R.styleable.DialogAuthentication_messageTextAppearance, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET
        val errorAppearance = a?.getResourceId(R.styleable.DialogAuthentication_errorTextAppearance, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET
        val messageLineSpacing = a?.getDimension(R.styleable.DialogAuthentication_messageLineSpacing, AuthenticationDialogFragment.NOT_SET_FLOAT) ?: AuthenticationDialogFragment.NOT_SET_FLOAT
        val buttonNegativeAppearance = a?.getResourceId(R.styleable.DialogAuthentication_buttonNegativeTextAppearance, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET
        val buttonNeutralAppearance = a?.getResourceId(R.styleable.DialogAuthentication_buttonNeutralTextAppearance, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET
        val buttonPositiveAppearance = a?.getResourceId(R.styleable.DialogAuthentication_buttonPositiveTextAppearance, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET
        val buttonBackground = a?.getResourceId(R.styleable.DialogAuthentication_buttonBackground, AuthenticationDialogFragment.NOT_SET) ?: AuthenticationDialogFragment.NOT_SET

        a.recycle()

        if (titleTextAppearance != AuthenticationDialogFragment.NOT_SET) {
            setTextAppearance(titleTextView, titleTextAppearance)
        }

        if (messageTextAppearance != AuthenticationDialogFragment.NOT_SET) {
            setTextAppearance(messageTextView, messageTextAppearance)
        }

        if (errorAppearance != AuthenticationDialogFragment.NOT_SET) {
            setTextAppearance(errorTextView, errorAppearance)
        }

        if (messageLineSpacing != AuthenticationDialogFragment.NOT_SET_FLOAT) {
            messageTextView.setLineSpacing(messageLineSpacing, AuthenticationDialogFragment.LINE_SPACE_MULTIPLIER)
            errorTextView.setLineSpacing(messageLineSpacing, AuthenticationDialogFragment.LINE_SPACE_MULTIPLIER)
        }

        if (buttonNegativeAppearance != AuthenticationDialogFragment.NOT_SET) {
            setTextAppearance(buttonNegative, buttonNegativeAppearance)
        }

        if (buttonNeutralAppearance != AuthenticationDialogFragment.NOT_SET) {
            setTextAppearance(buttonNeutral, buttonNeutralAppearance)
        }

        if (buttonPositiveAppearance != AuthenticationDialogFragment.NOT_SET) {
            setTextAppearance(buttonPositive, buttonPositiveAppearance)
        }

        if (buttonBackground != AuthenticationDialogFragment.NOT_SET) {
            val buttonBackgroundDrawable = ContextCompat.getDrawable(context!!, buttonBackground)
            buttonNegative.background = buttonBackgroundDrawable
            buttonNeutral.background = buttonBackgroundDrawable
            buttonPositive.background = buttonBackgroundDrawable
        }
    }

    private fun setTextAppearance(view: TextView, @StyleRes style: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setTextAppearance(style)
        } else {
            @Suppress("DEPRECATION")
            view.setTextAppearance(context, style)
        }
    }

    private fun setListener(listener: AuthenticationDialogFragment.AuthenticationDialogFragmentListener?) {
        listener?.let {
            this.listener = WeakReference(it)
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        listener?.get()?.onAuthenticationCancelled()
    }

    interface AuthenticationDialogFragmentListener {
        fun onAuthenticationSuccess()
        fun onAuthenticationCancelled()
    }

    companion object {

        private const val LINE_SPACE_MULTIPLIER = 1f
        private const val NOT_SET = -1
        private const val NOT_SET_FLOAT = -1f

        private const val ARG_MESSAGE = "ARG_MESSAGE"
        private const val ARG_STYLE_RES = "ARG_STYLE_RES"
        private const val ARG_DIALOG_WIDTH_STYLE_RES = "ARG_DIALOG_WIDTH_STYLE_RES"

        fun newInstance(listener: AuthenticationDialogFragmentListener): AuthenticationDialogFragment {

            val dialogFragment = AuthenticationDialogFragment()

            val args = Bundle()
            args.putInt(ARG_STYLE_RES, R.style.DialogAuthenticationStyle)
            args.putInt(ARG_DIALOG_WIDTH_STYLE_RES, R.style.DialogAuthenticationWidthStyle)
            dialogFragment.arguments = args

            dialogFragment.setListener(listener)

            return dialogFragment
        }

        fun newInstance(message: String, authenticationSuccessHandler: () -> Unit): AuthenticationDialogFragment {

            val dialogFragment = AuthenticationDialogFragment()

            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            args.putInt(ARG_STYLE_RES, R.style.DialogAuthenticationStyle)
            args.putInt(ARG_DIALOG_WIDTH_STYLE_RES, R.style.DialogAuthenticationWidthStyle)
            dialogFragment.arguments = args

            dialogFragment.authenticationSuccessHandler = authenticationSuccessHandler

            return dialogFragment
        }
    }
}