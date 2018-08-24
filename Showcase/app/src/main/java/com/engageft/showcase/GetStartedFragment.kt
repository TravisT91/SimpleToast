package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseToolbarConfig
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.LotusFullScreenFragmentConfig

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/22/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GetStartedFragment : LotusFullScreenFragment() {
    override val lotusFullScreenFragmentConfig = object : LotusFullScreenFragmentConfig() {
        override val navigationVisible = false
        override val toolbarConfig = object : BaseToolbarConfig() {
            override val toolbarType = ToolbarType.NONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_get_started, container, false)
        return view
    }
}