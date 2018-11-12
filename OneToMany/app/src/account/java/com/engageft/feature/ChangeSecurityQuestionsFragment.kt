package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.databinding.FragmentChangeSecurityQuestionsBinding

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangeSecurityQuestionsFragment : LotusFullScreenFragment() {
    private lateinit var changeSecurityQuestionsViewModel: ChangeSecurityQuestionsViewModel
    override fun createViewModel(): BaseViewModel? {
        changeSecurityQuestionsViewModel = ViewModelProviders.of(this).get(ChangeSecurityQuestionsViewModel::class.java)
        return changeSecurityQuestionsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChangeSecurityQuestionsBinding.inflate(inflater, container, false)
        binding.viewModel = changeSecurityQuestionsViewModel
        binding.palette = Palette

        return binding.root
    }
}