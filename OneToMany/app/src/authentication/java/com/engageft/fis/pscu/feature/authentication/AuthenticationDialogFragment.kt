package com.engageft.fis.pscu.feature.authentication

import android.app.Dialog
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.setTextSizeAndFont
import com.engageft.apptoolbox.util.showKeyboard
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.BaseEngageDialogFragment

/**
 * AuthenticationDialogFragment
 * <p>
 * Authentication dialog baseFragmentIm that can prompt for fingerprint auth, passcode, or password.
 *
 * </p>
 * Created by kurteous on 12/1/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthenticationDialogFragment : BaseEngageDialogFragment() {

    private var message: CharSequence? = null
    private var styleResId: Int = NOT_SET
    private var dialogWidthStyleResId: Int = NOT_SET

    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var passwordPasscodeEditText: EditText
    private lateinit var errorTextView: TextView
    private lateinit var buttonPositive: Button
    private lateinit var buttonNeutral: Button
    private lateinit var buttonNegative: Button

    var titleTextSizeAndFont: Pair<Float?, Typeface?>? = null
    var messageTextSizeAndFont: Pair<Float?, Typeface?>? = null
    var errorTextSizeAndFont: Pair<Float?, Typeface?>? = null
    var positiveButtonTextSizeAndFont: Pair<Float?, Typeface?>? = null
    var neutralButtonTextSizeAndFont: Pair<Float?, Typeface?>? = null
    var negativeButtonTextSizeAndFont: Pair<Float?, Typeface?>? = null

    @ColorInt
    var titleTextColor: Int = NOT_SET
    @ColorInt
    var messageTextColor: Int = NOT_SET
    @ColorInt
    var errorTextColor: Int = NOT_SET
    @ColorInt
    var positiveButtonTextColor: Int = NOT_SET
    @ColorInt
    var neutralButtonTextColor: Int = NOT_SET
    @ColorInt
    var negativeButtonTextColor: Int = NOT_SET

    private var authenticationSuccessFunction: (() -> Unit)? = null

    private lateinit var viewModel: AuthenticationDialogViewModel

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(AuthenticationDialogViewModel::class.java)
        return viewModel
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.apply {
            message = getCharSequence(AuthenticationDialogFragment.ARG_MESSAGE)
            styleResId = getInt(AuthenticationDialogFragment.ARG_STYLE_RES)
            dialogWidthStyleResId = getInt(AuthenticationDialogFragment.ARG_DIALOG_WIDTH_STYLE_RES)
        }

        val builder = AlertDialog.Builder(context!!, dialogWidthStyleResId)
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_fragment_authentication, null)

        view.apply {
            titleTextView = findViewById(R.id.titleTextView)
            messageTextView = findViewById(R.id.messageTextView)
            passwordPasscodeEditText = findViewById(R.id.passwordPasscodeEditText)
            errorTextView = findViewById(R.id.errorTextView)
            buttonPositive = findViewById(R.id.buttonPositive)
            buttonNeutral = findViewById(R.id.buttonNeutral)
            buttonNegative = findViewById(R.id.buttonNegative)

            builder.setView(this)
        }

        if (!message.isNullOrEmpty()) {
            messageTextView.text = message
        } else {
            messageTextView.visibility = View.GONE
        }

        buttonPositive.setOnClickListener {
            when (viewModel.authMethodObservable.value) {
                AuthenticationDialogViewModel.AuthMethod.BIOMETRIC -> viewModel.authenticateBiometric()
                AuthenticationDialogViewModel.AuthMethod.PASSCODE -> viewModel.authenticatePasscode("test")
                AuthenticationDialogViewModel.AuthMethod.PASSWORD -> viewModel.authenticatePassword(passwordPasscodeEditText.text.toString())
            }
        }

        buttonNeutral.setOnClickListener {
            viewModel.tryNextAuthMethod()
        }

        buttonNegative.setOnClickListener {
            dismiss()
        }

        viewModel = ViewModelProviders.of(this).get(AuthenticationDialogViewModel::class.java)

        viewModel.apply {
            authMethodObservable.observe(this@AuthenticationDialogFragment, Observer { authMethod ->
                when (authMethod) {
                    AuthenticationDialogViewModel.AuthMethod.BIOMETRIC -> {
                        // TODO(kurt): configure view for fingerprint auth
                    }
                    AuthenticationDialogViewModel.AuthMethod.PASSCODE -> {
                        titleTextView.text = String.format(
                                getString(R.string.auth_dialog_title_password_passcode_format),
                                getString(R.string.auth_dialog_title_passcode),
                                username
                        )
                        passwordPasscodeEditText.text = null
                        passwordPasscodeEditText.hint = getString(R.string.auth_dialog_input_hint_passcode)
                        passwordPasscodeEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        passwordPasscodeEditText.requestFocus()
                        buttonNeutral.text = getString(R.string.auth_dialog_button_forgot_passcode)

                        Handler().post { passwordPasscodeEditText.showKeyboard() }
                    }
                    AuthenticationDialogViewModel.AuthMethod.PASSWORD -> {
                        titleTextView.text = String.format(
                                getString(R.string.auth_dialog_title_password_passcode_format),
                                getString(R.string.auth_dialog_title_password),
                                username
                        )
                        passwordPasscodeEditText.text = null
                        passwordPasscodeEditText.hint = getString(R.string.auth_dialog_input_hint_password)
                        passwordPasscodeEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        passwordPasscodeEditText.requestFocus()
                        buttonNeutral.text = getString(R.string.auth_dialog_button_forgot_password)

                        Handler().post { passwordPasscodeEditText.showKeyboard() }
                    }
                }
            })

            authEventObservable.observe(this@AuthenticationDialogFragment, Observer { authEvent ->
                when (authEvent) {
                    AuthenticationDialogViewModel.AuthEvent.SUCCESS -> {
                        authenticationSuccessFunction?.invoke()
                        dismiss()
                    }
                    AuthenticationDialogViewModel.AuthEvent.RESET_PASSWORD -> {
                        // TODO(kurt) show password reset dialog
                        Toast.makeText(context, "TODO: prompt to reset password", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            })

            errorMessageObservable.observe(this@AuthenticationDialogFragment, Observer { errorMessage ->
                if (errorMessage.isNullOrBlank()) {
                    errorTextView.visibility = View.INVISIBLE
                    errorTextView.text = "" // just to be safe
                } else {
                    errorTextView.text = errorMessage
                    errorTextView.visibility = View.VISIBLE
                }
            })

            dialogInfoObservable.observe(this@AuthenticationDialogFragment, Observer { dialogInfo ->
                dialogInfo?.apply {
                    errorTextView.text = message
                    errorTextView.visibility = View.VISIBLE
                }
            })
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()

        if (styleResId != NOT_SET) {
            applyStyle(styleResId)
        }

        titleTextSizeAndFont?.let{titleTextView.setTextSizeAndFont(it)}
        messageTextSizeAndFont?.let{messageTextView.setTextSizeAndFont(it)}
        errorTextSizeAndFont?.let{errorTextView.setTextSizeAndFont(it)}
        positiveButtonTextSizeAndFont?.let{buttonPositive.setTextSizeAndFont(it)}
        neutralButtonTextSizeAndFont?.let{buttonNeutral.setTextSizeAndFont(it)}
        negativeButtonTextSizeAndFont?.let{buttonNegative.setTextSizeAndFont(it)}

        if (titleTextColor != NOT_SET) titleTextView.setTextColor(titleTextColor)
        if (messageTextColor != NOT_SET) messageTextView.setTextColor(messageTextColor)
        if (errorTextColor != NOT_SET) errorTextView.setTextColor(errorTextColor)
        if (positiveButtonTextColor != NOT_SET) buttonPositive.setTextColor(positiveButtonTextColor)
        if (neutralButtonTextColor != NOT_SET) buttonNeutral.setTextColor(neutralButtonTextColor)
        if (negativeButtonTextColor != NOT_SET) buttonNegative.setTextColor(negativeButtonTextColor)
    }

    private fun applyStyle(@StyleRes styleResId: Int) {
        val a = context!!.obtainStyledAttributes(styleResId, R.styleable.DialogAuthentication)

        val titleTextAppearance = a?.getResourceId(R.styleable.DialogAuthentication_titleTextAppearance, NOT_SET) ?: NOT_SET
        val messageTextAppearance = a?.getResourceId(R.styleable.DialogAuthentication_messageTextAppearance, NOT_SET) ?: NOT_SET
        val errorMessageTextAppearance = a?.getResourceId(R.styleable.DialogAuthentication_errorMessageTextAppearance, NOT_SET) ?: NOT_SET
        val messageLineSpacing = a?.getDimension(R.styleable.DialogAuthentication_messageLineSpacing, NOT_SET_FLOAT) ?: NOT_SET_FLOAT
        val buttonPositiveAppearance = a?.getResourceId(R.styleable.DialogAuthentication_buttonPositiveTextAppearance, NOT_SET) ?: NOT_SET
        val buttonNeutralAppearance = a?.getResourceId(R.styleable.DialogAuthentication_buttonNeutralTextAppearance, NOT_SET) ?: NOT_SET
        val buttonNegativeAppearance = a?.getResourceId(R.styleable.DialogAuthentication_buttonNegativeTextAppearance, NOT_SET) ?: NOT_SET
        val buttonBackground = a?.getResourceId(R.styleable.DialogAuthentication_buttonBackground, NOT_SET) ?: NOT_SET

        a.recycle()

        if (titleTextAppearance != NOT_SET) {
            setTitleTextAppearance(titleTextAppearance)
        }

        if (messageTextAppearance != NOT_SET) {
            setMessageTextAppearance(messageTextAppearance)
        }

        if (errorMessageTextAppearance != NOT_SET) {
            setErrorTextAppearance(errorMessageTextAppearance)
        }

        if (messageLineSpacing != NOT_SET_FLOAT) {
            titleTextView.setLineSpacing(messageLineSpacing, AuthenticationDialogFragment.LINE_SPACE_MULTIPLIER)
            messageTextView.setLineSpacing(messageLineSpacing, AuthenticationDialogFragment.LINE_SPACE_MULTIPLIER)
            errorTextView.setLineSpacing(messageLineSpacing, AuthenticationDialogFragment.LINE_SPACE_MULTIPLIER)
        }

        if (buttonPositiveAppearance != NOT_SET) {
            setPositiveButtonTextAppearance(buttonPositiveAppearance)
        }

        if (buttonNeutralAppearance != NOT_SET) {
            setTextAppearance(buttonNeutral, buttonNeutralAppearance)
        }

        if (buttonNeutralAppearance != NOT_SET) {
            setNeutralButtonTextAppearance(buttonNeutralAppearance)
        }

        if (buttonNegativeAppearance != NOT_SET) {
            setNegativeButtonTextAppearance(buttonNegativeAppearance)
        }

        if (buttonBackground != NOT_SET) {
            val buttonBackgroundDrawable = ContextCompat.getDrawable(context!!, buttonBackground)
            buttonPositive.background = buttonBackgroundDrawable
            buttonNeutral.background = buttonBackgroundDrawable
            buttonNegative.background = buttonBackgroundDrawable
        }
    }

    fun setPositiveButtonTextAppearance(@StyleRes buttonPositiveAppearance: Int) {
        setTextAppearance(buttonPositive, buttonPositiveAppearance)
    }

    fun setNeutralButtonTextAppearance(@StyleRes buttonNeutralAppearance: Int) {
        setTextAppearance(buttonNeutral, buttonNeutralAppearance)
    }

    fun setNegativeButtonTextAppearance(@StyleRes buttonNegativeAppearance: Int) {
        setTextAppearance(buttonNegative, buttonNegativeAppearance)
    }

    fun setMessageTextAppearance(@StyleRes messageTextAppearance: Int) {
        setTextAppearance(messageTextView, messageTextAppearance)
    }

    fun setTitleTextAppearance(@StyleRes titleTextAppearance: Int) {
        setTextAppearance(titleTextView, titleTextAppearance)
    }

    fun setErrorTextAppearance(@StyleRes errorTextAppearance: Int) {
        setTextAppearance(errorTextView, errorTextAppearance)
    }

    private fun setTextAppearance(view: TextView, @StyleRes style: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setTextAppearance(style)
        } else {
            @Suppress("DEPRECATION")
            view.setTextAppearance(context, style)
        }
    }

    companion object {
        const val TAG = "AuthenticationDialogFragment"

        private const val LINE_SPACE_MULTIPLIER = 1f
        private const val NOT_SET = -1
        private const val NOT_SET_FLOAT = -1f

        private const val ARG_MESSAGE = "ARG_MESSAGE"
        private const val ARG_STYLE_RES = "ARG_STYLE_RES"
        private const val ARG_DIALOG_WIDTH_STYLE_RES = "ARG_DIALOG_WIDTH_STYLE_RES"

        fun newInstance(message: CharSequence, authenticationSuccessFunction: () -> Unit): AuthenticationDialogFragment {
            val dialogFragment = AuthenticationDialogFragment()

            val args = Bundle()
            args.putCharSequence(ARG_MESSAGE, message)
            args.putInt(ARG_STYLE_RES, R.style.DialogAuthenticationStyle)
            args.putInt(ARG_DIALOG_WIDTH_STYLE_RES, R.style.DialogAuthenticationWidthStyle)
            dialogFragment.arguments = args

            dialogFragment.authenticationSuccessFunction = authenticationSuccessFunction

            return dialogFragment
        }
    }
}