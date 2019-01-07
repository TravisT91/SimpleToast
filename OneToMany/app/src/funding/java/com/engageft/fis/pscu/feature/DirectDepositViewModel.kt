package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.ob.ws.dom.DirectDepositInfoResponse

/**
 * DirectDepositViewModel
 * </p>
 * This is the corresponding ViewModel to the DirectDepositFragment.
 * </p>
 * Created by Travis Tkachuk 12/6/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DirectDepositViewModel : BaseEngageViewModel() {

    var routingNumber = MutableLiveData<String>()
    var accountNumber = MutableLiveData<String>()
    var accountType = MutableLiveData<String>()
    var bankName = MutableLiveData<String>()
    var shouldShowPrintButton = MutableLiveData<Boolean>()
    var accountTypeString = ""

    init {
        routingNumber.value = ""
        accountNumber.value = ""
        accountType.value =  ""
        bankName.value =  ""
        shouldShowPrintButton.value = false
    }

    fun getDirectDepositInfo() {
        val engageService = EngageService.getInstance()
        val token = engageService.authManager.authToken
        val cardId = engageService.storageManager.currentCard.debitCardId
        val requestFieldMap = CardRequest(token, cardId).fieldMap
        engageApi().postDebitDirectDepositInfo(requestFieldMap)
                .subscribeWithDefaultProgressAndErrorHandling<DirectDepositInfoResponse>(
                        this, { applyDirectDepositInfo(it) })
    }

    private fun applyDirectDepositInfo(directDepositInfoResponse: DirectDepositInfoResponse){
        directDepositInfoResponse.let{
            routingNumber.value = it.routeNumber
            accountNumber.value = it.accountNumber
            accountType.value = accountTypeString
            BrandingInfoRepo.financialInfo?.institutionName?.let{
                brandingBankName -> bankName.value = brandingBankName
            }
            shouldShowPrintButton.value = true
        }
    }

    fun formatDirectDepositUrl(unformattedUrl: String) :String {
        val webSiteUrl = if (EngageAppConfig.isUsingProdEnvironment) {
            EngageAppConfig.engageKitConfig.prodEnvironment.websiteUrl
        } else {
            EngageAppConfig.engageKitConfig.devEnvironment.websiteUrl
        }
        val token = EngageService.getInstance().authManager.authToken
        return String.format(unformattedUrl, webSiteUrl, token)
    }
}