package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.PayPlanPauseResumeRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import java.lang.IllegalArgumentException
import java.math.BigDecimal

class GoalDetailViewModel(var goalId: Long): GoalDeleteViewModel() {

    enum class RecurringTransferStatus {
        PAUSE_RESUME_FAILURE
    }

    val goalDetailStatesListObservable = MutableLiveData<List<GoalDetailState>>()
    val goalScreenTitleObservable = MutableLiveData<String>()
    val goalRecurringTransferObservable = MutableLiveData<RecurringTransferStatus>()

    var fundAmount: BigDecimal = BigDecimal.ZERO
    var goalName: String = ""

    private lateinit var goalInfo : GoalInfo

    fun onDelete() {
        onTransferAndDelete(goalId)
    }

    fun initGoalData(useCache: Boolean) {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        initGoal(LoginResponseUtils.getCurrentCard(response), useCache)
                    } else {
                        dismissProgressOverlay()
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private fun initGoal(debitCardInfo: DebitCardInfo, useCache: Boolean) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, useCache)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is GoalsResponse) {
                                var goalDetailStateList : List<GoalDetailState> = listOf()
                                response.goals.find { goalInfo ->
                                    goalInfo.goalId == goalId
                                }?.let { goalInfo ->
                                    fundAmount = goalInfo.fundAmount
                                    goalName = goalInfo.name
                                    this.goalInfo = goalInfo
                                    goalDetailStateList = getGoalDetailStateList(goalInfo)
                                }
                                goalScreenTitleObservable.value = goalName
                                goalDetailStatesListObservable.value = goalDetailStateList
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    fun onPauseResumeGoal() {
        showProgressOverlayImmediate()
        val newPauseValue = !goalInfo.payPlan.isPaused
        val request = PayPlanPauseResumeRequest.newInstanceForGoal(goalInfo.payPlan.payPlanId, newPauseValue)

        compositeDisposable.add(engageApi().postPayPlanPause(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess) {
                                initGoalData(useCache = false)
                            } else {
                                dismissProgressOverlay()
                                goalRecurringTransferObservable.value = RecurringTransferStatus.PAUSE_RESUME_FAILURE
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            goalRecurringTransferObservable.value = RecurringTransferStatus.PAUSE_RESUME_FAILURE
                            handleThrowable(e)
                        })
        )
    }

    private fun getGoalDetailStateList(goalInfo: GoalInfo) :  List<GoalDetailState> {
        val goalDetailStateList = mutableListOf<GoalDetailState>()

        if (goalInfo.isAchieved) {
            goalDetailStateList.add(GoalDetailState.GoalCompleteHeaderItem(goalInfo.fundAmount))
            goalDetailStateList.add(GoalDetailState.SingleTransferItem)
            goalDetailStateList.add(GoalDetailState.DeleteItem)
        } else {
            var errorState = GoalDetailState.ErrorState.NONE

            // check if goal is in Error state
            if (goalInfo.payPlan.isPaused && goalInfo.estimatedCompleteDate.isNotBlank()) {
                val estimatedCompletionDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate)
                estimatedCompletionDate?.let { date ->
                    if (date.isBeforeNow) {
                        errorState = GoalDetailState.ErrorState.ERROR
                    }
                }
            }

            val recurrenceType : GoalDetailState.GoalIncompleteHeaderItem.PayPlanType = when (goalInfo.payPlan.recurrenceType) {
                PayPlanInfoUtils.PAY_PLAN_DAY -> GoalDetailState.GoalIncompleteHeaderItem.PayPlanType.DAY
                PayPlanInfoUtils.PAY_PLAN_WEEK -> GoalDetailState.GoalIncompleteHeaderItem.PayPlanType.WEEK
                PayPlanInfoUtils.PAY_PLAN_MONTH -> GoalDetailState.GoalIncompleteHeaderItem.PayPlanType.MONTH
                else -> throw IllegalArgumentException("payPlan is of wrong type")
            }
            val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null) {
                goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
            } else {
                0f
            }
            val goalIncompleteHeaderModel = GoalDetailState.GoalIncompleteHeaderItem.GoalIncompleteHeaderModel(
                    fundAmount = goalInfo.fundAmount,
                    goalAmount = goalInfo.amount,
                    frequencyAmount = goalInfo.payPlan.amount,
                    progress = progress,
                    payPlanType = recurrenceType,
                    isPaused = goalInfo.payPlan.isPaused,
                    errorState = errorState,
                    goalCompleteDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate))

            if (errorState == GoalDetailState.ErrorState.ERROR) {
                goalDetailStateList.add(GoalDetailState.ErrorItem)
            }

            goalDetailStateList.add(GoalDetailState.GoalIncompleteHeaderItem(goalIncompleteHeaderModel))

            goalDetailStateList.add(GoalDetailState.SingleTransferItem)
            goalDetailStateList.add(GoalDetailState.GoalPauseItem(goalInfo.payPlan.isPaused, errorState))
            goalDetailStateList.add(GoalDetailState.EditItem)
            goalDetailStateList.add(GoalDetailState.DeleteItem)
        }

        return goalDetailStateList
    }
}

sealed class GoalDetailState {
    enum class ErrorState {
        ERROR,
        NONE
    }
    class GoalCompleteHeaderItem(val fundAmount: BigDecimal) : GoalDetailState()
    class GoalIncompleteHeaderItem(val goalIncompleteHeaderModel: GoalIncompleteHeaderModel) : GoalDetailState() {
        enum class PayPlanType {
            DAY,
            WEEK,
            MONTH
        }

        data class GoalIncompleteHeaderModel(val fundAmount: BigDecimal, val goalAmount: BigDecimal,
                                             val progress: Float, val frequencyAmount: BigDecimal,
                                             val isPaused: Boolean,
                                             val payPlanType: GoalDetailState.GoalIncompleteHeaderItem.PayPlanType,
                                             val goalCompleteDate: DateTime,
                                             val errorState: ErrorState = ErrorState.NONE)
    }
    class GoalPauseItem(val isGoalPaused: Boolean, val errorState: ErrorState) : GoalDetailState()
    object SingleTransferItem : GoalDetailState()
    object EditItem : GoalDetailState()
    object DeleteItem : GoalDetailState()
    object ErrorItem : GoalDetailState()
}

class GoalDetailViewModelFactory(private val goalId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalDetailViewModel(goalId) as T
    }
}

