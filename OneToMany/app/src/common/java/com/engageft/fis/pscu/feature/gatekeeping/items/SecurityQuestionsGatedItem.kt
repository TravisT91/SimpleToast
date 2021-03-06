package com.engageft.fis.pscu.feature.gatekeeping.items

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AuthenticatedRequest
import com.engageft.fis.pscu.feature.authentication.AuthenticationConfig
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.GatedItemResultListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * SecurityQuestionsGatedItem
 * <p>
 * Check if the user needs to answer security questions.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class SecurityQuestionsGatedItem(private val compositeDisposable: CompositeDisposable) : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        if (AuthenticationConfig.requireSecurityQuestions) {
            compositeDisposable.add(
                        EngageService.getInstance().engageApiInterface.postHasSecurityQuestions(AuthenticatedRequest().fieldMap)
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribe({ response ->
                            if (response.isSuccess && response.message == "true") {
                                resultListener.onItemCheckPassed()
                            } else {
                                resultListener.onItemCheckFailed()
                            }
                        }) {e ->
                            resultListener.onItemError(e, null)
                        }
            )
        } else {
            resultListener.onItemCheckPassed()
        }
    }
}