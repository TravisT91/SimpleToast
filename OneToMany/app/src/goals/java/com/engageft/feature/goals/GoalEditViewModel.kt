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
import com.engageft.feature.budgets.extension.isEqualTo
import com.engageft.feature.goals.utils.PayPlanType
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

    private var goalInfo: GoalInfo? = null

    lateinit var goalInfoModel: GoalInfoModel
        private set

    var goalName = ObservableField("")
    var goalAmount = ObservableField("")
    var frequencyAmount = ObservableField("")
    var frequencyType = ObservableField("")
    var nextRunDate = ObservableField("")
    var goalCompleteDate = ObservableField("")
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
        goalCompleteDate.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                validateFormAndButtonState()
            }
        })

        initData()
    }

    fun hasUnsavedChanges(): Boolean {
        goalInfo?.let { goalInfo ->
            val amountGoal = if (goalAmount.get()!!.isNotEmpty()) {
                BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, goalAmount.get()!!))
            } else {
                BigDecimal.ZERO
            }

            return goalName.get()!! != goalInfo.name
                    || !goalInfo.amount.isEqualTo(amountGoal)
                    || hasFrequencyTypeChanged()
                    || hasFrequencyAmountOrGoalDateChanged()
        }

        return false
    }

    private fun initData() {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        LoginResponseUtils.getCurrentCard(response)?.let { debitCard -> initGoalInfo(debitCard) }
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

    private fun initGoalInfo(debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is GoalsResponse) {
                                response.goals.find { goalInfo ->
                                    goalInfo.goalId == goalId
                                }?.let { goalInfo ->
                                    this.goalInfo = goalInfo
                                    setUpData(goalInfo)
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
                nextRunDate.set(DateTime(goalInfo.payPlan.nextRunDate).toString(DisplayDateTimeUtils.shortDateFormatter))
            }
        }

        // user had set a complete goal date originally
        if (!goalInfo.completeDate.isNullOrBlank()) {
            hasCompleteGoalDate.set(true)
            goalCompleteDate.set(DateTime(goalInfo.completeDate).toString(DisplayDateTimeUtils.shortDateFormatter))
        } else {
            hasCompleteGoalDate.set(false)
            frequencyAmount.set(StringUtils.formatCurrencyStringWithFractionDigits(goalInfo.payPlan.amount.toString(), true))
        }
    }

    private fun validateFormAndButtonState() {
        if (isFormValid() && hasUnsavedChanges()) {
            setupGoalModelInfo()
            nextButtonStateObservable.value = ButtonState.SHOW
        } else {
            nextButtonStateObservable.value = ButtonState.HIDE
        }
    }

    private fun setupGoalModelInfo() {
        goalInfoModel = GoalInfoModel(
                goalId = goalId,
                goalName = goalName.get()!!,
                goalAmount = getNonFormattedValue(goalAmount.get()!!),
                frequencyAmount = if (!hasCompleteGoalDate.get()!!) getNonFormattedValue(frequencyAmount.get()!!) else null,
                recurrenceType = getRecurrenceType(),
                startDate = if (nextRunDate.get()!!.isEmpty()) null else DisplayDateTimeUtils.shortDateFormatter.parseDateTime(nextRunDate.get()!!),
                dayOfWeek = DisplayDateTimeUtils.getDayOfWeekNumber(dayOfWeek.get()!!),
                goalCompleteDate = if (!hasCompleteGoalDate.get()!!) null else DisplayDateTimeUtils.shortDateFormatter.parseDateTime(goalCompleteDate.get()!!),
                hasCompleteDate = hasCompleteGoalDate.get()!!)
    }

    private fun getNonFormattedValue(currencyValue: String) : BigDecimal {
        return if (currencyValue.isNotEmpty()) {
            BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, currencyValue))
        } else {
            BigDecimal.ZERO
        }
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
            hasGoalCompleteDateOrAmountSet() && hasFrequencyType())

    private fun hasGoalCompleteDateOrAmountSet(): Boolean {
        return if (hasCompleteGoalDate.get()!!) {
            goalCompleteDate.get()!!.isNotEmpty()
        } else {
            frequencyAmount.get()!!.isNotEmpty()
        }
    }

    private fun hasFrequencyType(): Boolean {
        if (frequencyType.get()!! == FREQUENCY_TYPE_DAILY) return true
        if (frequencyType.get()!! == FREQUENCY_TYPE_WEEKLY && dayOfWeek.get()!!.isNotEmpty()) return true
        if (frequencyType.get()!! == FREQUENCY_TYPE_MONTHLY && nextRunDate.get()!!.isNotEmpty()) return true
        return false
    }

    private fun hasFrequencyAmountOrGoalDateChanged() : Boolean {
        goalInfo?.let { goalInfo ->
            return if (hasCompleteGoalDate.get()!!) {
                goalInfo.completeDate != getBackendFormatDateStringFromDisplayFormat(goalCompleteDate.get()!!)
            } else {
                val amountFrequency = if (frequencyAmount.get()!!.isNotEmpty()) {
                    BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, frequencyAmount.get()!!))
                } else {
                    BigDecimal.ZERO
                }
                !goalInfo.payPlan.amount.isEqualTo(amountFrequency)
            }
        }

        return false
    }

    private fun getBackendFormatDateStringFromDisplayFormat(dateString: String): String {
        val date = DisplayDateTimeUtils.shortDateFormatter.parseDateTime(dateString)
        return DisplayDateTimeUtils.yearMonthDayFormatter.print(date).toString()
    }

    private fun hasFrequencyTypeChanged(): Boolean {
        goalInfo?.let { goalInfo ->
            val isNewFrequency = goalInfo.payPlan.recurrenceType != getRecurrenceType().toString()
            if (isNewFrequency) return true

            return when (frequencyType.get()!!) {
                FREQUENCY_TYPE_WEEKLY -> {
                    dayOfWeek.get()!!.isNotEmpty() && goalInfo.payPlan.dayOfWeek != null &&
                            dayOfWeek.get() != DisplayDateTimeUtils.getDayOfWeekStringForNumber(goalInfo.payPlan.dayOfWeek)

                }
                FREQUENCY_TYPE_MONTHLY -> {
                    goalInfo.payPlan.recurrenceType != PayPlanInfoUtils.PAY_PLAN_MONTH || (nextRunDate.get()!!.isNotEmpty()
                            && getBackendFormatDateStringFromDisplayFormat(nextRunDate.get()!!) != goalInfo.payPlan.nextRunDate)
                }
                else -> {
                    false
                }
            }
        }

        return false
    }

    private fun getRecurrenceType(): PayPlanType {
        return when (frequencyType.get()!!) {
            FREQUENCY_TYPE_WEEKLY -> PayPlanType.WEEK
            FREQUENCY_TYPE_MONTHLY -> PayPlanType.MONTH
            else -> PayPlanType.DAY
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