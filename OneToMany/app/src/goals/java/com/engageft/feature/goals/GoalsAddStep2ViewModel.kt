package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.feature.goals.GoalsAddStep2ViewModel.ButtonState.*
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.domain.lookup.RecurrenceType
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import java.math.BigDecimal
import java.math.BigInteger

class GoalsAddStep2ViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    val nextButtonStateObservable = MutableLiveData<ButtonState>()

//    var saveByDate = ObservableField("02/28/2019")
//    var amountSetAside = ObservableField("")
//    var showSaveByDate = ObservableField(false)
//    var showSaveWeekly = ObservableField(false)

    lateinit var goalName: String
    lateinit var goalAmount: BigDecimal
    lateinit var recurrenceType: String
    var startDate: DateTime? = null
    var dayOfWeek: Int = -1

    var hasGoalDateInMind: Boolean = false
    var goalCompleteDate: DateTime = DateTime.now()
    var frequencyAmountBigDecimal = BigDecimal(BigInteger.ZERO)

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

    fun isDataValidAndFormattedCorrectly(): Boolean {
        return if (hasGoalDateInMind) {
            goalCompleteByDate.isNotEmpty()
        } else {
            frequencyAmount.isNotEmpty()
            frequencyAmountBigDecimal = BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(
                    currencyCode = EngageAppConfig.currencyCode,
                    stringWithCurrencySymbol = frequencyAmount))
            true
        }
    }

    private fun updateNextButtonState() {
        if (isFormValid()) {
            nextButtonStateObservable.value = SHOW
        } else {
            nextButtonStateObservable.value = HIDE
        }
    }

    private fun isFormValid(): Boolean {
        return if (hasGoalDateInMind) {
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
//        saveByDate.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
//            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
//                if (saveByDate.get()!!.isNotEmpty()) {
//
//                } else {
//
//                }
//            }
//        })
//
//        amountSetAside.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
//            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
//                if (amountSetAside.get()!!.isNotEmpty()) {
//
//                }
//            }
//        })
    }
}