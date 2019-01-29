package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.GoalInfoRequest
import com.engageft.engagekit.rest.request.GoalRequest
import com.engageft.engagekit.rest.request.PayPlanAddUpdateRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.GoalResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.PayPlanResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.PayPlanInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GoalsAddEditConfirmationViewModel: BaseEngageViewModel() {
    enum class GoalSuccessState {
        SUCCESS
    }
    val successStateObservable = MutableLiveData<GoalSuccessState>()
    lateinit var goalInfoModel: GoalInfoModel

    //TODO(aHashimi) Handle when working on EDIT Goals
    private val isNewGoal: Boolean = true

    fun onSaveGoal() {
        showProgressOverlayImmediate()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        // don't hide progressOverlay
                        val purseId = LoginResponseUtils.getCurrentCard(response).purseId
                        saveGoalInfo(getNewGoalInfo(purseId))
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

    private fun getNewGoalInfo(purseId: Long) : GoalInfo {
        val goalInfoRequest = GoalInfoRequest(
                goalName = goalInfoModel.goalName,
                goalAmount = goalInfoModel.goalAmount,
                payPlan = getPayPlanInfo(goalInfoModel.recurrenceType),
                purseId = purseId)

        val goalInfo = goalInfoRequest.goalInfo

        if (goalInfoModel.hasCompleteDate) {
            val goalDateString = BackendDateTimeUtils.getIso8601String(goalInfoModel.goalCompleteDate!!)
            goalInfo.completeDate = goalDateString
            goalInfo.estimatedCompleteDate = goalDateString
        } else {
            goalInfo.completeDate = null
            goalInfo.estimatedCompleteDate = null
            // set payPlan amount based on user input
            goalInfo.payPlan.amount = goalInfoModel.frequencyAmount
        }
        return goalInfo
    }

    private fun getPayPlanInfo(recurrenceType: String): PayPlanInfo {
        val payPlanInfo = PayPlanInfo()

        when (recurrenceType) {
            PayPlanInfoUtils.PAY_PLAN_MONTH -> {
                payPlanInfo.dayOfMonth = goalInfoModel.startDate!!.dayOfMonth
                payPlanInfo.monthOfYear = null
                payPlanInfo.dayOfWeek = null
            }
            PayPlanInfoUtils.PAY_PLAN_WEEK -> {
                payPlanInfo.dayOfMonth = null
                payPlanInfo.monthOfYear = null
                payPlanInfo.dayOfWeek = goalInfoModel.dayOfWeek
            }
            PayPlanInfoUtils.PAY_PLAN_DAY -> {
                payPlanInfo.dayOfMonth = null
                payPlanInfo.monthOfYear = null
                payPlanInfo.dayOfWeek = null
            }
            else -> {
                throw IllegalArgumentException("Not a valid payPlan")
            }
        }
        payPlanInfo.recurrenceType = recurrenceType
        return payPlanInfo
    }

    private fun saveGoalInfo(goalInfo: GoalInfo) {
        val payPlanInfo = goalInfo.payPlan
        // PayPlanInfo is set by a follow-up API call, so no need to serialize it here.
        goalInfo.payPlan = null

        compositeDisposable.add(engageApi().postGoalAddUpdate(goalInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // hide the progressOverlay if payPlanInfo is success
                    if (response.isSuccess && response is GoalResponse) {
                        // set payPlanInfo now
                        goalInfo.payPlan = payPlanInfo
                        // Now submit payPlanInfo
                        savePayPlan(response.goal.goalId, goalInfo, payPlanInfo)
                    } else {
                        // reset payPlanInfo so user can proceed
                        goalInfo.payPlan = payPlanInfo
                        dismissProgressOverlayImmediate()
                        handleBackendErrorForForms(response, "$TAG: Failed to create/save a goal")
                    }
                }, { e ->
                    // reset payPlanInfo so user can proceed
                    goalInfo.payPlan = payPlanInfo
                    dismissProgressOverlayImmediate()
                    handleThrowable(e)
                })
        )
    }

    private fun savePayPlan(goalId: Long, goalInfo: GoalInfo, payPlanInfo: PayPlanInfo) {
        compositeDisposable.add(
                getPayPlanApiObservable(payPlanInfo, goalId, goalInfo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is PayPlanResponse) {
                                refreshData()
                            } else {
                                if (isNewGoal) {
                                    // remove newly-created goal from the server, since it has no payPlan
                                    cleanupGoalAfterPayPlanSubmissionFailure(goalId)
                                } else {
                                    dismissProgressOverlayImmediate()
                                }
                                // reset payPlanInfo so user can proceed
                                goalInfo.payPlan = payPlanInfo
                                handleBackendErrorForForms(response, "$TAG: failed submission of payPlan")
                            }
                        }, { e ->
                            if (isNewGoal) {
                                // remove newly-created goal from the server, since it has no payplan
                                cleanupGoalAfterPayPlanSubmissionFailure(goalId)
                            } else {
                                dismissProgressOverlayImmediate()
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
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            dismissProgressOverlayImmediate()
                            // don't show the backend error here since we want the user to see
                            // the payPlan submission failure error
                        }, { e ->
                            dismissProgressOverlayImmediate()
                            handleThrowable(e)
                        })
        )
    }


    private fun refreshData() {
        compositeDisposable.add(EngageService.getInstance().refreshLoginObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // if response is success hide progressOverlay after refreshing goals
                    if (response is LoginResponse) {
                        refreshGoals(LoginResponseUtils.getCurrentCard(response))
                    } else {
                        dismissProgressOverlayImmediate()
                        // take user to Goals Success even if refresh is not successful
                        successStateObservable.value = GoalSuccessState.SUCCESS
                    }
                }, { e ->
                    dismissProgressOverlayImmediate()
                    // take user to Goals Success even if refresh is not successful
                    successStateObservable.value = GoalSuccessState.SUCCESS
                })
        )
    }

    private fun refreshGoals(debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            dismissProgressOverlayImmediate()
                            // take user to Goals Success even if refresh is not successful
                            successStateObservable.value = GoalSuccessState.SUCCESS
                        }, { e ->
                            dismissProgressOverlayImmediate()
                            // take user to Goals Success even if refresh is not successful
                            successStateObservable.value = GoalSuccessState.SUCCESS
                        })
        )
    }

    companion object {
        private const val TAG = "GoalsAddEditConfirmationViewModel"
    }
}