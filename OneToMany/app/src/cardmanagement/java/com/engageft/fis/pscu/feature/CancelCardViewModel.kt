package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.BasicResponse

class CancelCardViewModel : BaseEngageViewModel() {

    val cardCanceledSuccess = MutableLiveData<Boolean>()

    fun onCancelClicked(){
        val cardId = EngageService.getInstance().storageManager.currentCard.debitCardId
        engageApi().postCancelCard(CardRequest(cardId).fieldMap)
                .subscribeWithDefaultProgressAndErrorHandling<BasicResponse>(
                        this, {
                    cardCanceledSuccess.value = true
                    EngageService.getInstance().storageManager.removeLoginResponse()
                })
    }
}