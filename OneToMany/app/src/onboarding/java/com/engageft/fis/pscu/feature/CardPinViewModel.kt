package com.engageft.fis.pscu.feature

import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.fis.pscu.feature.BaseEngageViewModel

class CardPinViewModel: BaseEngageViewModel() {

    enum class CardPinFlow {
        CONFIRM_PIN,
        MISMATCH_PIN,
        SUBMIT_PIN
    }

    val flowObservable = MutableLiveData<CardPinFlow>()

    private var cardPin: Int = 0
    fun validatePin(pin: Int) {
        cardPin = pin
        // todo: proper validation? recheck for
        if (pin.toString().length == 4) {
            flowObservable.value = CardPinFlow.CONFIRM_PIN
        } else {
            dialogInfoObservable.value = CardPinDialogInfo(dialogType = DialogInfo.DialogType.OTHER)
        }
    }

    fun confirmPin(confirmPin: Int) {
        // todo: proper validation? recheck for
        if (cardPin.toString().length == 4 && cardPin == confirmPin) {
            submit()
        }
    }

    private fun submit() {

    }
}
class CardPinDialogInfo(title: String? = null,
                      message: String? = null,
                      tag: String? = null,
                      dialogType: DialogType = DialogType.GENERIC_ERROR,
                      var cardPinDialogType: CardPinDialogInfo.CardPinDialogType = CardPinDialogType.PIN_VALIDATION_ERROR) : DialogInfo(title, message, tag, dialogType) {
    enum class CardPinDialogType {
        PIN_VALIDATION_ERROR
    }
}