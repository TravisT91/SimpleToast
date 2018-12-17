package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentMobileCheckDepositOpenBinding
import com.engageft.fis.pscu.feature.config.MobileCheckDepositConfig

/**
 * MobileCheckDepositOpenFragment
 * <p>
 * Fragment directing user to open Ingo app for Mobile check deposits.
 * </p>
 * Created by joeyhutchins on 12/14/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class MobileCheckDepositOpenFragment : BaseEngageFullscreenFragment() {
    override fun createViewModel(): BaseViewModel? {
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMobileCheckDepositOpenBinding.inflate(inflater,container,false)
        binding.button.setOnClickListener {
            val appPackageName = MobileCheckDepositConfig.ingoAppPackage
            try {
                val launchIntent = activity!!.packageManager.getLaunchIntentForPackage(appPackageName)
                startActivity(launchIntent)
            } catch (e: Exception) {
                handleGenericThrowable(e)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (MobileCheckDepositConfig.isIngoPackageInstalled(activity!!)) {
            findNavController().navigate(R.id.action_mobileCheckDepositOpenFragment_to_mobileCheckDepositFragment)
        }
    }
}