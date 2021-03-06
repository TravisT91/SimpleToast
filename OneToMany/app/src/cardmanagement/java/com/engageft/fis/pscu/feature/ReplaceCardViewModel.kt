package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import utilGen1.StringUtils

class ReplaceCardViewModel : BaseEngageViewModel() {

    var address: String = ""

    val replacementRequestIsSuccess = MutableLiveData<Boolean>()

    fun onOrderReplacementClicked(){
        progressOverlayShownObservable.value = true
        val cardId = EngageService.getInstance().storageManager.currentCard.debitCardId
        engageApi().postReplaceCard(CardRequest(cardId).fieldMap)
                .subscribeWithDefaultProgressAndErrorHandling<BasicResponse>(
                        this, {
                    replacementRequestIsSuccess.value = true
                    EngageService.getInstance().storageManager.removeLoginResponse()
                })
    }

    init{
        val loginResponse = EngageService.getInstance().storageManager.loginResponse
        loginResponse?.let{ setAddressFromLoginResponse(it) } ?: run {
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
