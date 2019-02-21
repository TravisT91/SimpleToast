package com.engageft.feature.goals

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class BaseGoalsViewModel: BaseEngageViewModel() {

    fun refreshLoginResponse(observable: SingleLiveEvent<Unit>) {
        compositeDisposable.add(EngageService.getInstance().refreshLoginObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // if response is success hide progressOverlay after refreshing goals
                    if (response is LoginResponse) {
                        refreshGoals(LoginResponseUtils.getCurrentCard(response), observable)
                    } else {
                        dismissProgressOverlay()
                        // notify observer of success even if refresh failed
                        observable.call()
                    }
                }, {
                    dismissProgressOverlay()
                    // notify observer of success even if refresh failed
                    observable.call()
                })
        )
    }

    private fun refreshGoals(debitCardInfo: DebitCardInfo, observable: SingleLiveEvent<Unit>) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            dismissProgressOverlay()
                            // notify observer of success even if refresh failed
                            observable.call()
                        }, {
                            dismissProgressOverlay()
                            // notify observer of success even if refresh failed
                            observable.call()
                        })
        )
    }
}