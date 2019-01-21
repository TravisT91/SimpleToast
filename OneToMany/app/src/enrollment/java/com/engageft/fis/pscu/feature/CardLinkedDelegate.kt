package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.engageft.engagekit.EngageService
import com.ob.domain.lookup.branding.BrandingCard

/**
 * Created by joeyhutchins on 1/21/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CardLinkedDelegate(private val viewModel: EnrollmentViewModel, private val navController: NavController,
                         private val linkedNavigations: EnrollmentViewModel.EnrollmentNavigations.LinkedNavigations) {

    val brandingCardObservable = MutableLiveData<BrandingCard>()
    val productCardViewModelDelegate = ProductCardViewDelegate(viewModel)

    fun onDoneClicked() {
        if (viewModel.activationCardInfo.isParentActivationRequired) {
            navController.navigate(linkedNavigations.linkedToLogin)
        } else {
            EngageService.getInstance().authManager.authToken = viewModel.activationResponse.token
            navController.navigate(linkedNavigations.linkedToDashboard)
        }
    }
}