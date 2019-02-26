package com.engageft.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.GoalInfoRequest
import com.engageft.engagekit.rest.request.GoalRequest
import com.engageft.engagekit.rest.request.PayPlanAddUpdateRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.feature.goals.utils.GoalConstants
import com.engageft.feature.goals.utils.PayPlanType
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.GoalResponse
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.PayPlanResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.GoalTargetInfo
import com.ob.ws.dom.utility.PayPlanInfo
import io.reactivex.schedulers.Schedulers

class GoalAddEditConfirmationViewModel(val goalInfoModel: GoalInfoModel): BaseGoalsViewModel() {
    enum class GoalEvent {
        ADD,
        EDIT
    }

    val addEditSuccessObservable = SingleLiveEvent<Unit>()

    private var eventType: GoalEvent = GoalEvent.ADD

    init {
        if (goalInfoModel.goalId != GoalConstants.GOAL_ID_DEFAULT) {
            eventType = GoalEvent.EDIT
        }
    }

    fun onSaveGoal() {
        showProgressOverlayImmediate()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    // don't hide progressOverlay
                    if (response is LoginResponse) {
                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        when (eventType) {
                            GoalEvent.ADD -> {
                                val newGoalInfo = GoalInfo()
                                newGoalInfo.purseId = currentCard.purseId
                                saveGoalInfo(adjustGoalInfo(newGoalInfo))
                            }
                            GoalEvent.EDIT -> {
                                currentCard?.let { updateGoal(it) }
                            }
                        }
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

    private fun updateGoal(debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is GoalsResponse) {
                                response.goals.find { goalInfo ->
                                    goalInfo.goalId == goalInfoModel.goalId
                                }?.let { goalInfo ->
                                    saveGoalInfo(adjustGoalInfo(goalInfo))
                                }
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

    private fun adjustGoalInfo(goalInfo: GoalInfo) : GoalInfo {
        return goalInfo.apply {
            name = goalInfoModel.goalName
            amount = goalInfoModel.goalAmount

            when (eventType) {
                GoalEvent.ADD -> {
                    val goalTargetInfo = GoalTargetInfo()
                    goalTargetInfo.name = goalInfoModel.goalName
                    goalTargetInfo.amount = goalInfoModel.goalAmount

                    val goalTargetInfoList = ArrayList<GoalTargetInfo>()
                    goalTargetInfoList.add(goalTargetInfo)
                    goalTargets = goalTargetInfoList
                }
                GoalEvent.EDIT -> {
                    if (goalTargets.isNotEmpty()) {
                        // there's one Goal Target per goal
                        val goalTargetInfo = goalInfo.goalTargets[0]
                        goalTargetInfo.name = goalInfoModel.goalName
                        goalTargetInfo.amount = goalInfoModel.goalAmount
                    }
                }
            }

            this.payPlan = getPayPlanInfo(goalInfoModel.recurrenceType)

            if (goalInfoModel.hasCompleteDate) {
                val goalDateString = BackendDateTimeUtils.getIso8601String(goalInfoModel.goalCompleteDate!!)
                completeDate = goalDateString
                estimatedCompleteDate = goalDateString
            } else {
                completeDate = null
                estimatedCompleteDate = null
                // set payPlan amount based on user input
                payPlan.amount = goalInfoModel.frequencyAmount
            }
        }
    }

    private fun getPayPlanInfo(recurrenceType: PayPlanType): PayPlanInfo {
        val payPlanInfo = PayPlanInfo()

        when (recurrenceType) {
            PayPlanType.MONTH -> {
                payPlanInfo.dayOfMonth = goalInfoModel.startDate!!.dayOfMonth
                payPlanInfo.monthOfYear = null
                payPlanInfo.dayOfWeek = null
            }
            PayPlanType.WEEK -> {
                payPlanInfo.dayOfMonth = null
                payPlanInfo.monthOfYear = null
                payPlanInfo.dayOfWeek = goalInfoModel.dayOfWeek
            }
            PayPlanType.DAY -> {
                payPlanInfo.dayOfMonth = null
                payPlanInfo.monthOfYear = null
                payPlanInfo.dayOfWeek = null
            }
        }
        payPlanInfo.recurrenceType = recurrenceType.toString()
        return payPlanInfo
    }

    private fun saveGoalInfo(goalInfo: GoalInfo) {
        val goalInfoToSave = GoalInfoRequest(goalInfo).goalInfo
        val payPlanInfo = goalInfoToSave.payPlan
        // PayPlanInfo is set by a follow-up API call, so no need to serialize it here.
        goalInfoToSave.payPlan = null

        compositeDisposable.add(engageApi().postGoalAddUpdate(goalInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    // hide the progressOverlay if payPlanInfo is success
                    if (response.isSuccess && response is GoalResponse) {
                        // set payPlanInfo now
                        goalInfoToSave.payPlan = payPlanInfo
                        // Now submit payPlanInfo
                        savePayPlan(response.goal.goalId, goalInfo, payPlanInfo)
                    } else {
                        // reset payPlanInfo so user can proceed
                        goalInfoToSave.payPlan = payPlanInfo
                        dismissProgressOverlay()
                        handleBackendErrorForForms(response, "$TAG: Failed to create/save a goal")
                    }
                }, { e ->
                    // reset payPlanInfo so user can proceed
                    goalInfoToSave.payPlan = payPlanInfo
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private fun savePayPlan(goalId: Long, goalInfo: GoalInfo, payPlanInfo: PayPlanInfo) {
        compositeDisposable.add(
                getPayPlanApiObservable(payPlanInfo, goalId, goalInfo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is PayPlanResponse) {
                                refreshLoginResponse(addEditSuccessObservable)
                            } else {
                                if (eventType == GoalEvent.ADD) {
                                    // remove newly-created goal from the server, since it has no payPlan
                                    cleanupGoalAfterPayPlanSubmissionFailure(goalId)
                                } else {
                                    dismissProgressOverlay()
                                }
                                // reset payPlanInfo so user can proceed
                                goalInfo.payPlan = payPlanInfo
                                handleBackendErrorForForms(response, "$TAG: failed submission of payPlan")
                            }
                        }, { e ->
                            if (eventType == GoalEvent.ADD) {
                                // remove newly-created goal from the server, since it has no payplan
                                cleanupGoalAfterPayPlanSubmissionFailure(goalId)
                            } else {
                                dismissProgressOverlay()
                                // reset payPlanInfo so user can proceed
                                goalInfo.payPlan = payPlanInfo
                                handleThrowable(e)
                            }
                        })
        )
    }

    private fun getPayPlanApiObservable(payPlanInfo: PayPlanInfo, goalId: Long, goalInfo: GoalInfo): io.reactivex.Observable<BasicResponse> {
        lateinit var observable: io.reactivex.Observable<BasicResponse>
        lateinit var request: PayPlanAddUpdateRequest
        when (payPlanInfo.recurrenceType) {
            PayPlanInfoUtils.PAY_PLAN_MONTH -> {
                request = PayPlanAddUpdateRequest.newInstanceForGoalMonthly(goalId, payPlanInfo)
                observable = engageApi().postPayPlanAddUpdateMonthly(request.fieldMap)
            }
            PayPlanInfoUtils.PAY_PLAN_WEEK -> {
                request = PayPlanAddUpdateRequest.newInstanceForGoalWeekly(goalId, payPlanInfo)
                observable = engageApi().postPayPlanAddUpdateWeekly(request.fieldMap)
            }
            PayPlanInfoUtils.PAY_PLAN_DAY -> {
                request = PayPlanAddUpdateRequest.newInstanceForGoalDailyAndPaycheck(goalId, payPlanInfo)
                observable = engageApi().postPayPlanAddUpdateDaily(request.fieldMap)
            }
            else -> {
                request = PayPlanAddUpdateRequest.newInstanceForGoalDailyAndPaycheck(goalId, payPlanInfo)
                observable = engageApi().postPayPlanAddUpdateDaily(request.fieldMap)
            }
        }

        return observable
    }

    private fun cleanupGoalAfterPayPlanSubmissionFailure(goalId: Long) {
        val request = GoalRequest(goalId)
        compositeDisposable.add(
                engageApi().postGoalRemove(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({
                            dismissProgressOverlay()
                            // don't show the backend error here since we want the user to see
                            // the payPlan submission failure error
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    companion object {
        private const val TAG = "GoalAddEditConfirmationViewModel"
    }
}

class GoalAddEditConfirmationViewModelFactory(private val goalInfoModel: GoalInfoModel) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalAddEditConfirmationViewModel(goalInfoModel) as T
    }
}