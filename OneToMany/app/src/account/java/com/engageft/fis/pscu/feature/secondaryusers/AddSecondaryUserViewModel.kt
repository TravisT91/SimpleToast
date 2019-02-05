package com.engageft.fis.pscu.feature.secondaryusers

import androidx.databinding.ObservableField
import com.engageft.fis.pscu.feature.BaseEngageViewModel

/**
 * Created by joeyhutchins on 2/5/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class AddSecondaryUserViewModel : BaseEngageViewModel() {
    val firstName : ObservableField<String> = ObservableField("")
    val lastName : ObservableField<String> = ObservableField("")
    val phoneNumber : ObservableField<String> = ObservableField("")
    val dob : ObservableField<String> = ObservableField("")

    fun onAddClicked() {

    }
}