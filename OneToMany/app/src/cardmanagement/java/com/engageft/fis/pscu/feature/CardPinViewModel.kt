package com.engageft.fis.pscu.feature

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
class CardPinViewModel: BaseEngageViewModel(), CardPinViewModelListener {
    val cardPinViewModelDelegate = CardPinViewModelDelegate(this, this)

    init {
        cardPinViewModelDelegate.productCardViewModelDelegate.updateCardView()
    }

    override fun onPostPin(pinNumber: Int) {
        val currentCard = EngageService.getInstance().storageManager.currentCard
        progressOverlayShownObservable.value = true
        compositeDisposable.add(
                EngageService.getInstance().setCardPinObservable(currentCard, pinNumber)
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
    }
}