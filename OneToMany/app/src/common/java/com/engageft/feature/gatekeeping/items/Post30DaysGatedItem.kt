package com.engageft.feature.gatekeeping.items

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.feature.gatekeeping.GatedItem
import com.engageft.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Post30DaysGatedItem
 * <p>
 * Check if the user is past 30 days of transactions.
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class Post30DaysGatedItem(private val compositeDisposable: CompositeDisposable) : GatedItem() {
    override fun checkItem(resultListener: GatedItemResultListener) {
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                // Setup our Budgets state here for free:
                                val budgetInfo = response.budgetInfo

                                val daysSinceStart = LoginResponseUtils.daysSinceStart(response)
                                val budgetAmount = if (budgetInfo.budgetAmount != null && budgetInfo.budgetAmount.isNotEmpty()) {
                                    try {
                                        java.lang.Float.parseFloat(budgetInfo.budgetAmount)
                                    } catch (e: NumberFormatException) {
                                        resultListener.onItemError(e, null)
                                    }
                                } else {
                                    0.0f
                                }
                                val seenSplash = EngageService.getInstance().storageManager.hasSeenBudgets30DaysSplash
                                if (daysSinceStart > 30 && budgetAmount == 0.0F && !seenSplash) {
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
    }
}