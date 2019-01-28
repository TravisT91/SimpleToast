package com.engageft.feature.goals

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.GoalInfoRequest
import com.engageft.engagekit.rest.request.GoalRequest
import com.engageft.engagekit.rest.request.PayPlanAddUpdateRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.GoalResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.PayPlanResponse
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.PayPlanInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GoalsAddEditConfirmationViewModel: BaseEngageViewModel() {

    lateinit var goalInfoModel: GoalInfoModel

    //TODO(aHashimi) change when working on EDIT Goals
    private val isNewGoal: Boolean = true

    fun onSaveGoal() {
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        val purseId = LoginResponseUtils.getCurrentCard(response).purseId
                        saveNewGoal(purseId)
                    } else {
                        dialogInfoObservable.value = DialogInfo()
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }

    private fun getPayPlanInfo(recurrenceType: String): PayPlanInfo {
        val payPlanInfo = PayPlanInfo()

        when (recurrenceType) {
            PayPlanInfoUtils.PAY_PLAN_ANNUAL, PayPlanInfoUtils.PAY_PLAN_QUARTER -> {
                payPlanInfo.dayOfMonth = goalInfoModel.startDate!!.dayOfMonth
                payPlanInfo.monthOfYear = goalInfoModel.startDate!!.monthOfYear
                payPlanInfo.dayOfWeek = null
            }
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
            PayPlanInfoUtils.PAY_PLAN_DAY, PayPlanInfoUtils.PAY_PLAN_PAYCHECK -> {
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

    private fun saveNewGoal(purseId: Long) {

        val goalInfo = GoalInfoRequest(
                goalName = goalInfoModel.goalName,
                goalAmount = goalInfoModel.goalAmount,
                payPlan = getPayPlanInfo(goalInfoModel.recurrenceType),
                purseId = purseId)

        if (goalInfoModel.hasCompleteDate) {
            val goalDateString = BackendDateTimeUtils.getIso8601String(goalInfoModel.goalCompleteDate!!)
            goalInfo.completeDate = goalDateString
            goalInfo.estimatedCompleteDate = goalDateString
            // let server set payPlan amount based on completion date
//                goalInfo.payPlan.amount = BigDecimal(BigInteger.ZERO)
        } else {
            goalInfo.completeDate = null
            goalInfo.estimatedCompleteDate = null
            // set payPlan amount based on user input
            goalInfo.payPlan.amount = goalInfoModel.frequencyAmount
        }

        saveGoalInfo(goalInfo)
    }

    private fun saveGoalInfo(goalInfo: GoalInfo) {
        progressOverlayShownObservable.value = true

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
                        progressOverlayShownObservable.value = false
                        handleBackendErrorForForms(response, "$TAG: Failed to create/save a goal")
                    }
                }, { e ->
                    // reset payPlanInfo so user can proceed
                    goalInfo.payPlan = payPlanInfo
                    progressOverlayShownObservable.value = false
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
                                progressOverlayShownObservable.value = false
                                EngageService.getInstance().storageManager.clearGoalsResponse(LoginResponseUtils.getCurrentCard(EngageService.getInstance().storageManager.loginResponse)!!)
                                EngageService.getInstance().storageManager.removeDashboardResponse()
                                // onGoalSaved()
                            } else {
                                if (isNewGoal) {
                                    // remove newly-created goal from the server, since it has no payPlan
                                    cleanupGoalAfterPayPlanSubmissionFailure(goalId)
                                } else {
                                    progressOverlayShownObservable.value = false
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
                                progressOverlayShownObservable.value = false
                            }
                            // reset payPlanInfo so user can proceed
                            goalInfo.payPlan = payPlanInfo
                            handleThrowable(e)
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
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (!response.isSuccess) {
                                handleBackendErrorForForms(response, "$TAG: Failed to remove just-created goal after unsuccessful save of payplan")
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }

    companion object {
        private const val TAG = "GoalsAddEditConfirmationViewModel"
    }
}