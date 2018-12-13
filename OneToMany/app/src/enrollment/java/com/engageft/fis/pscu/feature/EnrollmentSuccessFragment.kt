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
class EnrollmentSuccessFragment : BaseEngageFullscreenFragment() {
    override fun createViewModel(): BaseViewModel? {
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_enrollment_success, container, false)
        val button1 = view.findViewById<Button>(R.id.button1)
        val button2 = view.findViewById<Button>(R.id.button2)
        button1.setOnClickListener{
            view.findNavController().navigate(R.id.action_enrollmentSuccessFragment_to_cardActiveFragment)
        }
        button2.setOnClickListener{
            view.findNavController().navigate(R.id.action_enrollmentSuccessFragment_to_cardLinkedFragment)
        }
        return view
    }
}