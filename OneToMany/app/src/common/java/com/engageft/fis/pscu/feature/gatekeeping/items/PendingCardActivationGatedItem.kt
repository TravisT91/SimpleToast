package com.engageft.fis.pscu.feature.gatekeeping.items

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.DebitCardInfoUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * PendingCardActivationGatedItem
 * <p>
 * Check if the user is pending card activation.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class PendingCardActivationGatedItem(private val compositeDisposable: CompositeDisposable) : GatedItem() {
    private var hasBeenChecked = false
    override fun checkItem(resultListener: GatedItemResultListener) {
        if (!hasBeenChecked) {
            hasBeenChecked = true
            compositeDisposable.add(
                    EngageService.getInstance().loginResponseAsObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                if (response.isSuccess && response is LoginResponse) {
                                    val currentCard = LoginResponseUtils.getCurrentCard(response)
                                    if (currentCard != null && DebitCardInfoUtils.isPendingActivation(currentCard)) {
                                        resultListener.onItemCheckFailed()
                                    } else {
                                        resultListener.onItemCheckPassed()
                                    }
                                } else {
                                    resultListener.onItemError(null, response.message)
                                }
                            }) { e ->
                                resultListener.onItemError(e, null)
                            }
            )
        } else {
            resultListener.onItemCheckPassed()
        }
    }
}