package com.engageft.fis.pscu

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AuthenticatedViewModel: BaseEngageViewModel() {
    val lastFourCardDigitsObservable = MutableLiveData<Int>()
    val goalsEnableStateObservable = MutableLiveData<Boolean>()

    init {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response.isSuccess && response is LoginResponse) {
                        val currentAccountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                        // set the last four digits of card
                        val lastFour = currentAccountInfo.debitCardInfo.lastFour
                        lastFourCardDigitsObservable.value = lastFour.toInt()

                        goalsEnableStateObservable.value = currentAccountInfo.debitCardInfo.cardPermissionsInfo.isGoalsEnabled
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }
}