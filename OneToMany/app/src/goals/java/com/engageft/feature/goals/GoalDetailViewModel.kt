package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import java.lang.IllegalArgumentException
import java.math.BigDecimal

class GoalDetailViewModel: GoalDeleteViewModel() {
    val goalDetailStatesListObservable = MutableLiveData<List<GoalDetailState>>()
    val goalScreenTitleObservable = MutableLiveData<String>()

    var goalId: Long = 0L
    var fundAmount: BigDecimal = BigDecimal.ZERO
    var goalName: String = ""

    fun onDelete() {
        onTransferAndDelete(goalId)
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
                                var goalDetailStateList : List<GoalDetailState> = listOf()
                                for (goalInfo in response.goals) {
                                    if (goalInfo.goalId == goalId) {
                                        fundAmount = goalInfo.fundAmount
                                        goalName = goalInfo.name
                                        this.goalId = goalInfo.goalId
                                        goalDetailStateList = getGoalDetailStateList(goalInfo)
                                        break
                                    }
                                }
                                goalDetailStatesListObservable.value = goalDetailStateList
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlayImmediate()
                            handleThrowable(e)
                        })
        )
    }

    private fun getGoalDetailStateList(goalInfo: GoalInfo) :  List<GoalDetailState> {
        val goalDetailStateList = mutableListOf<GoalDetailState>()

        if (goalInfo.isAchieved) {
            goalDetailStateList.add(GoalDetailState.GoalCompleteHeader(goalInfo.fundAmount))
            goalDetailStateList.add(GoalDetailState.SingleTransfer)
            goalDetailStateList.add(GoalDetailState.Delete)
        } else {
            val recurrenceType : GoalDetailState.GoalIncompleteHeader.PayPlanType = when (goalInfo.payPlan.recurrenceType) {
                PAYPLAN_TYPE_DAY -> GoalDetailState.GoalIncompleteHeader.PayPlanType.DAY
                PAYPLAN_TYPE_WEEK -> GoalDetailState.GoalIncompleteHeader.PayPlanType.WEEK
                PAYPLAN_TYPE_MONTH -> GoalDetailState.GoalIncompleteHeader.PayPlanType.MONTH
                else -> {
                    throw IllegalArgumentException("payPlan is of wrong type")
                }
            }
            val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null)
                goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
            else
                0f
            val goalIncompleteHeaderModel = GoalDetailState.GoalIncompleteHeader.GoalIncompleteHeaderModel(
                    fundAmount = goalInfo.fundAmount,
                    goalAmount = goalInfo.amount,
                    frequencyAmount = goalInfo.payPlan.amount,
                    progress = progress,
                    payPlanType = recurrenceType,
                    goalCompleteDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate))
            goalDetailStateList.add(GoalDetailState.GoalIncompleteHeader(goalIncompleteHeaderModel))

            goalDetailStateList.add(GoalDetailState.SingleTransfer)
            goalDetailStateList.add(GoalDetailState.GoalPauseState(goalInfo.payPlan.isPaused))
            goalDetailStateList.add(GoalDetailState.Edit)
            goalDetailStateList.add(GoalDetailState.Delete)
        }

        return goalDetailStateList
    }

    companion object {
        private const val PAYPLAN_TYPE_DAY = "DAY"
        private const val PAYPLAN_TYPE_WEEK = "WEEK"
        private const val PAYPLAN_TYPE_MONTH = "MONTH"
    }


}
sealed class GoalDetailState {
    class GoalCompleteHeader(val fundAmount: BigDecimal) : GoalDetailState()
    class GoalIncompleteHeader(val goalIncompleteHeaderModel: GoalIncompleteHeaderModel) : GoalDetailState() {
        enum class PayPlanType {
            DAY,
            WEEK,
            MONTH
        }
        data class GoalIncompleteHeaderModel(val fundAmount: BigDecimal, val goalAmount: BigDecimal,
                                             val progress: Float, val frequencyAmount: BigDecimal,
                                             val payPlanType: GoalDetailState.GoalIncompleteHeader.PayPlanType,
                                             val goalCompleteDate: DateTime)
    }
    class GoalPauseState(val isGoalPaused: Boolean) : GoalDetailState()
    object SingleTransfer : GoalDetailState()
    object Edit : GoalDetailState()
    object Delete : GoalDetailState()
}

