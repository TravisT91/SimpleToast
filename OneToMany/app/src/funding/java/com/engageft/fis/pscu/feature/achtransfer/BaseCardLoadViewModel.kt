package com.engageft.fis.pscu.feature.achtransfer

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class BaseCardLoadViewModel: BaseEngageViewModel() {

    fun refreshLoginResponse(observable: SingleLiveEvent<Unit>) {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().refreshLoginObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response is LoginResponse) {
                        observable.call()
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
}