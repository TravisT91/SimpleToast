package com.engageft.fis.pscu.feature.secondaryusers

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.engageft.fis.pscu.feature.utils.CardStatusUtils
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.RelatedCardsResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by joeyhutchins on 1/15/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SecondaryUserListViewModel: BaseEngageViewModel() {
                fun setCardTitle(debitCardInfo: DebitCardInfo) {

            }

    val secondaryUserListObservable = MutableLiveData<List<SecondaryUserListItem>>()
    val showSecondarySplashObservable = MutableLiveData<Boolean>()

    fun refreshViews() {
        secondaryUserListObservable.value = ArrayList()
        showSecondarySplashObservable.value = false
        progressOverlayShownObservable.value = true

        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // don't hide progressOverlay just yet
                    if (response is LoginResponse) {
                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        // TODO: Eventually we have to pull all the cards secondary info...
//                        val cards = LoginResponseUtils.getAllCardsSorted(response)
//                        for (card in cards) {
//                            getSecondariesForCard(card)
//                        }
                        getSecondariesForCard(currentCard)
                    } else {
                        progressOverlayShownObservable.value = false
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )
    }

    private fun getSecondariesForCard(debitCardInfo: DebitCardInfo) {
        // TODO(jhutchins): Someday we need to support more than just one of these queries to support multiple card types.
        compositeDisposable.add(EngageService.getInstance().engageApiInterface.postGetSecondaryCards(CardRequest(debitCardInfo.debitCardId).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response.isSuccess && response is RelatedCardsResponse) {
                        if (response.accountList.isNotEmpty()) {
                            val secondaryAccountList = ArrayList<SecondaryUserListItem>()
                            secondaryAccountList.add(SecondaryUserListItem.CardHeaderType(getCardTitleText(debitCardInfo)))

                            for (account in response.accountList) {
                                secondaryAccountList.add(SecondaryUserListItem.ActiveSecondaryUserType("${account.firstName} ${account.lastName}",
                                        CardStatusUtils.productCardModelStatusFromDebitCardInfo(account.debitCardInfo)))
                                account.debitCardInfo.status
                            }
                            if (debitCardInfo.cardPermissionsInfo.isCardSecondaryAddAllowable) {
                                secondaryAccountList.add(SecondaryUserListItem.AddUserType())
                            }
                            secondaryAccountList.add(SecondaryUserListItem.CardFooterType(debitCardInfo.cardPermissionsInfo.cardSecondaryMaxCount))

                            showSecondarySplashObservable.value = false
                            secondaryUserListObservable.value = secondaryAccountList
                        } else {
                            showSecondarySplashObservable.value = true
                            secondaryUserListObservable.value = ArrayList()
                        }
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )
    }

    private fun getCardTitleText(debitCardInfo: DebitCardInfo): String {
        val brandingCard = BrandingInfoRepo.cards?.find { card ->
            card.type == debitCardInfo.cardType
        }
        return brandingCard?.name ?: "unknown card type"
    }
}