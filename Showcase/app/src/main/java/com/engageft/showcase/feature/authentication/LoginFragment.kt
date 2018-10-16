package com.engageft.showcase.feature.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.showcase.R
import com.engageft.showcase.databinding.FragmentLoginBinding
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class LoginFragment : LotusFullScreenFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(layoutInflater, R.layout.fragment_login, container, false)


        val model = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        binding.viewModel = model
        model.navigationObservable.observe(this, Observer { splashNavigationEvent : LoginViewModel.LoginNavigationEvent ->
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
        model.emailError.observe(this, Observer { error: String ->
            emailInput.setError(error)
        })
        model.passwordError.observe(this, Observer { error: String ->
            passwordInput.setError(error)
        })
        return binding.root
    }
}