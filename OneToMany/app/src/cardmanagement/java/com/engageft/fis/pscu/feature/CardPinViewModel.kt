package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.isDigitsOnly
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
/**
 * CardPinViewModel
 * <p>
 * ViewModel for handling changing card PIN number
 * </p>
 * Created by Atia Hashimi on 12/3/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardPinViewModel: BaseEngageViewModel() {

    companion object {
        private const val PIN_LENGTH = 4
    }

    enum class CardPinState {
        MISMATCH_PIN,
        CONFIRM_PIN,
        INITIAL_ENTER_PIN,
        INVALID_PIN,
    }

    enum class PinDigits {
        DIGIT_ADDED,
        DIGIT_DELETED
    }

    val cardPinStateObservable = MutableLiveData<CardPinState>()
    val cardPinDigitsState = MutableLiveData<Pair<PinDigits, Int>>()

    var pinNumber: String = ""
    set(value) {
        val temp = field
        field = value

        if (field.length == 1 && cardPinStateObservable.value == CardPinState.MISMATCH_PIN) {
            cardPinStateObservable.value = CardPinState.INITIAL_ENTER_PIN
        }

        if (field.length in 0..PIN_LENGTH) {
            when {
                field.length > temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_ADDED, field.length - 1)
                field.length < temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_DELETED, temp.length - 1)
            }

            if (field.length == PIN_LENGTH) {
                cardPinStateObservable.value = CardPinState.CONFIRM_PIN
            }
        }
    }

    var confirmPinNumber: String = ""
    set(value) {
        val temp = field
        field = value

        if (field.length in 0..PIN_LENGTH) {
            when {
                field.length > temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_ADDED, field.length - 1)
                field.length < temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_DELETED, temp.length - 1)
            }

            if (field.length == PIN_LENGTH) {
               if (field == pinNumber) {
                   submit()
               } else {
                   cardPinStateObservable.value = CardPinState.MISMATCH_PIN
               }
            }
        }
    }

    init {
        cardPinStateObservable.value = CardPinState.INITIAL_ENTER_PIN
    }

    private fun submit() {
        if (isValidPin(pinNumber)) {
            val currentCard = EngageService.getInstance().storageManager.currentCard

            progressOverlayShownObservable.value = true
            compositeDisposable.add(
                    EngageService.getInstance().setCardPinObservable(currentCard, pinNumber.toInt())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                progressOverlayShownObservable.value = false
                                if (response.isSuccess) {
                                    dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_SUCCESS)
                                } else {
                                    dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.message)
                                }
                            }, { e ->
                                progressOverlayShownObservable.value = false
                                handleThrowable(e)
                            })
            )
        } else {
            // this is redundant for CardPinFragment, technically this should not happen
            cardPinStateObservable.value = CardPinState.INVALID_PIN
        }
    }

    private fun isValidPin(pin: String): Boolean {
        return pin.isDigitsOnly() && pin.length == PIN_LENGTH
    }
}