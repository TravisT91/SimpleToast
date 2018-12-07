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
class GoalAdd1Fragment : LotusPageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override val name: String
        get() = "Goals1"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_goal_add_1, container, false)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener{
            Handler().postDelayed( {
                view.findNavController().navigate(R.id.action_goalAdd1Fragment_to_goalAdd2Fragment)
            }, 2000)
            val desiredState = if (toolbarController.toolbarVisibilityState == ToolbarVisibilityState.GONE) ToolbarVisibilityState.VISIBLE else ToolbarVisibilityState.GONE
            toolbarController.animateToolbarVisibility(desiredState, 500L)
        }
        return view
    }
}