package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.fis.pscu.feature.BaseEngageViewModel

class GoalsAddStep1ViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    var goalName = ObservableField("")
    var goalAmount = ObservableField("")
    var savingsFrequency = ObservableField("")
    var startDate = ObservableField("")
    var dayOfWeek = ObservableField("")
    var goalDateInMind = ObservableField("")
    var showStartDate = ObservableField(false)
    var showDayOfWeek = ObservableField(false)

    var hasGoalDateInMind = false

    val nextButtonStateObservable = MutableLiveData<ButtonState>()

    init {
        nextButtonStateObservable.value = ButtonState.HIDE

        goalDateInMind.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                hasGoalDateInMind = goalDateInMind.get()!! == YES
            }
        })
    }

    private companion object {
        const val YES = "Yes"
    }
}