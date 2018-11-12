package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R

/**
 * Welcome1Fragment
 *
 * Welcome screen 1.
 *
 * Created by Atia Hashimi 11/6/2018.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class Welcome1Fragment: LotusFullScreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome1, container, false)
    }
}