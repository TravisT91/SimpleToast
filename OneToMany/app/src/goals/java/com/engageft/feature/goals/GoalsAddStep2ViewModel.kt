package com.engageft.feature.goals

import androidx.databinding.ObservableField
import com.engageft.fis.pscu.feature.BaseEngageViewModel

class GoalsAddStep2ViewModel: BaseEngageViewModel() {

    var saveByDate = ObservableField("")
    var saveWeekly = ObservableField("")
    var showsaveByDate = ObservableField(true)
    var showSaveWeekly = ObservableField(true)

}