package com.engageft.fis.pscu.feature.branding

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AuthenticatedRequest
import com.engageft.engagekit.rest.request.RefCodeRequest
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.BrandingInfoResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * BrandingManager
 * </p>
 * This object is responsible for handling the BrandingInfoResponse and applying it to the
 * Palette and BrandingInfo objects.
 * </p>
 * Created by Travis Tkachuk 12/4/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

object BrandingManager {

    fun getBrandingWithToken(token: String): Observable<BasicResponse> {
        return EngageService.getInstance().engageApiInterface.postGetAccountBrandingInfo(
                AuthenticatedRequest(token).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isSuccess && it is BrandingInfoResponse) {
                        applyBrandingInfo(it)
                    }
                }
    }

    fun getBrandingWithRefCode(refCode: String): Observable<BasicResponse> {
        return EngageService.getInstance().engageApiInterface.postGetBrandingInfoFromRefCode(
                RefCodeRequest(refCode).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isSuccess && it is BrandingInfoResponse) {
                        applyBrandingInfo(it)
                    }
                }
    }

    private fun applyBrandingInfo(brandingInfoResponse: BrandingInfoResponse) {
        brandingInfoResponse.brandingInfo.apply {
            Palette.applyBrandingInfo(brandingInfo = this)
            BrandingInfoRepo.setBrandingInfo(brandingInfo = this)
        }
    }

    fun clearBranding() {
        Palette.reset()
        BrandingInfoRepo.reset()
    }
}