package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseToolbarConfig
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.LotusFullScreenFragmentConfig
import com.engageft.apptoolbox.view.EmailInputWithLabel
import com.engageft.apptoolbox.view.PasswordInputWithLabel
import com.engageft.apptoolbox.view.PhoneInputWithLabel
import com.engageft.apptoolbox.view.TextInputWithLabel

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/22/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashFragment : LotusFullScreenFragment() {
    override val lotusFullScreenFragmentConfig = object : LotusFullScreenFragmentConfig() {
        override val navigationVisible = false
        override val toolbarConfig = object : BaseToolbarConfig() {
            override val toolbarType = ToolbarType.NONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener{ view.findNavController().navigate(R.id.action_splash_fragment_to_get_started_fragment) }

        val input1 = view.findViewById<TextInputWithLabel>(R.id.input1)
        input1.setClearIcon(ContextCompat.getDrawable(activity!!, R.drawable.ic_clear))
        input1.setClearIconTint(R.color.warning)


        val input2 = view.findViewById<PasswordInputWithLabel>(R.id.input2)
        input2.setHelperText("put a password!")

        val input3 = view.findViewById<PhoneInputWithLabel>(R.id.input3)
        input3.setClearIcon(ContextCompat.getDrawable(activity!!, R.drawable.ic_clear))
        input3.setClearIconTint(R.color.black)
        input3.setInputTextAppearance(R.style.HelperText)
        input3.setLabelText("I am a phone input!")

        val input4 = view.findViewById<EmailInputWithLabel>(R.id.input4)
        input4.setErrorTextAppearance(R.style.ErrorText)
        input4.setHelperTextAppearance(R.style.HelperText)
        input4.setHelperText("You won't see me!")
        input4.setError("I am an error!")
        return view
    }
}