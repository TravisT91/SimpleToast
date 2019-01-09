package com.engageft.fis.pscu.feature

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardLockUnlockRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.ResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.R
import com.ob.domain.lookup.DebitCardStatus
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CardLockUnlockViewModel: BaseEngageViewModel() {

    enum class CardStatus {
        LOCKED,
        UNLOCKED,
        UNKNOWN,
        CHANGED_SUCCESS
    }

    val cardStatusObservable = MutableLiveData<CardStatus>()

    private lateinit var currentCard: DebitCardInfo

    init {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        progressOverlayShownObservable.value = false
                        currentCard = LoginResponseUtils.getCurrentCard(response)
                        when (currentCard.status) {
                            DebitCardStatus.ACTIVE -> cardStatusObservable.value = CardStatus.UNLOCKED
                            DebitCardStatus.LOCKED_USER -> cardStatusObservable.value = CardStatus.LOCKED
                            else -> cardStatusObservable.value == CardStatus.UNKNOWN
                        }
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }

    fun isCardLocked(): Boolean {
        when (currentCard.status) {
            DebitCardStatus.ACTIVE ->  return false // it's unlocked, lock it
            DebitCardStatus.LOCKED_USER -> return true // it's locked, unlock it
        }

        return false
    }

    fun onLockUnlock() {
        updateCardLockStatus(!isCardLocked())
    }

    private fun updateCardLockStatus(lock: Boolean){
        engageApi().postLockCard(
                CardLockUnlockRequest(
                        EngageService.getInstance().storageManager.loginResponse.token,
                        EngageService.getInstance().storageManager.currentCard.debitCardId,
                        lock).fieldMap)
                .subscribeWithDefaultProgressAndErrorHandling<BasicResponse>(this, {
                    EngageService.getInstance().clearLoginAndDashboardResponses()
                    cardStatusObservable.value = CardStatus.CHANGED_SUCCESS
//                    productCardViewModelDelegate.updateCardView()
                })
    }
}