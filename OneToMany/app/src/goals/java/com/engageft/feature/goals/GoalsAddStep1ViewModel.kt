package com.engageft.feature.goals

import android.widget.Button
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
    var showStartDate = ObservableField(false)
    var showDayOfWeek = ObservableField(false)

    val nextButtonStateObservable = MutableLiveData<ButtonState>()

    init {
        nextButtonStateObservable.value = ButtonState.HIDE

    }

    private companion object {

    }
}