package com.engageft.fis.pscu.feature

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentMobileCheckDepositDownloadBinding
import com.engageft.fis.pscu.feature.config.MobileCheckDepositConfig

/**
 * MobileCheckDepositDownloadFragment
 * <p>
 * Fragment directing user to get Ingo app for Mobile check deposits.
 * </p>
 * Created by joeyhutchins on 12/14/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class MobileCheckDepositDownloadFragment : BaseEngagePageFragment() {
    override fun createViewModel(): BaseViewModel? {
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMobileCheckDepositDownloadBinding.inflate(inflater,container,false)

        binding.button.setOnClickListener {
            val appPackageName = MobileCheckDepositConfig.ingoAppPackage
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }
        binding.buttonLearnMore.setOnClickListener {
            try {
                val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MobileCheckDepositConfig.ingoAppWebsite))
                startActivity(myIntent)
            } catch (e: ActivityNotFoundException) {
                engageFragmentDelegate.handleGenericThrowable(e)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (MobileCheckDepositConfig.isIngoPackageInstalled(activity!!)) {
            findNavController().navigate(R.id.action_mobileCheckDepositFragment_to_mobileCheckDepositOpenFragment)
        }
    }
}