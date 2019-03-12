package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import utilGen1.StringUtils

class ReportLostStolenCardViewModel : BaseEngageViewModel() {
    var address: String = ""

    val lostStolenReportedSuccess = MutableLiveData<Boolean>()

    fun onReportLostStolenClicked(){
        progressOverlayShownObservable.value = true
        val cardId = EngageService.getInstance().storageManager.currentCard.debitCardId
        engageApi().postLostStolenCard(CardRequest(cardId).fieldMap)
                .subscribeWithDefaultProgressAndErrorHandling<BasicResponse>(
                        this, {
                    lostStolenReportedSuccess.value = true
                    EngageService.getInstance().storageManager.removeLoginResponse()
                })
    }

    init{
        val loginResponse = EngageService.getInstance().storageManager.loginResponse
        loginResponse?.let{ setAddressFromLoginResponse(it) } ?: run {
            progressOverlayShownObservable.value = true
            EngageService.getInstance().loginResponseAsObservable
                    .subscribeWithDefaultProgressAndErrorHandling<LoginResponse>(
                            this, { setAddressFromLoginResponse(it) })
        }
    }

    private fun setAddressFromLoginResponse(loginResponse: LoginResponse){
        val addressInfo = LoginResponseUtils.getAddressInfoForCurrentDebitCard(loginResponse)
        address = StringUtils.formatAddressString(addressInfo)
    }
}