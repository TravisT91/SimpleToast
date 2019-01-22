package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentGoalsAddStep1Binding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsAddStep1Fragment : BaseEngagePageFragment() {

    private lateinit var addGoalViewModel: GoalsAddStep1ViewModel

    override fun createViewModel(): BaseViewModel? {
        addGoalViewModel = ViewModelProviders.of(this).get(GoalsAddStep1ViewModel::class.java)
        return addGoalViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentGoalsAddStep1Binding.inflate(inflater, container, false).apply {
            viewModel = addGoalViewModel
            palette = Palette
        }

        return binding.root
    }
}