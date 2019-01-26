package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import java.lang.IllegalStateException
import java.math.BigDecimal

class GoalsAddStep1ViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    var goalName = ObservableField("")
    var goalAmount = ObservableField("")
    var frequency = ObservableField("")
    var startDate = ObservableField("")
    var dayOfWeek = ObservableField("")
    var goalCompleteDate = ObservableField("")
    var showStartDate = ObservableField(false)
    var showDayOfWeek = ObservableField(false)

    var hasGoalDateInMind = false
    var recurrenceType: String = ""
    var startOnDate: DateTime? = null
    var dayOfWeekInt: Int = 0
    var amount: BigDecimal = BigDecimal.ZERO

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
        frequency.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                onFrequencyTypeChanged()
                validateFormAndButtonState()
            }
        })
        startDate.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
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
    }

    private fun onFrequencyTypeChanged() {
        when (frequency.get()!!) {
            FREQUENCY_TYPE_WEEKLY -> {
                showDayOfWeek.set(true)
                showStartDate.set(false)
            }
            FREQUENCY_TYPE_MONTHLY -> {
                showStartDate.set(true)
                showDayOfWeek.set(false)
            }
            else -> {
                showDayOfWeek.set(false)
                showStartDate.set(false)
            }
        }
    }

    fun validateFormAndButtonState() {
        if (isFormValid()) {
            nextButtonStateObservable.value = ButtonState.SHOW
        } else {
            nextButtonStateObservable.value = ButtonState.HIDE
        }
    }

    fun isDataValidAndFormatted(): Boolean {
        if (isFormValid()) {
            hasGoalDateInMind = goalCompleteDate.get()!! == YES
            amount = BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(
                    currencyCode = EngageAppConfig.currencyCode,
                    stringWithCurrencySymbol = goalAmount.get()!!))
            when (frequency.get()!!) {
                FREQUENCY_TYPE_DAILY -> {
                    recurrenceType = PayPlanInfoUtils.PAY_PLAN_DAY
                }
                FREQUENCY_TYPE_WEEKLY -> {
                    recurrenceType = PayPlanInfoUtils.PAY_PLAN_WEEK
                    dayOfWeekInt = DisplayDateTimeUtils.getDayOfWeekNumber(dayOfWeek.get()!!)
                }
                FREQUENCY_TYPE_MONTHLY -> {
                    recurrenceType = PayPlanInfoUtils.PAY_PLAN_MONTH
                    startOnDate = DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(startDate.get()!!)
                }
                else -> {
                    throw IllegalStateException("Wrong frequency type")
                }
            }
            return true
        }
        return false
    }

    private fun hasFrequencyType(): Boolean {
        if (frequency.get()!! == FREQUENCY_TYPE_DAILY) return true
        if (frequency.get()!! == FREQUENCY_TYPE_WEEKLY && dayOfWeek.get()!!.isNotEmpty()) return true
        if (frequency.get()!! == FREQUENCY_TYPE_MONTHLY && startDate.get()!!.isNotEmpty()) return true
        return false
    }

    private fun isFormValid() = (goalName.get()!!.isNotEmpty() && goalAmount.get()!!.isNotEmpty()
            && hasFrequencyType() && goalCompleteDate.get()!!.isNotEmpty())

    fun hasUnsavedChanges(): Boolean {
        if (goalName.get()!!.isNotEmpty() || goalAmount.get()!!.isNotEmpty() ||
                (frequency.get()!!.isNotEmpty() && hasFrequencyType()) || goalCompleteDate.get()!!.isNotEmpty()) {
            return true
        }
        return false
    }

    private companion object {
        const val YES = "Yes"
        const val FREQUENCY_TYPE_DAILY = "Daily"
        const val FREQUENCY_TYPE_WEEKLY = "Weekly"
        const val FREQUENCY_TYPE_MONTHLY = "Monthly"
    }
}
