package com.engageft.fis.pscu.feature

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardFeatureNotAvailableBinding
import com.engageft.fis.pscu.feature.branding.BrandingInfo
import com.engageft.fis.pscu.feature.branding.Palette
import utilGen1.StringUtils

/**
 * CardFeatureNotAvailableFragment
 * </p>
 * This fragment lets the user know that the feature they selected is not available to them.
 * </p>
 * Created by Travis Tkachuk 12/3/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class CardFeatureNotAvailableFragment: BaseEngageFullscreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(CardFeatureNotAvailableViewModel::class.java)
    }

    companion object {
        const val KEY_UNAVAILABLE_FEATURE_ID = "KEY_UNAVAILABLE_ID"
    }

    enum class UnavailableFeatureType(val id: Int){
        UNKNOWN(-1),
        LOST_STOLEN(0),
        CANCEL(1),
        REPLACE(2);

        companion object {
            fun getFeatureById(id: Int): UnavailableFeatureType {
                return when(id){
                    0 -> LOST_STOLEN
                    1 -> CANCEL
                    2 -> REPLACE
                    else -> UNKNOWN
                }
            }
        }
    }

    private fun getMessageByFeature(feature: UnavailableFeatureType) : String {
        return when(feature){
            CardFeatureNotAvailableFragment.UnavailableFeatureType.LOST_STOLEN ->
                getString(R.string.FEATURE_NOT_AVAILABLE_LOST_STOLEN)
            CardFeatureNotAvailableFragment.UnavailableFeatureType.CANCEL ->
                getString(R.string.FEATURE_NOT_AVAILABLE_CANCEL)
            CardFeatureNotAvailableFragment.UnavailableFeatureType.REPLACE ->
                getString(R.string.FEATURE_NOT_AVAILABLE_REPLACE)
            CardFeatureNotAvailableFragment.UnavailableFeatureType.UNKNOWN ->
                getString(R.string.FEATURE_NOT_AVAILABLE_UNKNOWN)
        }
    }

    private fun getTitleByFeature(feature: UnavailableFeatureType) : String{
        return when(feature) {
            CardFeatureNotAvailableFragment.UnavailableFeatureType.UNKNOWN ->
                getString(R.string.FEATURE_NOT_AVAILABLE_UNKNOWN_FEATURE_TITLE)
            CardFeatureNotAvailableFragment.UnavailableFeatureType.LOST_STOLEN ->
                getString(R.string.fragment_title_report_lost_stolen_card)
            CardFeatureNotAvailableFragment.UnavailableFeatureType.CANCEL ->
                getString(R.string.fragment_title_cancel_card)
            CardFeatureNotAvailableFragment.UnavailableFeatureType.REPLACE ->
                getString(R.string.fragment_title_replace_card)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCardFeatureNotAvailableBinding.inflate(inflater,container,false).apply {

            palette = Palette
            cardFeatureNotAvailableViewModel = (viewModel as CardFeatureNotAvailableViewModel)

            title.text = StringUtils.applyTypefaceAndColorToSubString(
                    Palette.primaryColor,
                    Palette.font_bold!!,
                    title.text.toString(),
                    getString(R.string.FEATURE_NOT_AVAILABLE_HEADER_SUBSTRING))

            callSupportButton.setOnClickListener {
                activity?.startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:" + BrandingInfo.financialInfo?.supportNumber) })

            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val featureId = arguments?.getInt(KEY_UNAVAILABLE_FEATURE_ID,-1) ?: UnavailableFeatureType.UNKNOWN.id
        (viewModel as? CardFeatureNotAvailableViewModel)?.apply {
            feature.observe(this@CardFeatureNotAvailableFragment, Observer {
                message = getMessageByFeature(it)
                (activity as? AppCompatActivity)?.supportActionBar?.title = getTitleByFeature(it)
            })
            feature.value = UnavailableFeatureType.getFeatureById(featureId)
        }
    }

}