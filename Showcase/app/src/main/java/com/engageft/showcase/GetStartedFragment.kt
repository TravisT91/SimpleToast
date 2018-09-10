package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.engageft.apptoolbox.LotusFullScreenFragment

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/22/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GetStartedFragment : LotusFullScreenFragment() {
    /*override val lotusFullScreenFragmentConfig = object : LotusFullScreenFragmentConfig() {
        override val navigationVisible = false // This will be ignored
        override val toolbarConfig: BaseToolbarConfig? = null
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_get_started, container, false)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener{ view.findNavController().navigate(R.id.action_get_started_fragment_to_login_fragment) }
        val signUpButton = view.findViewById<Button>(R.id.signUpButton)
        signUpButton.setOnClickListener{ view.findNavController().navigate(R.id.action_get_started_fragment_to_sign_up_1_fragment) }
        return view
    }
}