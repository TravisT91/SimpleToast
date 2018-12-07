package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusPageFragment
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.R

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsFragment : LotusPageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override val name: String
        get() = "GoalsFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_goals, container, false)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener{
            toolbarController.setToolbarVisibility(ToolbarVisibilityState.GONE)
            Handler().postDelayed({
                view.findNavController().navigate(R.id.action_goals_fragment_to_goalAdd1Fragment)
            }, 2000L)
        }
        return view
    }
}