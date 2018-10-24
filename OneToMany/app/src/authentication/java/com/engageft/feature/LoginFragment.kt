package com.engageft.feature

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
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
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

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)
        constraintSet = ConstraintSet()
        constraintSet.clone(binding.root as ConstraintLayout)

        val vm = (viewModel as LoginViewModel)
        binding.viewModel = vm
        vm.navigationObservable.observe(this, Observer { splashNavigationEvent : LoginViewModel.LoginNavigationEvent ->
            val navDestinationId = when (splashNavigationEvent) {
                LoginViewModel.LoginNavigationEvent.AUTHENTICATED_ACTIVITY -> {
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
            }
            if (navDestinationId != 0) {
                binding.root.findNavController().navigate(navDestinationId)
            }
        })
        vm.emailError.observe(this, Observer { error: LoginViewModel.EmailValidationError ->
            when (error) {
                LoginViewModel.EmailValidationError.NONE -> emailInput.setError("")
                LoginViewModel.EmailValidationError.INVALID_CREDENTIALS -> emailInput.setError("I am not ready yet!") // Localize this
            }
            // Make sure error is animated
            setLayoutTransitions()
        })
        vm.passwordError.observe(this, Observer { error: LoginViewModel.PasswordValidationError ->
            when (error) {
                LoginViewModel.PasswordValidationError.NONE -> passwordInput.setError("")
                LoginViewModel.PasswordValidationError.INVALID_CREDENTIALS -> passwordInput.setError("I am not ready yet!") // Localize this
            }
            // Make sure error is animated
            setLayoutTransitions()
        })
        vm.loginButtonState.observe(this, Observer { loginButtonState: LoginViewModel.LoginButtonState ->
            when (loginButtonState) {
                LoginViewModel.LoginButtonState.SHOW -> {
                    // Animate the login button onto the screen.
                    val constraintLayout = binding.root as ConstraintLayout
                    constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.TOP, R.id.forgotPasswordText, ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(R.id.loginButton, ConstraintSet.BOTTOM, R.id.loginFooter, ConstraintSet.TOP, 0)

                    setLayoutTransitions()
                }
                LoginViewModel.LoginButtonState.HIDE -> {
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

        binding.btnIssuerStatement.setOnClickListener { vm.issuerStatementClicked() }
        binding.btnDisclosures.setOnClickListener { vm.disclosuresClicked() }

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