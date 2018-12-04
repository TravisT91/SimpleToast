package com.engageft.fis.pscu.feature.branding

import com.ob.domain.lookup.branding.BrandingFinancialInfo

/**
 * BrandingInfo
 * </p>
 * This object will act as a holder for all non-UI related branding information.
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