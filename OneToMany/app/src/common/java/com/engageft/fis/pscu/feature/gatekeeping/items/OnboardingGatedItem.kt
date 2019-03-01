package com.engageft.fis.pscu.feature.gatekeeping.items

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.model.AccountUIPropertyNames
import com.engageft.engagekit.rest.request.GetUIPropertiesRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.GatedItemResultListener
import com.ob.ws.dom.AccountUIPropertiesResponse
import com.ob.ws.dom.AccountUIPropertyResponse
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
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
    private var hasBeenChecked = false

    inner class FetchResponse(val propertyResponse: BasicResponse, val loginResponse: BasicResponse)

    override fun checkItem(resultListener: GatedItemResultListener) {
        if (!hasBeenChecked) {
            hasBeenChecked = true

            val propertyObservable = engageApi().postGetUIProperties(GetUIPropertiesRequest().fieldMap)
            val loginResponseObservable = EngageService.getInstance().loginResponseAsObservable

            val zippedObservable = io.reactivex.Observable.zip(propertyObservable.subscribeOn(Schedulers.io()), loginResponseObservable.subscribeOn(Schedulers.io()),
                    BiFunction<BasicResponse, BasicResponse, FetchResponse> { propertyResponse, loginResponse ->
                        FetchResponse(propertyResponse, loginResponse)
                    })

            compositeDisposable.add(zippedObservable
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ finalResponse ->
                        // Let's check both calls succeeded:
                        var failed = false
                        if (!finalResponse.propertyResponse.isSuccess && finalResponse.propertyResponse is AccountUIPropertiesResponse) {
                            failed = true
                            resultListener.onItemError(null, finalResponse.propertyResponse.message)
                        }
                        if (!finalResponse.loginResponse.isSuccess && finalResponse.loginResponse !is LoginResponse && !failed) {
                            failed = true
                            resultListener.onItemError(null, finalResponse.loginResponse.message)
                        }
                        if (!failed) {
                            val propertyResponse = finalResponse.propertyResponse as AccountUIPropertiesResponse
                            val loginResponse = finalResponse.loginResponse as LoginResponse

                            var coreOnboardingProperty: AccountUIPropertyResponse? = null
                            var budgetsOnboardingProperty: AccountUIPropertyResponse? = null
                            var goalsOnboardingProperty: AccountUIPropertyResponse? = null
                            propertyResponse.accountUIProperties?.forEach { property ->
                                when (property.propertyName) {
                                    AccountUIPropertyNames.coreOnboardingCompleteDate -> coreOnboardingProperty = property
                                    AccountUIPropertyNames.budgetsOnboardingCompleteDate -> budgetsOnboardingProperty = property
                                    AccountUIPropertyNames.goalsOnboardingCompleteDate -> goalsOnboardingProperty = property
                                    else -> {}// Do nothing
                                }
                            }

                            var showOnboarding = false
                            coreOnboardingProperty?.let {
                                val timestamp = try {
                                    BackendDateTimeUtils.parseDateTimeFromIso8601String(it.propertyValue as String)
                                } catch (e: Exception) {
                                    null
                                }
                                if (timestamp == null) {
                                    showOnboarding = true
                                }
                            } ?: kotlin.run {
                                showOnboarding = true
                            }

                            if (!showOnboarding) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(loginResponse)
                                if (debitCardInfo.cardPermissionsInfo.isBudgetsEnabled) {
                                    budgetsOnboardingProperty?.let {
                                        val timestamp = try {
                                            BackendDateTimeUtils.parseDateTimeFromIso8601String(it.propertyValue as String)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        if (timestamp == null) {
                                            showOnboarding = true
                                        }
                                    } ?: kotlin.run {
                                        showOnboarding = true
                                    }
                                }

                                if (!showOnboarding) {
                                    if (debitCardInfo.cardPermissionsInfo.isGoalsEnabled) {
                                        goalsOnboardingProperty?.let {
                                            val timestamp = try {
                                                BackendDateTimeUtils.parseDateTimeFromIso8601String(it.propertyValue as String)
                                            } catch (e: Exception) {
                                                null
                                            }
                                            if (timestamp == null) {
                                                showOnboarding = true
                                            }
                                        } ?: kotlin.run {
                                            showOnboarding = true
                                        }
                                    }
                                }
                            }
                            if (showOnboarding) {
                                resultListener.onItemCheckFailed()
                            } else {
                                resultListener.onItemCheckPassed()
                            }
                        }
                    }, { e ->
                        resultListener.onItemError(e, null)
                    })
            )
        } else {
            resultListener.onItemCheckPassed()
        }
    }
}