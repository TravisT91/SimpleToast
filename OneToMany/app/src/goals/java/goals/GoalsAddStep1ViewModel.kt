package goals

import androidx.databinding.ObservableField
import com.engageft.fis.pscu.feature.BaseEngageViewModel

class GoalsAddStep1ViewModel: BaseEngageViewModel() {

    var goalName = ObservableField("")
    var goalAmount = ObservableField("")
    var savingsFrequency = ObservableField("")
    var startDate = ObservableField("")
    var dayOfWeek = ObservableField("")
    var showStartDate = ObservableField(false)
    var showDayOfWeek = ObservableField(false)
}