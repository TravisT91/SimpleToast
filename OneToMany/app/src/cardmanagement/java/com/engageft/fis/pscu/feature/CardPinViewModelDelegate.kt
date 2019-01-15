package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.isDigitsOnly

/**
 * CardPinViewModelDelegate
 * <p>
 * Delegate object for multiple viewModels to share.
 * </p>
 * Created by joeyhutchins on 1/8/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CardPinViewModelDelegate(private val engageViewModel: BaseEngageViewModel, private val cardPinViewModelListener: CardPinViewModelListener) {
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
        DIGIT_DELETED,
        DIGITS_CLEARED
    }

    val cardPinStateObservable = MutableLiveData<CardPinState>()
    val cardPinDigitsState = MutableLiveData<Pair<PinDigits, Int>>()
    val productCardViewModelDelegate = ProductCardViewDelegate(engageViewModel)

    var pinNumber: String = ""
        set(value) {
            val temp = field
            field = value

            if (field.length == 1 && cardPinStateObservable.value == CardPinState.MISMATCH_PIN) {
                cardPinStateObservable.value = CardPinState.INITIAL_ENTER_PIN
            }

            if (field.length in 0..PIN_LENGTH) {
                if (temp.length > 1 && field.isEmpty()) {
                    cardPinDigitsState.value = Pair(PinDigits.DIGITS_CLEARED, 0)
                } else {
                    when {
                        field.length > temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_ADDED, field.length - 1)
                        field.length < temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_DELETED, temp.length - 1)
                    }
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
                if (temp.length > 1 && field.isEmpty()) {
                    cardPinDigitsState.value = Pair(PinDigits.DIGITS_CLEARED, 0)
                } else {
                    when {
                        field.length > temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_ADDED, field.length - 1)
                        field.length < temp.length -> cardPinDigitsState.value = Pair(PinDigits.DIGIT_DELETED, temp.length - 1)
                    }
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
            val result = pinNumber.toInt()
            
            // reset...
            cardPinStateObservable.value = CardPinState.INITIAL_ENTER_PIN
            pinNumber = ""
            confirmPinNumber = ""

            cardPinViewModelListener.onPostPin(result)
        } else {
            // this is redundant for CardPinFragment, technically this should not happen
            cardPinStateObservable.value = CardPinState.INVALID_PIN
        }
    }

    private fun isValidPin(pin: String): Boolean {
        return pin.isDigitsOnly() && pin.length == PIN_LENGTH
    }
}

interface CardPinViewModelListener {
    fun onPostPin(pinNumber: Int)
}