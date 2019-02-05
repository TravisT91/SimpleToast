package com.engageft.fis.pscu.feature.secondaryusers

import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.feature.BaseEngagePageFragment

/**
 * Created by joeyhutchins on 2/5/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class AddSecondaryUserFragment : BaseEngagePageFragment() {
    private lateinit var secondaryUserViewModel: AddSecondaryUserViewModel

    override fun createViewModel(): BaseViewModel? {
        secondaryUserViewModel = ViewModelProviders.of(this).get(AddSecondaryUserViewModel::class.java)
        return secondaryUserViewModel
    }
}