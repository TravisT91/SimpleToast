package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.LotusFullScreenFragment



/**
 * SplashFragment
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/22/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SplashFragment : LotusFullScreenFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        val model = ViewModelProviders.of(this).get(SplashScreenViewModel::class.java)


        return view
    }
}