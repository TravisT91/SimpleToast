package com.engageft.fis.pscu.feature.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.engagekit.utils.DeviceUtils
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentLoginBinding
import com.engageft.fis.pscu.feature.BaseEngageFullscreenFragment
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.EasterEggGestureDetector
import com.engageft.fis.pscu.feature.EasterEggGestureListener
import com.engageft.fis.pscu.feature.infoDialogSimpleMessageNoTitle

/**
 * LoginFragment
 * <p>
 * UI Fragment for Login screen.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginFragment : BaseEngageFullscreenFragment() {
    private lateinit var constraintSet: ConstraintSet
    private lateinit var contentConstraintSet: ConstraintSet
    private lateinit var binding: FragmentLoginBinding
    private lateinit var gestureDetector: EasterEggGestureDetector

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //TODO(aHashimi): needs to fix the problem of the buttons overlapping other views when keyboard is shown
        // https://engageft.atlassian.net/browse/SHOW-363
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)

        constraintSet = ConstraintSet()
        constraintSet.clone(binding.loginParent)

        contentConstraintSet = ConstraintSet()
        contentConstraintSet.clone(binding.contentBox)

        val vm = (viewModel as LoginViewModel)
        binding.viewModel = vm
        vm.navigationObservable.observe(this, Observer { splashNavigationEvent : LoginViewModel.LoginNavigationEvent ->
            val navDestinationId = when (splashNavigationEvent) {
                LoginViewModel.LoginNavigationEvent.AUTHENTICATED_ACTIVITY -> {
                    activity!!.finish()
                    R.id.action_loginFragment_to_authenticatedActivity
                }
                LoginViewModel.LoginNavigationEvent.ISSUER_STATEMENT -> {
                    // TODO(jhutchins): Navigate to Issuer Statement
                    Toast.makeText(context!!, "TODO: Navigate to Issuer Statement", Toast.LENGTH_LONG).show()
                    0
                }
                LoginViewModel.LoginNavigationEvent.DISCLOSURES -> {
                    // TODO(jhutchins): Navigate to Disclosures
                    Toast.makeText(context!!, "TODO: Navigate to Disclosures", Toast.LENGTH_LONG).show()
                    0
                }
                LoginViewModel.LoginNavigationEvent.TWO_FACTOR_AUTHENTICATION -> {
                    //TODO(aHashimi): https://engageft.atlassian.net/browse/SHOW-273
                    Toast.makeText(context!!, "TODO: Navigate to Two Factor Auth", Toast.LENGTH_LONG).show()
                    activity!!.finish()
                    R.id.action_loginFragment_to_authenticatedActivity
                }
                LoginViewModel.LoginNavigationEvent.ACCEPT_TERMS -> {
                    //TODO(aHashimi): https://engageft.atlassian.net/browse/SHOW-354
                    //TODO(aHashimi): this's here to bypass Accept terms until it's resolved
                    Toast.makeText(context!!, "TODO: Navigate to accept terms", Toast.LENGTH_LONG).show()
                    activity!!.finish()
                    R.id.action_loginFragment_to_authenticatedActivity
                }
                LoginViewModel.LoginNavigationEvent.SECURITY_QUESTIONS -> {
                    Toast.makeText(context!!, "Showing security questions", Toast.LENGTH_LONG).show()
                    activity!!.finish()
                    R.id.action_login_fragment_to_securityQuestionsActivity
                }
            }
            if (navDestinationId != 0) {
                binding.root.findNavController().navigate(navDestinationId)
            }
        })
        vm.loginButtonState.observe(this, Observer { loginButtonState: LoginViewModel.ButtonState ->
            when (loginButtonState) {
                LoginViewModel.ButtonState.SHOW -> {
                    // Animate the login button onto the screen.
                    val constraintLayout = binding.loginParent
                    constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.TOP, R.id.contentBox, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.BOTTOM, R.id.loginFooter, ConstraintSet.TOP, 0)
                    constraintSet.connect(R.id.loginFooter, ConstraintSet.TOP, R.id.loginButton, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.contentBox, ConstraintSet.BOTTOM, R.id.loginButton, ConstraintSet.TOP, 0)

                    setFullLayoutTransitions()
                }
                LoginViewModel.ButtonState.HIDE -> {
                    // Animate the login button off the screen.
                    val constraintLayout = binding.loginParent as ConstraintLayout
                    constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.BOTTOM, 0)
                    constraintSet.clear(R.id.loginButton, ConstraintSet.BOTTOM)
                    constraintSet.connect(R.id.loginFooter, ConstraintSet.TOP, R.id.contentBox, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.contentBox, ConstraintSet.BOTTOM, R.id.loginFooter, ConstraintSet.TOP, 0)

                    setFullLayoutTransitions()
                }
            }
        })
        vm.demoAccountButtonState.observe(this, Observer { buttonState: LoginViewModel.ButtonState ->
            when (buttonState) {
                LoginViewModel.ButtonState.SHOW -> {
                    constraintSet.setVisibility(R.id.demoAccountButton, View.VISIBLE)
                    constraintSet.connect(R.id.loginFooter, ConstraintSet.TOP, R.id.demoAccountButton, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.contentBox, ConstraintSet.BOTTOM, R.id.demoAccountButton, ConstraintSet.TOP, 0)
                }
                LoginViewModel.ButtonState.HIDE -> {
                    constraintSet.setVisibility(R.id.demoAccountButton, View.GONE)
                    constraintSet.connect(R.id.loginFooter, ConstraintSet.TOP, R.id.contentBox, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.contentBox, ConstraintSet.BOTTOM, R.id.loginFooter, ConstraintSet.TOP, 0)
                }
            }
            setFullLayoutTransitions()
        })
        // If testMode was saved as enabled, make the switch visible initially.
        if (vm.testMode.get()!! || DeviceUtils.isEmulator()) {
            contentConstraintSet.setVisibility(R.id.testSwitch, View.VISIBLE)
            setFullLayoutTransitions()
        }
        // The gestureDetector does not enable or disable anything, it merely controls visibility of the
        // switch so it CAN be changed. 
        gestureDetector = EasterEggGestureDetector(context!!, binding.root, object : EasterEggGestureListener {
            override fun onEasterEggActivated() {
                contentConstraintSet.setVisibility(R.id.testSwitch, View.VISIBLE)
                setFullLayoutTransitions()
            }

            override fun onEasterEggDeactivated() {
                contentConstraintSet.setVisibility(R.id.testSwitch, View.INVISIBLE)
                setFullLayoutTransitions()
            }
        })

        vm.dialogInfoObservable.observe(this, Observer { dialogInfo ->
            when (dialogInfo.dialogType) {
                DialogInfo.DialogType.OTHER -> {
                    when ((dialogInfo as LoginDialogInfo).loginDialogType) {

                        LoginDialogInfo.LoginDialogType.EMAIL_VERIFICATION_PROMPT -> {
                            val listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                                override fun onDialogFragmentNegativeButtonClicked() {
                                    vm.logout()
                                }

                                override fun onDialogFragmentPositiveButtonClicked() {
                                    vm.onConfirmEmail()
                                }

                                override fun onDialogCancelled() {
                                    vm.logout()
                                }
                            }
                            showDialog(InformationDialogFragment.newLotusInstance(
                                    title = getString(R.string.login_confirm_email_alert_title),
                                    message = getString(R.string.login_confirm_email_alert_message),
                                    buttonPositiveText = getString(R.string.login_confirm_email_send_button),
                                    buttonNegativeText = getString(R.string.dialog_information_cancel_button),
                                    layoutType = InformationDialogFragment.LayoutType.BUTTONS_STACKED,
                                    listener = listener))
                        }
                        LoginDialogInfo.LoginDialogType.EMAIL_VERIFICATION_SUCCESS -> {
                            showDialog(infoDialogSimpleMessageNoTitle(context!!,
                                    message = getString(R.string.login_confirm_email_success)))
                        }
                    }
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

        binding.apply {
            passwordInput.setImeOptions(EditorInfo.IME_ACTION_DONE)
            passwordInput.onImeAction(EditorInfo.IME_ACTION_DONE) { vm.loginClicked() }

            btnIssuerStatement.setOnClickListener { vm.issuerStatementClicked() }
            btnDisclosures.setOnClickListener { vm.disclosuresClicked() }
            loginButton.setOnClickListener { vm.loginClicked() }
            demoAccountButton.setOnClickListener { vm.createDemoAccount() }

            root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        return binding.root
    }

    private fun setFullLayoutTransitions() {
        val constraintLayout = binding.loginParent
        val transition = AutoTransition()
        transition.duration = 250
        TransitionManager.beginDelayedTransition(constraintLayout, transition)
        constraintSet.applyTo(constraintLayout)
        setContentLayoutTransitions()
    }

    private fun setContentLayoutTransitions() {
        val contentConstraintLayout = binding.contentBox
        val transition = AutoTransition()
        transition.duration = 250
        TransitionManager.beginDelayedTransition(contentConstraintLayout, transition)
        contentConstraintSet.applyTo(contentConstraintLayout)
    }
}