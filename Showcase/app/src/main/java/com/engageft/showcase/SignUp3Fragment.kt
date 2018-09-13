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
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SignUp3Fragment : LotusFullScreenFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_3, container, false)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener{ view.findNavController().navigate(R.id.action_sign_up_3_fragment_to_login_fragment) }
        return view
    }
}