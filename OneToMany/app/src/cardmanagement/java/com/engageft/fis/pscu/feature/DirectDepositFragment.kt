package com.engageft.fis.pscu.feature

import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel

/**
 * DirectDepositFragment
 * </p>
 * This fragment displays direct deposit info to the user and let's them print a direct deposit form.
 * </p>
 * Created by Travis Tkachuk 12/6/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class DirectDepositFragment: BaseEngageFullscreenFragment(){
    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(DirectDepositViewModel::class.java)
    }

}