package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R

/**
 * GetStartedFragment
 * <p>
 * First screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GetStartedFragment : BaseEngageFullscreenFragment() {
    override fun createViewModel(): BaseViewModel? {
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_get_started, container, false)
        val button1 = view.findViewById<Button>(R.id.button1)
        val button2 = view.findViewById<Button>(R.id.button2)
        val button3 = view.findViewById<Button>(R.id.button3)
        val button4 = view.findViewById<Button>(R.id.button4)
        val button5 = view.findViewById<Button>(R.id.button5)
        button1.setOnClickListener{
            view.findNavController().navigate(R.id.action_getStartedFragment_to_enrollmentCardPinFragment)
        }
        button2.setOnClickListener{
            view.findNavController().navigate(R.id.action_getStartedFragment_to_createAccountFragment)
        }
        button3.setOnClickListener{
            view.findNavController().navigate(R.id.action_getStartedFragment_to_verifyIdentityFragment)
        }
        button4.setOnClickListener{
            view.findNavController().navigate(R.id.action_getStartedFragment_to_termsOfUseFragment)
        }
        button5.setOnClickListener{
            view.findNavController().navigate(R.id.action_getStartedFragment_to_sendingEnrollmentFragment)
        }
        return view
    }
}