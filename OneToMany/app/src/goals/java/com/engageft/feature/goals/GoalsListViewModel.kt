package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo

class GoalsListViewModel: BaseEngageViewModel() {
    private var canEditGoal = false
    private var goalsContributed = ""
    val goalsListObservable = MutableLiveData<GoalModelItem>()
    private var goalsList = listOf<GoalInfo>()

    fun initData(useCache: Boolean) {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // if response is success hide progressOverlay after getting goal API
                    if (response is LoginResponse) {
                        goalsContributed = response.goalsContributed
                        canEditGoal = LoginResponseUtils.canEditGoals(response)
                        getGoals(LoginResponseUtils.getCurrentCard(response), useCache)
                    } else {
                        dismissProgressOverlayImmediate()
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlayImmediate()
                    handleThrowable(e)
                })
        )
    }

    private fun getGoals(debitCardInfo: DebitCardInfo, useCache: Boolean) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, useCache)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlayImmediate()
                            if (response.isSuccess && response is GoalsResponse) {
                                // don't trigger observers unless goals list has changed or first time
                                if (goalsList != response.goals || goalsListObservable.value == null) {
                                    goalsList = response.goals

                                    val goalModelList = mutableListOf<GoalModel>()
                                    for (goalInfo in response.goals) {
                                        val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null)
                                            goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
                                        else
                                            0f
                                        goalModelList.add(GoalModel(goalInfo, progress))
                                    }
                                    goalsListObservable.value = GoalModelItem(goalModelList, canEditGoal, goalsContributed)
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlayImmediate()
                            handleThrowable(e)
                        })
        )
    }

    // an explicit refresh
    fun refreshData() {
        EngageService.getInstance().clearLoginAndDashboardResponses()
        initData(false)
    }

    data class GoalModel(val goalInfo: GoalInfo, val progress: Float)
    data class GoalModelItem(val goalModelList: List<GoalModel>, val canEditGoal: Boolean, val goalContributions: String)
}