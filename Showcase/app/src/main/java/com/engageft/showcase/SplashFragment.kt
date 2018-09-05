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
import com.engageft.apptoolbox.view.InputWithLabel

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

        val input1 = view.findViewById<InputWithLabel>(R.id.input1)
        //input1.setHelperText("This is helper text.")
        //input1.setError("This is an error.\nThis is a second error.\nThis is a third error.\nThis is a fourth error.")
        //input1.setClearIcon(R.drawable.ic_clear)
        input1.setClearIcon(ContextCompat.getDrawable(activity!!, R.drawable.ic_clear))


        val input2 = view.findViewById<InputWithLabel>(R.id.input2)
        input2.setInputType(InputWithLabel.Companion.InputType.PASSWORD)
        input2.setInputText("This is helper text.")
        return view
    }
}