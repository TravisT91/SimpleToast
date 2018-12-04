package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.BasicResponse
import io.reactivex.disposables.CompositeDisposable

class CancelCardViewModel : BaseEngageViewModel() {
    val compositeDisposable = CompositeDisposable()

    val cardCanceledSuccess = MutableLiveData<Boolean>()

    fun onCancelClicked(){
        progressOverlayShownObservable.value = true
        val token = EngageService.getInstance().storageManager.loginResponse.token
        val cardId = EngageService.getInstance().storageManager.currentCard.debitCardId
        compositeDisposable.add(engageApi().postCancelCard(CardRequest(token,cardId).fieldMap)
                .subscribeWithProgressAndDefaultErrorHandling<BasicResponse>(
                        this, {
                    cardCanceledSuccess.value = true
                    EngageService.getInstance().storageManager.removeLoginResponse()
                }))
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}