package com.engageft.fis.pscu.feature.branding

import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.OneToManyApplication
import com.engageft.fis.pscu.feature.Palette
import com.ob.domain.lookup.branding.BrandingFinancialInfo
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.BrandingInfoResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * BrandingDelegate
 * </p>
 * Handles the parsing and storing of the BrandingInfoResponse
 * </p>
 * Created by Travis Tkachuk 12/3/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

object BrandingDelegate{
    var financialInfo : BrandingFinancialInfo? = null

    fun getBrandingWithToken(token : String): Observable<BasicResponse> {
        return EngageService.getInstance().engageApiInterface.postGetAccountBrandingInfo(
                HashMap<String,String>().apply{ put("token",token)})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isSuccess && it is BrandingInfoResponse){
                        applyBrandingInfo(it)
                    }
                }}

    fun getBrandingWithRefCode(refCode : String): Observable<BasicResponse> {
        return EngageService.getInstance().engageApiInterface.postGetBrandingInfoFromRefCode(
                HashMap<String,String>().apply{ put("refCode",refCode)})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isSuccess && it is BrandingInfoResponse){
                        applyBrandingInfo(it)
                    }
                }}

    private fun applyBrandingInfo(brandingInfoResponse: BrandingInfoResponse){
        brandingInfoResponse.brandingInfo.apply {
            Palette.applyColors(colors)
            Palette.setFontsFromString(font)
            this@BrandingDelegate.financialInfo = financialInfo
        }
    }

    fun clearBranding(){
        OneToManyApplication.sInstance.setPaletteDefaults()

    }
}