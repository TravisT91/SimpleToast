package com.engageft.feature.goals

import android.util.Log
import androidx.databinding.ObservableField
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.GoalRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.PayPlanResponse
import com.engageft.engagekit.rest.request.PayPlanAddUpdateRequest
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.engageft.fis.pscu.feature.utils.createGoalInfo
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.PayPlanInfo
import com.ob.ws.dom.GoalResponse
import com.ob.ws.dom.LoginResponse
import org.joda.time.DateTime
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.engageApi
import utilGen1.DisplayDateTimeUtils
import java.math.BigDecimal


class GoalsAddStep2ViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    val nextButtonStateObservable = MutableLiveData<GoalsAddStep1ViewModel.ButtonState>()

    var saveByDate = ObservableField("02/28/2019")
    var amountSaveWeekly = ObservableField("")
    var showSaveByDate = ObservableField(true)
    var showSaveWeekly = ObservableField(true)

    var goalName: String = ""
    var goalAmount: String = ""
    var recurrenceType: String = ""
    var startDate: DateTime = DateTime.now()
    var dayOfWeek: Int = -1

    private var isNewGoal = false
//    fun isValidGoalName(goalName: String): Boolean {
//        return !TextUtils.isEmpty(goalName) && ValidationUtils.isMax20Chars(goalName)
//    }
    init {
        saveByDate.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (saveByDate.get()!!.isNotEmpty()) {

                } else {

                }
            }
        })

        amountSaveWeekly.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (amountSaveWeekly.get()!!.isNotEmpty()) {
                }
            }
        })
    }

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

    private fun saveNewGoal(purseId: Long) {
        val goalInfo = createGoalInfo(goalName = goalName,
                goalAmount = goalAmount,
                recurrenceType = recurrenceType,
                startDate = startDate,
                dayOfWeek = dayOfWeek,
                purseId = purseId)

        when {
            saveByDate.get()!!.isNotEmpty() -> {
                val goalDate = DisplayDateTimeUtils.shortDateFormatter.parseDateTime(saveByDate.get()!!)
                val goalDateString = BackendDateTimeUtils.getIso8601String(goalDate)
                goalInfo.completeDate = goalDateString
                goalInfo.estimatedCompleteDate = goalDateString
                // let server set payPlan amount based on completion date
                goalInfo.payPlan.amount = null
            }
            amountSaveWeekly.get()!!.isNotEmpty() -> {
                goalInfo.completeDate = null
                goalInfo.estimatedCompleteDate = null
                // set payPlan amount based on user input
                goalInfo.payPlan.amount = BigDecimal.valueOf(amountSaveWeekly.get()!!.toLong())
            }
            else -> throw IllegalArgumentException("Must set Complete Goal Date or PayPlan amount")
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
                            if (response.isSuccess && response is GoalResponse) {

                                response.goal?.let { goalInfo ->
                                    // This should always be true.
                                    goalInfo.estimatedCompleteDate = goalInfo.estimatedCompleteDate
                                }
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
//        savePayPlan(goalId, goalInfo, payPlanInfo)
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
                                    // remove newly-created goal from the server, since it has no payplan
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

    private fun cleanupGoalAfterPayPlanSubmissionFailure(goalId: Long) {
        Log.e(TAG, "About to remove just-created bill after unsuccessful save of payPlan")
        val request = GoalRequest(goalId)
        compositeDisposable.add(
                engageApi().postGoalRemove(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess) {
                                Log.e(TAG, "Removed just-created goal after unsuccessful save of payplan")
                            } else {
                                Log.e(TAG, "Failed to remove just-created goal after unsuccessful save of payplan")
                                handleBackendErrorForForms(response, "$TAG: Failed to remove just-created goal after unsuccessful save of payplan")
                            }
                        }, { e ->
                            Log.e(TAG, "Failed to remove just-created goal after unsuccessful save of payplan: " + e.message)
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }

    private companion object {
        const val TAG = "GoalsAddStep2ViewModel"
    }
}