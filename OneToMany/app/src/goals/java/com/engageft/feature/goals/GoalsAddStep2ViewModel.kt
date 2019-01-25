package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
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

    val nextButtonStateObservable = MutableLiveData<GoalsAddStep1ViewModel.ButtonState>()

//    var saveByDate = ObservableField("02/28/2019")
//    var amountSetAside = ObservableField("")
//    var showSaveByDate = ObservableField(false)
//    var showSaveWeekly = ObservableField(false)

    lateinit var goalName: String
    lateinit var goalAmount: BigDecimal
    lateinit var recurrenceType: String
    lateinit var startDate: DateTime
    var dayOfWeek: Int = -1

    var hasGoalDateInMind: Boolean = false
    var goalCompleteDate = DateTime.now()
    var frequencyAmountBigDecimal = BigDecimal(BigInteger.ZERO)

    var goalSaveByDate: String = ""
    set(value) {
        field = value
        if (hasGoalDateInMind && field.isNotEmpty()) {
            goalCompleteDate = DisplayDateTimeUtils.shortDateFormatter.parseDateTime(field)
            nextButtonStateObservable.value = GoalsAddStep1ViewModel.ButtonState.SHOW
        } else {
            nextButtonStateObservable.value = GoalsAddStep1ViewModel.ButtonState.HIDE
        }
    }

    var frequencyAmount: String = ""
    set(value) {
        field = value
        if (!hasGoalDateInMind && field.isNotEmpty()) {
            frequencyAmountBigDecimal = BigDecimal(field)
            nextButtonStateObservable.value = GoalsAddStep1ViewModel.ButtonState.SHOW
        } else {
            nextButtonStateObservable.value = GoalsAddStep1ViewModel.ButtonState.HIDE
        }    }

//    fun updateButtonState() {
//        if (hasGoalDateInMind && goalSaveByDate.isNotEmpty())
//    }
    init {
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