package com.engageft.fis.pscu.feature.branding

import com.ob.domain.lookup.branding.BrandingCard
import com.ob.domain.lookup.branding.BrandingFinancialInfo
import com.ob.domain.lookup.branding.BrandingInfo

/**
 * BrandingInfoRepo
 * </p>
 * This object is a repository for non-ui branding information.
 * </p>
 * Created by Travis Tkachuk 12/3/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

object BrandingInfoRepo {
    var financialInfo: BrandingFinancialInfo? = null
        private set
    var cards: List<BrandingCard>? = null
        private set

    fun reset(){
        financialInfo = null
        cards = null
        financialInfo = null
    }

    fun setBrandingInfo(brandingInfo: BrandingInfo){
        cards = brandingInfo.cards
        financialInfo = brandingInfo.financialInfo
    }
}