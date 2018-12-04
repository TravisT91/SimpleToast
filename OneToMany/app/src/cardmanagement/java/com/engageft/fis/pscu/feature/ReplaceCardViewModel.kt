package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.disposables.CompositeDisposable
import utilGen1.StringUtils

class ReplaceCardViewModel : BaseEngageViewModel() {
    val compositeDisposable = CompositeDisposable()

    var address: String = ""

    val replacementRequestIsSuccess = MutableLiveData<Boolean>()

    fun onOrderReplacementClicked(){
        progressOverlayShownObservable.value = true
        val token = EngageService.getInstance().storageManager.loginResponse.token
        val cardId = EngageService.getInstance().storageManager.currentCard.debitCardId
        compositeDisposable.add(
                engageApi().postReplaceCard(CardRequest(token,cardId).fieldMap)
                        .subscribeWithProgressAndDefaultErrorHandling<BasicResponse>(
                        this, {
                            replacementRequestIsSuccess.value = true
                            EngageService.getInstance().storageManager.removeLoginResponse()
                        }))
    }

    init{
        val loginResponse = EngageService.getInstance().storageManager.loginResponse
        loginResponse?.let{ setAddressFromLoginResponse(it) } ?: run {
            progressOverlayShownObservable.value = true
            compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                    .subscribeWithProgressAndDefaultErrorHandling<LoginResponse>(
                            this, { setAddressFromLoginResponse(it) }))
        }
    }

    private fun setAddressFromLoginResponse(loginResponse: LoginResponse){
        val addressInfo = LoginResponseUtils.getAddressInfoForCurrentDebitCard(loginResponse)
        address = StringUtils.formatAddressString(addressInfo)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
