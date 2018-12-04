package com.engageft.fis.pscu.feature.branding

import com.ob.domain.lookup.branding.BrandingFinancialInfo

/**
 * BrandingDelegate
 * </p>
 * Handles the parsing and storing of the BrandingInfoResponse
 * </p>
 * Created by Travis Tkachuk 12/3/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

object BrandingInfo{
    var financialInfo : BrandingFinancialInfo? = null

    fun resetBrandingInfo(){
        financialInfo = null
    }
}