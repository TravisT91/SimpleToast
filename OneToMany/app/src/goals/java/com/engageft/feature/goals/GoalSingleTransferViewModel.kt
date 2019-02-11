package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.adapters.SeekBarBindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.fis.pscu.feature.BaseEngageViewModel

class GoalSingleTransferViewModel(goalId: Long): BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    val nextButtonStateObservable = MutableLiveData<ButtonState>()

    val from = ObservableField("")
    val to = ObservableField("")
    val amount = ObservableField("")

    private val fromOnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

        }
    }
    private val toOnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

        }
    }
    private val amountOnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

        }
    }

    init {
        nextButtonStateObservable.value = ButtonState.HIDE

        from.addOnPropertyChangedCallback(fromOnPropertyChangedCallback)
        to.addOnPropertyChangedCallback(fromOnPropertyChangedCallback)
        amount.addOnPropertyChangedCallback(fromOnPropertyChangedCallback)
    }

    fun hasUnsavedChanges(): Boolean {
        return false
    }
}

//todo make this to return a generic viewModel?
class GoalSingleTransferViewModelFactory(private val goalId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalSingleTransferViewModel(goalId) as T
    }
}