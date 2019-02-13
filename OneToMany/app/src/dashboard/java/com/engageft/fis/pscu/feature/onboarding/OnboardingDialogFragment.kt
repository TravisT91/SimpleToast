package com.engageft.fis.pscu.feature.onboarding

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.DialogFragmentOnboardingViewpagerBinding
import com.engageft.fis.pscu.feature.BaseEngageDialogFragment


/**
 * Created by joeyhutchins on 2/11/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class OnboardingDialogFragment : BaseEngageDialogFragment() {
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var binding: DialogFragmentOnboardingViewpagerBinding

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(OnboardingViewModel::class.java)

        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use no frame, no title, etc.
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LotusTheme)
    }

    override fun onResume() {
        super.onResume()
        //dialog.window!!.setLayout(200, 200)
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.
//        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG, WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        return dialog
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_fragment_onboarding_viewpager, container, false)

        return binding.root
    }
}