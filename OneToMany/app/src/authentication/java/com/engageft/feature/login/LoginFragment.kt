package com.engageft.feature.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.engagekit.utils.DeviceUtils
import com.engageft.feature.EasterEggGestureDetector
import com.engageft.feature.EasterEggGestureListener
import com.engageft.feature.Palette
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentLoginBinding
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * LoginFragment
 * <p>
 * UI Fragment for Login screen.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginFragment : LotusFullScreenFragment() {
    private lateinit var constraintSet: ConstraintSet
    private lateinit var binding: FragmentLoginBinding
    private lateinit var gestureDetector: EasterEggGestureDetector

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //TODO(aHashimi): needs to fix the problem of the buttons overlapping other views when keyboard is shown
        // https://engageft.atlassian.net/browse/SHOW-363
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)
        binding.palette = Palette

        constraintSet = ConstraintSet()
        constraintSet.clone(binding.root as ConstraintLayout)

        val vm = (viewModel as LoginViewModel)
        binding.viewModel = vm
        vm.navigationObservable.observe(this, Observer { splashNavigationEvent : LoginViewModel.LoginNavigationEvent ->
            val navDestinationId = when (splashNavigationEvent) {
                LoginViewModel.LoginNavigationEvent.AUTHENTICATED_ACTIVITY -> {
                    activity!!.finish()
                    R.id.action_login_fragment_to_authenticatedActivity
                }
                LoginViewModel.LoginNavigationEvent.ISSUER_STATEMENT -> {
                    // TODO(jhutchins): Navigate to Issuer Statement
                    Toast.makeText(context!!, "TODO: Navigate to Issuer Statement", Toast.LENGTH_SHORT).show()
                    0
                }
                LoginViewModel.LoginNavigationEvent.DISCLOSURES -> {
                    // TODO(jhutchins): Navigate to Disclosures
                    Toast.makeText(context!!, "TODO: Navigate to Disclosures", Toast.LENGTH_SHORT).show()
                    0
                }
                LoginViewModel.LoginNavigationEvent.TWO_FACTOR_AUTHENTICATION -> {
                    //TODO(aHashimi): https://engageft.atlassian.net/browse/SHOW-273
                    0
                }
                LoginViewModel.LoginNavigationEvent.ACCEPT_TERMS -> {
                    //TODO(aHashimi): https://engageft.atlassian.net/browse/SHOW-354
                    0
                }
            }
            if (navDestinationId != 0) {
                binding.root.findNavController().navigate(navDestinationId)
            }
        })
        vm.usernameError.observe(this, Observer { error: LoginViewModel.UsernameValidationError ->
            when (error) {
                LoginViewModel.UsernameValidationError.NONE -> usernameInput.setErrorTexts(null)
                LoginViewModel.UsernameValidationError.INVALID_CREDENTIALS -> usernameInput.setErrorTexts(listOf(getString(R.string.error_message_invalid_credentials)))
            }
            // Make sure error is animated
            setLayoutTransitions()
        })
        vm.passwordError.observe(this, Observer { error: LoginViewModel.PasswordValidationError ->
            when (error) {
                LoginViewModel.PasswordValidationError.NONE -> passwordInput.setErrorTexts(null)
                LoginViewModel.PasswordValidationError.INVALID_CREDENTIALS -> passwordInput.setErrorTexts(listOf(getString(R.string.error_message_invalid_credentials)))
            }
            // Make sure error is animated
            setLayoutTransitions()
        })
        vm.loginButtonState.observe(this, Observer { loginButtonState: LoginViewModel.ButtonState ->
            when (loginButtonState) {
                LoginViewModel.ButtonState.SHOW -> {
                    // Animate the login button onto the screen.
                    val constraintLayout = binding.root as ConstraintLayout
                    constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.TOP, R.id.forgotPasswordText, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.BOTTOM, R.id.loginFooter, ConstraintSet.TOP, 0)

                    setLayoutTransitions()
                }
                LoginViewModel.ButtonState.HIDE -> {
                    // Animate the login button off the screen.
                    val constraintLayout = binding.root as ConstraintLayout
                    constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.BOTTOM, 0)
                    constraintSet.clear(R.id.loginButton, ConstraintSet.BOTTOM)

                    setLayoutTransitions()
                }
            }
        })
        vm.demoAccountButtonState.observe(this, Observer { buttonState: LoginViewModel.ButtonState ->
            when (buttonState) {
                LoginViewModel.ButtonState.SHOW -> demoAccountButton.visibility = View.VISIBLE
                LoginViewModel.ButtonState.HIDE -> demoAccountButton.visibility = View.GONE
            }
        })
        // If testMode was saved as enabled, make the switch visible initially.
        if (vm.testMode.get()!! || DeviceUtils.isEmulator()) {
            constraintSet.setVisibility(R.id.testSwitch, View.VISIBLE)
            setLayoutTransitions()
        }
        // The gestureDetector does not enable or disable anything, it merely controls visibility of the
        // switch so it CAN be changed. 
        gestureDetector = EasterEggGestureDetector(context!!, binding.root, object : EasterEggGestureListener {
            override fun onEasterEggActivated() {
                constraintSet.setVisibility(R.id.testSwitch, View.VISIBLE)
                setLayoutTransitions()
            }

            override fun onEasterEggDeactivated() {
                constraintSet.setVisibility(R.id.testSwitch, View.INVISIBLE)
                setLayoutTransitions()
            }
        })
        vm.dialogInfoObservable.observe(this, Observer {
            when (it.dialogType) {
                LoginDialogInfo.DialogType.GENERIC_ERROR -> {
                    //TODO(aHashimi): show dialog
                }
                LoginDialogInfo.DialogType.SERVER_ERROR -> {
                    // TODO(aHashimi): https@//engageft.atlassian.net/browse/SHOW-364
                    // check title
                    //if (!it.message.isNullOrEmpty()) {
                        // TODO(aHashimi): show dialog with message
                    //}
                }
                LoginDialogInfo.DialogType.EMAIL_VERIFICATION -> {
                    //TODO(aHashimi): show dialog: https://engageft.atlassian.net/browse/SHOW-261
                }
            }
        })
        vm.loadingOverlayDialogObservable.observe(this, Observer { loadingOverlayDialog ->
            when (loadingOverlayDialog) {
                LoginViewModel.LoadingOverlayDialog.CREATING_DEMO_ACCOUNT -> {
                    //TODO(aHashimi) message textView runs to the edges of screen. https://engageft.atlassian.net/browse/SHOW-399
                    progressOverlayDelegate.showProgressOverlay(getString(R.string.login_preview_wait_message), R.style.LoadingOverlayDialogStyle)
                }
                LoginViewModel.LoadingOverlayDialog.DISMISS_DIALOG -> {
                    progressOverlayDelegate.dismissProgressOverlay()
                }
            }
        })

        binding.btnIssuerStatement.setOnClickListener { vm.issuerStatementClicked() }
        binding.btnDisclosures.setOnClickListener { vm.disclosuresClicked() }
        binding.loginButton.setOnClickListener { vm.loginClicked() }
        binding.demoAccountButton.setOnClickListener { vm.createDemoAccount() }

        binding.root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        return binding.root
    }

    private fun setLayoutTransitions() {
        val constraintLayout = binding.root as ConstraintLayout
        val transition = AutoTransition()
        transition.duration = 250
        TransitionManager.beginDelayedTransition(constraintLayout, transition)
        constraintSet.applyTo(constraintLayout)
    }
}