package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by joeyhutchins on 2/8/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class MoveMoneyViewModel : BaseEngageViewModel() {
    val cardLoadVisibilityObservable = MutableLiveData<Boolean>()
    val mobileCheckDepositVisibilityObservable = MutableLiveData<Boolean>()
    val directDepositVisibilityObservable = MutableLiveData<Boolean>()

    init {
        cardLoadVisibilityObservable.value = false
        mobileCheckDepositVisibilityObservable.value = false
        directDepositVisibilityObservable.value = false
        loadSettings()
    }

    private fun loadSettings() {
        // TODO(jhutchins): Maybe someday we need empty state handling?
        showProgressOverlayDelayed()
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is LoginResponse) {
                                val currentAccount = LoginResponseUtils.getCurrentAccountInfo(response)
                                cardLoadVisibilityObservable.value = currentAccount.accountPermissionsInfo.isFundingDebitCardEnabled
                                mobileCheckDepositVisibilityObservable.value = currentAccount.accountPermissionsInfo.isFundingCheckDepositEnabled
                                directDepositVisibilityObservable.value = currentAccount.accountPermissionsInfo.isFundingDirectDepositEnabled
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }

                        }) { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        }
        )
    }
}