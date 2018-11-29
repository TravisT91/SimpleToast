package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AuthenticatedRequest
import com.engageft.engagekit.rest.request.ReplaceCardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.tag.TokenRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import utilGen1.StringUtils

class ReplaceCardViewModel : BaseEngageViewModel() {
    val compositeDisposable = CompositeDisposable()

    var address: String = ""
    val replacementRequestStatus = MutableLiveData<ReplacementRequestStatus>()

    fun onOrderReplacementClicked(){
        replacementRequestStatus.value = ReplacementRequestStatus.PROCESSING
        val token = EngageService.getInstance().storageManager.loginResponse.token
        val cardId = EngageService.getInstance().storageManager.currentCard.debitCardId
        val params = HashMap<String,String>().apply {
            put("token", token)
            put("cardId", cardId.toString())
        }
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postReplaceCard(params)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    replacementRequestStatus.value =
                                            if(it.isSuccess) ReplacementRequestStatus.SUCCESS
                                            else ReplacementRequestStatus.FAILED
                                },
                                {
                                    replacementRequestStatus.value = ReplacementRequestStatus.FAILED
                                    handleThrowable(it)
                                }))
    }

    enum class ReplacementRequestStatus {
        PROCESSING, SUCCESS, FAILED
    }

    init{
        val loginResponse = EngageService.getInstance().storageManager.loginResponse
        val addressInfo = LoginResponseUtils.getAddressInfoForCurrentDebitCard(loginResponse)
        address = StringUtils.formatAddressString(addressInfo)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
