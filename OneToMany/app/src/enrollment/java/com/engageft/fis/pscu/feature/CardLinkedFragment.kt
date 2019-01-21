package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardLinkedBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.utils.cardStatusStringRes
import com.ob.domain.lookup.branding.BrandingCard

/**
 * CardLinkedFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardLinkedFragment : BaseEngagePageFragment() {
    private lateinit var viewModelDelegate: CardLinkedDelegate
    private lateinit var viewModel: BaseEngageViewModel
    private lateinit var binding: FragmentCardLinkedBinding

    private val brandingCardObserver = Observer<BrandingCard> { updateBrandingCard(it) }

    override fun createViewModel(): BaseViewModel? {
        val enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        viewModel = enrollmentViewModel
        viewModelDelegate = enrollmentViewModel.cardLinkedDelegate
        return enrollmentViewModel
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardLinkedBinding.inflate(inflater, container, false)
        binding.viewModel = viewModelDelegate
        binding.palette = Palette

        binding.title.text = getString(R.string.ENROLLMENT_CARD_LINKED).applyTypefaceAndColorToSubString(
                ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                Palette.primaryColor,
                getString(R.string.ENROLLMENT_CARD_LINKED_SUBSTRING))

        viewModelDelegate.productCardViewModelDelegate.cardInfoModelObservable.observe(this@CardLinkedFragment, Observer { productCardModel ->
            productCardModel.cardStatusText = getString(productCardModel.cardStatus.cardStatusStringRes())
            binding.cardView.updateWithProductCardModel(productCardModel)
        })
        viewModelDelegate.brandingCardObservable.observe(this@CardLinkedFragment, brandingCardObserver)
        return binding.root
    }

    private fun updateBrandingCard(brandingCard: BrandingCard?) {
        brandingCard?.let {
            binding.cardView.applyBranding(it, viewModel.compositeDisposable) { e ->
                Toast.makeText(context, "Failed to retrieve card image", Toast.LENGTH_SHORT).show()
                Log.e("BRANDING_INFO_FAIL", e.message)
                //TODO(ttkachuk) right now it is not clear on how we should handle failure to retrieve the card image
                //tracked in FOTM-497
            }
        }
    }
}