package com.engageft.feature.authentication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.ProgressOverlayDelegate
import com.engageft.onetomany.NotAuthenticatedActivity
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.DialogExpiredPasswordBinding

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

    private val progressOverlayStyle: Int = com.engageft.apptoolbox.R.style.LoadingOverlayDialogStyle
    protected lateinit var progressOverlayDelegate: ProgressOverlayDelegate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        authExpiredViewModel = ViewModelProviders.of(this).get(AuthExpiredViewModel::class.java)
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
                AuthExpiredViewModel.AuthExpiredNavigationEvent.LOGIN_ERROR -> {
                    // TODO(jhutcihns): How to show error.
                }
                AuthExpiredViewModel.AuthExpiredNavigationEvent.LOGIN_SUCCESS -> {
                    reauthenticationSucceeded()
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressOverlayDelegate = ProgressOverlayDelegate(progressOverlayStyle, this, authExpiredViewModel)
    }
}