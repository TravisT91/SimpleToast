package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusPageFragment
import com.engageft.fis.pscu.R

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalAdd2Fragment : LotusPageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override val name: String
        get() = "Goals2"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_goal_add_1, container, false)
        return view
    }
}