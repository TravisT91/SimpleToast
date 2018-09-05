package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseToolbarConfig
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.LotusFullScreenFragmentConfig

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SignUp2Fragment : LotusFullScreenFragment() {

    override val lotusFullScreenFragmentConfig = object : LotusFullScreenFragmentConfig() {
        override val navigationVisible = false
        override val toolbarConfig = object : BaseToolbarConfig() {
            override val toolbarType = ToolbarType.NONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_2, container, false)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener{ view.findNavController().navigate(R.id.action_sign_up_2_fragment_to_sign_up_3_fragment) }
        return view
    }
}