package com.engageft.feature.gatekeeping.items

import android.text.TextUtils
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.feature.gatekeeping.GatedItem
import com.engageft.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * OnboardingGatedItem
 * <p>
 * Check if the user has seen onboarding flows.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class OnboardingGatedItem(private val compositeDisposable: CompositeDisposable) : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                // Note(jhutchins):
                                // The onboardingCompleteDate, for some reason, is stored in the DebitCardInfo model.
                                // This is weird because we might have more than one card on this account.
                                // Will the onboardingCompleteDate be the same for every single DebitCardInfo?
                                // The answer is YES, according to Vipin Kumar. This was confirmed on 6-13-18.
                                // When asked why we store this date in the cards instead of at the account
                                // level (which makes sense), I was told it was done this way due to time
                                // constraints and this model was most quickly editable.
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                debitCardInfo?.let {
                                    if (TextUtils.isEmpty(debitCardInfo.onboardingCompleteDate)) {
                                        resultListener.onItemCheckFailed()
                                    } else {
                                        resultListener.onItemCheckPassed()
                                    }
                                } ?: kotlin.run {
                                    resultListener.onItemCheckFailed()
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