package com.engageft.fis.pscu

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NavMenuViewModel: BaseEngageViewModel() {
    val lastFourCardDigitsObservable = MutableLiveData<Int>()
    val goalsEnableStateObservable = MutableLiveData<Boolean>()

    init {
        goalsEnableStateObservable.value = false

        //TODO(aHashimi): how should we show progressOverlay for activities if ever needed?

        // Joey: I’ll start by saying this is acceptable for the short term, because we don’t have a means
        // to get a long term solution yet.This will get the budgets enabled settings only once when
        // this activity is first launched. The problem is if while this activity is alive, this setting
        // changes on the backend, this viewModel will never know about it, and the activity will forever
        // show the navigation. I think the ideal solution is a LiveData style observer on the LoginResponse,
        // so whenever it changes, this ViewModel is notified and can update the menu. [something for future]
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
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
                    handleThrowable(e)
                })
        )
    }
}