package com.engageft.fis.pscu.feature.gatekeeping.items

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.authentication.AuthenticationConfig
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * RequireEmailConfirmationGatedItem
 * <p>
 * Check if email confirmation is required and if the config has it enabled. 
 * </p>
 * Created by joeyhutchins on 12/10/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class RequireEmailConfirmationGatedItem(private val compositeDisposable: CompositeDisposable) : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                if (AuthenticationConfig.requireEmailConfirmation && LoginResponseUtils.requireEmailVerification(response)) {
                                    resultListener.onItemCheckFailed()
                                } else {
                                    resultListener.onItemCheckPassed()
                                }
                            } else {
                                resultListener.onItemError(null, response.message)
                            }
                        }) {e ->
                            resultListener.onItemError(e, null)
                        }
        )
    }
}