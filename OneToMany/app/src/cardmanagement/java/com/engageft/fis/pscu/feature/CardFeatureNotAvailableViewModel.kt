package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData

/**
 * CardFeatureNotAvailable
 * </p>
 * This is the viewModel for the CardFeatureNotAvailableFragment
 * </p>
 * Created by Travis Tkachuk 12/3/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardFeatureNotAvailableViewModel : BaseEngageViewModel() {

    var message = ""
    var feature = MutableLiveData<CardFeatureNotAvailableFragment.UnavailableFeatureType>()

}