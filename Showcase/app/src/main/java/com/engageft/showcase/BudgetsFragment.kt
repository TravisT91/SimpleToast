package com.engageft.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.engageft.apptoolbox.LotusFullScreenFragment

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class BudgetsFragment : LotusFullScreenFragment() {
    /*override val lotusFullScreenFragmentConfig = object : LotusFullScreenFragmentConfig() {
        override val navigationVisible = true
        override val toolbarConfig = object : BaseToolbarConfig() {
            override val actionMenuRes = 0
        }
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_budgets, container, false)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener{ view.findNavController().navigate(R.id.action_sign_up_1_fragment_to_sign_up_2_fragment) }
        return view
    }
}