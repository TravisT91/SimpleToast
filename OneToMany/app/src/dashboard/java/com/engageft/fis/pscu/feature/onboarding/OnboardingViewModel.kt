package com.engageft.fis.pscu.feature.onboarding

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by joeyhutchins on 2/11/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class OnboardingViewModel : BaseEngageViewModel() {
    val onboardingItemsObservable = MutableLiveData<List<OnboardingListItems>>().apply {
        value = getCoreOnboardingItems()
    }

    init {
        // TODO(jhutchins): Optimizations to this could be refreshing the data or attempting to retry on failure.
        showProgressOverlayDelayed()

        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is LoginResponse) {

                                // TODO(jhutchins): FOTM-989 update with user defined properties. We need to check if other front-ends
                                // already showed certain screens, if not, we show them, and later apply
                                // has seen = true;
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                val list = getCoreOnboardingItems()
                                if (debitCardInfo.cardPermissionsInfo.isBudgetsEnabled) {
                                    list.add(OnboardingListItems.BudgetsOnboardingItem)
                                }
                                if (debitCardInfo.cardPermissionsInfo.isGoalsEnabled) {
                                    list.add(OnboardingListItems.GoalsOnboardingItem)
                                }
                                onboardingItemsObservable.value = list
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }

                        }) { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        }
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