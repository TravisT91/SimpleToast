package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.fis.pscu.databinding.FragmentGetStartedBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.redmadrobot.inputmask.MaskedTextChangedListener

/**
 * GetStartedFragment
 * <p>
 * First screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GetStartedFragment : BaseEngageFullscreenFragment() {
    private lateinit var enrollmentViewModel: EnrollmentViewModel
    private lateinit var binding: FragmentGetStartedBinding
    override fun createViewModel(): BaseViewModel? {
        enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false).apply {
            this.viewModel = enrollmentViewModel.getStartedDelegate
            this.palette = Palette
            this.cardNumberInput.addTextChangeListener(object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    val productCardModel = ProductCardModel()
                    productCardModel.cardNumberFull = cardNumberInput.getInputTextWithMask().toString()
                    binding.cardView.updateWithProductCardModel(productCardModel)
                }
            })
        }
        return binding.root
    }
}