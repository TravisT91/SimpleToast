package com.engageft.fis.pscu.feature.authentication

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
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.NotAuthenticatedActivity
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.DialogExpiredPasswordBinding
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.infoDialogGenericErrorTitleMessageConditionalNewInstance
import com.engageft.fis.pscu.feature.infoDialogGenericErrorTitleMessageNewInstance

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
                AuthExpiredViewModel.AuthExpiredNavigationEvent.LOGIN_SUCCESS -> {
                    reauthenticationSucceeded()
                }
            }
        })

        authExpiredViewModel.loginButtonStateObservable.observe(this, Observer { buttonState ->
            when (buttonState) {
                AuthExpiredViewModel.LoginButtonState.GONE -> {
                    binding.buttonSignIn.visibility = View.GONE
                }
                AuthExpiredViewModel.LoginButtonState.VISIBLE_ENABLED -> {
                    binding.buttonSignIn.visibility = View.VISIBLE
                }
            }
        })

        return binding.root
    }

    private var informationDialogFragment: InformationDialogFragment? = null

    fun showDialog(newInfoDialogFragment: InformationDialogFragment) {
        informationDialogFragment?.let { displayedInfoDialogFragment ->
            if (displayedInfoDialogFragment.isResumed) {
                displayedInfoDialogFragment.dismiss()
            }
        }
        informationDialogFragment = newInfoDialogFragment
        informationDialogFragment?.show(activity!!.supportFragmentManager, "errorDialog")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressOverlayDelegate = ProgressOverlayDelegate(progressOverlayStyle, this, authExpiredViewModel)

        /**
         * This is duplicated from BaseEngageFullscreenFRagment because this class CANNOT inherit from
         * that class. This means this entire set of functionality should be refactored to a delegate pattern
         * so we will do that eventually as a TODO
         */
        authExpiredViewModel.dialogInfoObservable.observe(this, Observer {
            when (it.dialogType) {
                DialogInfo.DialogType.GENERIC_ERROR -> {
                    showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, it))
                }
                DialogInfo.DialogType.SERVER_ERROR -> {
                    showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, it))
                }
                DialogInfo.DialogType.NO_INTERNET_CONNECTION -> {
                    showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                            context!!, message = getString(R.string.alert_error_message_no_internet_connection)))
                }
                DialogInfo.DialogType.CONNECTION_TIMEOUT -> {
                    showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!, getString(R.string.alert_error_message_connection_timeout)))
                }
                DialogInfo.DialogType.OTHER -> {
                    // Do nothing
                }
            }
        })
    }
}