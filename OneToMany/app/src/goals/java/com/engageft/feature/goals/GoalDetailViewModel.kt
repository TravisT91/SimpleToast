package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GoalDetailViewModel: GoalDeleteViewModel() {
    val goalDetailModelObservable = MutableLiveData<GoalDetailModel>()

    fun onDelete() {
        onTransferAndDelete(goalDetailModelObservable.value!!.goalInfo.goalId)
    }

    fun initGoalDetail(goalId: Long) {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        initGoal(goalId, LoginResponseUtils.getCurrentCard(response), true)
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

    private fun initGoal(goalId: Long, debitCardInfo: DebitCardInfo, useCache: Boolean) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, useCache)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlayImmediate()
                            if (response.isSuccess && response is GoalsResponse) {
                                var goalDetailMode: GoalDetailModel? = null
                                for (goalInfo in response.goals) {
                                    if (goalInfo.goalId == goalId) {
                                        val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null)
                                            goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
                                        else
                                            0f
                                        goalDetailMode = GoalDetailModel(goalInfo, progress)
                                        break
                                    }
                                }
                                goalDetailMode?.let {
                                    goalDetailModelObservable.value = it
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

    data class GoalDetailModel(val goalInfo: GoalInfo, val progress: Float)
}