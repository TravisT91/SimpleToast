package com.engageft.fis.pscu.feature.onboarding

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.model.AccountUIPropertNames
import com.engageft.engagekit.model.BudgetsOnboardingCompleteDateUIProperty
import com.engageft.engagekit.model.CoreOnboardingCompleteDateUIProperty
import com.engageft.engagekit.model.GoalsOnboardingCompleteDateUIProperty
import com.engageft.engagekit.rest.request.AddUIPropertyRequest
import com.engageft.engagekit.rest.request.GetUIPropertiesRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.AccountUIPropertiesResponse
import com.ob.ws.dom.AccountUIPropertyResponse
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime


/**
 * Created by joeyhutchins on 2/11/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class OnboardingViewModel : BaseEngageViewModel() {
    val onboardingItemsObservable = MutableLiveData<List<OnboardingListItems>>().apply {
        value = getCoreOnboardingItems()
    }

    inner class FetchResponse(val propertyResponse: AccountUIPropertiesResponse, val loginResponse: BasicResponse)

    val dismissObservable = SingleLiveEvent<Any?>()

    init {
        // TODO(jhutchins): Optimizations to this could be refreshing the data or attempting to retry on failure.
        showProgressOverlayDelayed()

        val propertyObservable = EngageService.getInstance().engageApiInterface.postGetUIProperties(GetUIPropertiesRequest().fieldMap)
        val loginResponseObservable = EngageService.getInstance().loginResponseAsObservable

        val zippedObservable = io.reactivex.Observable.zip(propertyObservable.subscribeOn(Schedulers.io()), loginResponseObservable.subscribeOn(Schedulers.io()),
                BiFunction<AccountUIPropertiesResponse, BasicResponse, FetchResponse> { propertyResponse, loginResponse ->
                    FetchResponse(propertyResponse, loginResponse)
                })

        compositeDisposable.add(zippedObservable
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ finalResponse ->
                    dismissProgressOverlay()
                    // Let's check both calls succeeded:
                    if (finalResponse.propertyResponse.isSuccess) {

                    } else {
                        handleUnexpectedErrorResponse(finalResponse.propertyResponse)
                    }
                    if (finalResponse.loginResponse.isSuccess && finalResponse.loginResponse is LoginResponse) {

                    } else {
                        handleUnexpectedErrorResponse(finalResponse.loginResponse)
                    }

                    val propertyResponse = finalResponse.propertyResponse
                    val loginResponse = finalResponse.loginResponse as LoginResponse

                    var coreOnboardingProperty: AccountUIPropertyResponse? = null
                    var budgetsOnboardingProperty: AccountUIPropertyResponse? = null
                    var goalsOnboardingProperty: AccountUIPropertyResponse? = null
                    propertyResponse.accountUIProperties?.let {
                        for (property in it) {
                            if (property.propertyName == AccountUIPropertNames.coreOnboardingCompleteDate) {
                                coreOnboardingProperty = property
                            }
                            if (property.propertyName == AccountUIPropertNames.budgetsOnboardingCompleteDate) {
                                budgetsOnboardingProperty = property
                            }
                            if (property.propertyName == AccountUIPropertNames.goalsOnboardingCompleteDate) {
                                goalsOnboardingProperty = property
                            }
                        }
                    }

                    val list = ArrayList<OnboardingListItems>()
                    coreOnboardingProperty?.let {
                        val timestamp = try {
                            BackendDateTimeUtils.parseDateTimeFromIso8601String(it.propertyValue as String)
                        } catch (e: Exception) {
                            null
                        }
                        if (timestamp == null) {
                            list.addAll(getCoreOnboardingItems())
                        }
                    } ?: kotlin.run {
                        list.addAll(getCoreOnboardingItems())
                    }

                    val debitCardInfo = LoginResponseUtils.getCurrentCard(loginResponse)
                    if (debitCardInfo.cardPermissionsInfo.isBudgetsEnabled) {
                        budgetsOnboardingProperty?.let {
                            val timestamp = try {
                                BackendDateTimeUtils.parseDateTimeFromIso8601String(it.propertyValue as String)
                            } catch (e: Exception) {
                                null
                            }
                            if (timestamp == null) {
                                list.add(OnboardingListItems.BudgetsOnboardingItem)
                            }
                        } ?: kotlin.run {
                            list.add(OnboardingListItems.BudgetsOnboardingItem)
                        }
                    }
                    if (debitCardInfo.cardPermissionsInfo.isGoalsEnabled) {
                        goalsOnboardingProperty?.let {
                            val timestamp = try {
                                BackendDateTimeUtils.parseDateTimeFromIso8601String(it.propertyValue as String)
                            } catch (e: Exception) {
                                null
                            }
                            if (timestamp == null) {
                                list.add(OnboardingListItems.GoalsOnboardingItem)
                            }
                        } ?: kotlin.run {
                            list.add(OnboardingListItems.GoalsOnboardingItem)
                        }
                    }

                    if (list.isEmpty()) {
                        throw IllegalStateException("Showing onboarding with no onboarding to show!")
                    }

                    onboardingItemsObservable.value = list
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                }))
    }

    fun onOnboardingTourComplete() {
        showProgressOverlayDelayed()

        val onboardingItems = onboardingItemsObservable.value!!
        val hasSeenCore = onboardingItems.contains(OnboardingListItems.DashboardOnboardingItem)
        val hasSeenBudgets = onboardingItems.contains(OnboardingListItems.BudgetsOnboardingItem)
        val hasSeenGoals = onboardingItems.contains(OnboardingListItems.GoalsOnboardingItem)

        val observableList = ArrayList<Observable<BasicResponse>>()
        if (hasSeenCore) {
            val request = AddUIPropertyRequest(CoreOnboardingCompleteDateUIProperty(DateTime()))
            observableList.add(EngageService.getInstance().engageApiInterface.postAddUIProperty(request.fieldMap).subscribeOn(Schedulers.io()))
        }
        if (hasSeenBudgets) {
            val request = AddUIPropertyRequest(BudgetsOnboardingCompleteDateUIProperty(DateTime()))
            observableList.add(EngageService.getInstance().engageApiInterface.postAddUIProperty(request.fieldMap).subscribeOn(Schedulers.io()))
        }
        if (hasSeenGoals) {
            val request = AddUIPropertyRequest(GoalsOnboardingCompleteDateUIProperty(DateTime()))
            observableList.add(EngageService.getInstance().engageApiInterface.postAddUIProperty(request.fieldMap).subscribeOn(Schedulers.io()))
        }

        val zipped = Observable.zip<BasicResponse, List<BasicResponse>>(observableList) { args ->
            ArrayList<BasicResponse>().apply {
                for (arg in args) {
                    add(arg as BasicResponse)
                }
            }
        }

        compositeDisposable.add(zipped
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dismissProgressOverlay()
                    for (response in it) {
                        if (!response.isSuccess) {
                            handleUnexpectedErrorResponse(response)
                        }
                    }
                    dismissObservable.call()
                }, {e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private fun getCoreOnboardingItems(): MutableList<OnboardingListItems> {
        return mutableListOf<OnboardingListItems>().apply {
            add(OnboardingListItems.DashboardOnboardingItem)
            add(OnboardingListItems.CardOnboardingItem)
            add(OnboardingListItems.SearchOnboardingItem)
        }
    }
}

sealed class OnboardingListItems {
    object DashboardOnboardingItem : OnboardingListItems()
    object CardOnboardingItem : OnboardingListItems()
    object SearchOnboardingItem : OnboardingListItems()
    object BudgetsOnboardingItem : OnboardingListItems()
    object GoalsOnboardingItem : OnboardingListItems()
}