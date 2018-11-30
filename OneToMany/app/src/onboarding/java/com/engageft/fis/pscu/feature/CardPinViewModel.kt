package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.isDigitsOnly
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.config.EngageAppConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class CardPinViewModel: BaseEngageViewModel() {

    enum class CardPinState {
        MISMATCH_PIN,
        CONFIRM_PIN,
        ENTER_PIN,
        INVALID_PIN
    }
    private var pinEntered = ""
    val cardPinStateObservable = MutableLiveData<CardPinState>()

    init {
        cardPinStateObservable.value = CardPinState.ENTER_PIN
    }

    val productCardViewModelDelegate = ProductCardViewDelegate(this)

    fun submit(pin: String) {
        if (pinEntered.length == EngageAppConfig.cardPinLength && pin.isDigitsOnly() && pin == pinEntered) {
            val currentCard = EngageService.getInstance().storageManager.currentCard

            progressOverlayShownObservable.value = true
            compositeDisposable.add(
                    EngageService.getInstance().setCardPinObservable(currentCard, pin.toInt())
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
            cardPinStateObservable.value = CardPinState.MISMATCH_PIN
        }
    }

    fun validatePin(pin: String) {
        pinEntered = pin
        if (pin.isDigitsOnly() && pin.length == EngageAppConfig.cardPinLength) {
            cardPinStateObservable.value = CardPinState.CONFIRM_PIN
        } else {
            // this is redundant for CardPinFragment, technically this should not happen
            cardPinStateObservable.value = CardPinState.INVALID_PIN
        }
    }
}