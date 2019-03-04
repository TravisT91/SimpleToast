package com.engageft.fis.pscu.feature.authentication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.NotAuthenticatedActivity
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.DialogExpiredPasswordBinding

/**
 * AuthExpiredPasswordDialog
 * <p>
 * AuthExpiredDialog implementation to be used when the user is authenticating via password.
 * </p>
 * Created by joeyhutchins on 11/6/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AuthExpiredPasswordDialog : BaseAuthExpiredDialog() {
    private lateinit var authExpiredViewModel: AuthExpiredViewModel
    private lateinit var binding: DialogExpiredPasswordBinding

    override fun createViewModel(): BaseViewModel? {
        authExpiredViewModel = ViewModelProviders.of(this).get(AuthExpiredViewModel::class.java)
        return authExpiredViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_expired_password, container, false)
        binding.viewModel = authExpiredViewModel

        authExpiredViewModel.navigationObservable.observe(this, Observer { navigationEvent: AuthExpiredViewModel.AuthExpiredNavigationEvent ->
            when (navigationEvent) {
                AuthExpiredViewModel.AuthExpiredNavigationEvent.NONE -> {
                    // Do nothing
                }
                AuthExpiredViewModel.AuthExpiredNavigationEvent.LOGOUT -> {
                    activity!!.finish()
                    startActivity(Intent(context, NotAuthenticatedActivity::class.java))
                }
                AuthExpiredViewModel.AuthExpiredNavigationEvent.FORGOT_PASSWORD -> {
                    // TODO(jhutchins): Forgot password flow
                }
                AuthExpiredViewModel.AuthExpiredNavigationEvent.LOGIN_SUCCESS -> {
                    reauthenticationSucceeded()
                }
            }
        })

        authExpiredViewModel.loginButtonStateObservable.observe(this, Observer { buttonState ->
            when (buttonState) {
                AuthExpiredViewModel.LoginButtonState.GONE -> {
//                    binding.buttonSignIn.visibility = View.GONE
                }
                AuthExpiredViewModel.LoginButtonState.VISIBLE_ENABLED -> {
//                    binding.buttonSignIn.visibility = View.VISIBLE
                }
            }
        })

        binding.passwordInput.setImeOptions(EditorInfo.IME_ACTION_GO)
        binding.passwordInput.onImeAction(EditorInfo.IME_ACTION_GO) { authExpiredViewModel.onSignInClicked() }

        return binding.root
    }
}