package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils
import java.math.BigDecimal

class GoalEditViewModel(val goalId: Long): BaseEngageViewModel() {

    private lateinit var goalInfo: GoalInfo

    lateinit var goalInfoModel: GoalInfoModel
    private set

    var goalName = ObservableField("")
    var goalAmount = ObservableField("")
    var frequencyAmount = ObservableField("")
    var frequencyType = ObservableField("")
    var nextRunDate = ObservableField("")
    var completeGoalDate = ObservableField("")
    var dayOfWeek = ObservableField("")
    var showNextRunDate = ObservableField(false)
    var showDayOfWeek = ObservableField(false)

    var hasCompleteGoalDate = ObservableField(false)

    enum class ButtonState {
        SHOW,
        HIDE
    }

    val nextButtonStateObservable = MutableLiveData<ButtonState>()

    init {
        nextButtonStateObservable.value = ButtonState.HIDE

        goalName.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                validateFormAndButtonState()
            }
        })
        goalAmount.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                validateFormAndButtonState()
            }
        })
        frequencyAmount.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                validateFormAndButtonState()
            }
        })
        frequencyType.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                onFrequencyTypeChanged()
                validateFormAndButtonState()
            }
        })
        nextRunDate.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                validateFormAndButtonState()
            }
        })
        dayOfWeek.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                validateFormAndButtonState()
            }
        })

        initGoalData()
    }

    fun validateFormAndButtonState() {
        if (isFormValid() && hasUnsavedChanges()) {
            nextButtonStateObservable.value = ButtonState.SHOW
            setupGoalModelInfo()
        } else {
            nextButtonStateObservable.value = ButtonState.HIDE
        }
    }

    private fun setupGoalModelInfo() {
        val amountGoal = BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, goalAmount.get()!!))
        val amountFrequency = BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, frequencyAmount.get()!!))
        goalInfoModel = GoalInfoModel(
                goalName = goalName.get()!!,
                goalAmount = amountGoal,
                frequencyAmount = amountFrequency,
                recurrenceType = getRecurrenceType(),
                startDate = if (nextRunDate.get()!!.isEmpty()) null else DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(nextRunDate.get()!!),
                dayOfWeek = DisplayDateTimeUtils.getDayOfWeekNumber(dayOfWeek.get()!!),
                hasCompleteDate = false)
    }

    private fun onFrequencyTypeChanged() {
        when (frequencyType.get()!!) {
            FREQUENCY_TYPE_WEEKLY -> {
                showDayOfWeek.set(true)
                showNextRunDate.set(false)
            }
            FREQUENCY_TYPE_MONTHLY -> {
                showNextRunDate.set(true)
                showDayOfWeek.set(false)
            }
            else -> {
                showDayOfWeek.set(false)
                showNextRunDate.set(false)
            }
        }
    }

    private fun isFormValid() = (goalName.get()!!.isNotEmpty() && goalAmount.get()!!.isNotEmpty() &&
            frequencyAmount.get()!!.isNotEmpty() && hasFrequencyType())

    private fun hasFrequencyType(): Boolean {
        if (frequencyType.get()!! == FREQUENCY_TYPE_DAILY) return true
        if (frequencyType.get()!! == FREQUENCY_TYPE_WEEKLY && dayOfWeek.get()!!.isNotEmpty()) return true
        if (frequencyType.get()!! == FREQUENCY_TYPE_MONTHLY && nextRunDate.get()!!.isNotEmpty()) return true
        return false
    }

    private fun initGoalData() {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        initGoal(LoginResponseUtils.getCurrentCard(response))
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

    private fun initGoal(debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is GoalsResponse) {
                                for (goalInfo in response.goals) {
                                    if (goalInfo.goalId == goalId) {
                                        this.goalInfo = goalInfo
                                        setUpData(goalInfo)
                                        break
                                    }
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    private fun setUpData(goalInfo: GoalInfo) {
        goalName.set(goalInfo.name)
        goalAmount.set(StringUtils.formatCurrencyStringWithFractionDigits(goalInfo.amount.toString(), true))
        frequencyAmount.set(StringUtils.formatCurrencyStringWithFractionDigits(goalInfo.payPlan.amount.toString(), true))
        when(goalInfo.payPlan.recurrenceType) {
            PayPlanInfoUtils.PAY_PLAN_DAY -> {
                frequencyType.set(FREQUENCY_TYPE_DAILY)
                showDayOfWeek.set(false)
                showNextRunDate.set(false)
            }
            PayPlanInfoUtils.PAY_PLAN_WEEK -> {
                frequencyType.set(FREQUENCY_TYPE_WEEKLY)
                dayOfWeek.set(DisplayDateTimeUtils.getDayOfWeekStringForNumber(goalInfo.payPlan.dayOfWeek))
                showDayOfWeek.set(true)
                showNextRunDate.set(false)
            }
            PayPlanInfoUtils.PAY_PLAN_MONTH -> {
                frequencyType.set(FREQUENCY_TYPE_MONTHLY)
                showDayOfWeek.set(false)
                showNextRunDate.set(true)
                nextRunDate.set(DisplayDateTimeUtils.getMediumFormatted(DateTime(goalInfo.payPlan.nextRunDate)))
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        val amountGoal = if (goalAmount.get()!!.isNotEmpty()) {
            BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, goalAmount.get()!!))
        } else {
            BigDecimal.ZERO
        }

        val amountFrequency = if (frequencyAmount.get()!!.isNotEmpty()) {
            BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, frequencyAmount.get()!!))
        } else {
            BigDecimal.ZERO
        }

        return goalName.get()!! != goalInfo.name || goalInfo.amount.compareTo(amountGoal) != 0
                || goalInfo.fundAmount.compareTo(amountFrequency) != 0 || hasFrequencyTypeChanged()
    }

    private fun hasFrequencyTypeChanged(): Boolean {
        when (frequencyType.get()!!) {
            FREQUENCY_TYPE_DAILY -> {
                return goalInfo.payPlan.recurrenceType != PayPlanInfoUtils.PAY_PLAN_DAY
            }
            FREQUENCY_TYPE_WEEKLY -> {
                if (goalInfo.payPlan.recurrenceType != PayPlanInfoUtils.PAY_PLAN_WEEK || (goalInfo.payPlan.dayOfWeek != null
                                && dayOfWeek.get()!!.isNotEmpty() && dayOfWeek.get() != DisplayDateTimeUtils.getDayOfWeekStringForNumber(goalInfo.payPlan.dayOfWeek))) {
                    return true
                }
            }
            FREQUENCY_TYPE_MONTHLY -> {
                var dateFormat = ""
                if (nextRunDate.get()!!.isNotEmpty()) {
                    val date = DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(nextRunDate.get()!!)
                    dateFormat = DisplayDateTimeUtils.yearMonthDayFormatter.print(date).toString()
                }

                if (goalInfo.payPlan.recurrenceType != PayPlanInfoUtils.PAY_PLAN_MONTH || (nextRunDate.get()!!.isNotEmpty()
                                && dateFormat != goalInfo.payPlan.nextRunDate)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getRecurrenceType(): String {
        return when (frequencyType.get()!!) {
            FREQUENCY_TYPE_WEEKLY -> {
                PayPlanInfoUtils.PAY_PLAN_WEEK
            }
            FREQUENCY_TYPE_MONTHLY -> {
                PayPlanInfoUtils.PAY_PLAN_MONTH
            }
            else -> PayPlanInfoUtils.PAY_PLAN_DAY
        }
    }

    companion object {
        const val FREQUENCY_TYPE_DAILY = "Daily"
        const val FREQUENCY_TYPE_WEEKLY = "Weekly"
        const val FREQUENCY_TYPE_MONTHLY = "Monthly"
    }
}

class GoalEditViewModelFactory(private val goalId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalEditViewModel(goalId) as T
    }
}