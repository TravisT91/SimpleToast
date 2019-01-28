package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.feature.goals.GoalsAddStep2ViewModel.ButtonState.HIDE
import com.engageft.feature.goals.GoalsAddStep2ViewModel.ButtonState.SHOW
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import utilGen1.DisplayDateTimeUtils
import java.math.BigDecimal

class GoalsAddStep2ViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    val nextButtonStateObservable = MutableLiveData<ButtonState>()

    lateinit var goalInfoModel: GoalInfoModel

    var goalCompleteByDate: String = ""
    set(value) {
        field = value
        updateNextButtonState()
    }

    var frequencyAmount: String = ""
    set(value) {
        field = value
        updateNextButtonState()
    }

    fun getGoalData(): GoalInfoModel {
        if (goalInfoModel.hasCompleteDate) {
            goalInfoModel.goalCompleteDate = DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(goalCompleteByDate)
        } else {
            goalInfoModel.frequencyAmount = BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(
                    currencyCode = EngageAppConfig.currencyCode,
                    stringWithCurrencySymbol = frequencyAmount))
        }
        return goalInfoModel
    }

    private fun updateNextButtonState() {
        if (isFormValid()) {
            nextButtonStateObservable.value = SHOW
        } else {
            nextButtonStateObservable.value = HIDE
        }
    }

    private fun isFormValid(): Boolean {
        return if (goalInfoModel.hasCompleteDate) {
            goalCompleteByDate.isNotEmpty()
        } else {
            frequencyAmount.isNotEmpty()
        }
    }

    fun hasUnsavedChanges(): Boolean {
       return isFormValid()
    }

    init {
        nextButtonStateObservable.value = HIDE
    }
}