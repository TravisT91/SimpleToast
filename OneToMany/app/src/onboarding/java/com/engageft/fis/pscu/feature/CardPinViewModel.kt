package com.engageft.fis.pscu.feature

import android.os.Handler
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.isDigitsOnly
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.config.EngageAppConfig
import io.reactivex.disposables.CompositeDisposable


class CardPinViewModel: BaseEngageViewModel() {

    enum class CardPinFlow {
        INVALID_PIN,
        CONFIRM_PIN,
        MISMATCH_PIN,
        SUBMIT_PIN
    }

    val flowObservable = MutableLiveData<CardPinFlow>()

    fun submit(pin: Int, confirmPin: Int) {
        if (pin.toString().length == EngageAppConfig.cardPinLength && pin.toString().isDigitsOnly() && pin == confirmPin) {
            val currentCard = EngageService.getInstance().storageManager.currentCard

            progressOverlayShownObservable.value = true
            compositeDisposable.add(
                    EngageService.getInstance().setCardPinObservable(currentCard, pin)
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
            Handler().post {
                flowObservable.value = CardPinFlow.MISMATCH_PIN
            }
        }
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