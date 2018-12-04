package com.engageft.fis.pscu.feature.branding

import com.ob.domain.lookup.branding.BrandingFinancialInfo
import com.ob.domain.lookup.branding.BrandingInfo
import com.ob.domain.lookup.branding.BrandingTerm

/**
 * BrandingInfoRepo
 * </p>
 * This object is a repository for non-ui branding information.
 * </p>
 * Created by Travis Tkachuk 12/3/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

object BrandingInfoRepo {
    var cardImageUrl: String? = null
        private set
    var brandLogoUrl: String? = null
        private set
    var terms: Array<BrandingTerm>? = null
        private set
    var financialInfo: BrandingFinancialInfo? = null
        private set

    fun resetBrandingInfo(){
        financialInfo = null
        cardImageUrl = null
        terms = null
        financialInfo = null
    }

    fun setBrandingInfo(brandingInfo: BrandingInfo){
        cardImageUrl = brandingInfo.cardImage
        brandLogoUrl = brandingInfo.brandLogo
        terms = brandingInfo.terms
        financialInfo = brandingInfo.financialInfo
    }
}