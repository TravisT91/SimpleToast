package com.engageft.fis.pscu.feature

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.ob.ws.dom.LoginResponse
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

        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                debitCardInfo?.apply {
                                    // Find the BrandingCard that matches the current card type. This could be null
                                    // and null is handled in the view.
                                    cardPinViewModelDelegate.brandingCardObservable.value = BrandingInfoRepo.cards?.find { card ->
                                        card.type == cardType
                                    }
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        })
                        { e ->
                            handleThrowable(e)
                        }
        )
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