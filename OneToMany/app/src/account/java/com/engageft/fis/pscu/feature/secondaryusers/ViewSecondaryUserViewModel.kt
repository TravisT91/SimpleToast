package com.engageft.fis.pscu.feature.secondaryusers

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.CardRequest
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.utils.CardStatusUtils
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.RelatedCardsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by joeyhutchins on 2/26/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class ViewSecondaryUserViewModel : BaseEngageViewModel() {
    companion object {
        const val INVALID_ID = -1L
    }
    val titleObservable = MutableLiveData<String>()
    val firstName : ObservableField<String> = ObservableField("")
    val lastName : ObservableField<String> = ObservableField("")
    val phoneNumber : ObservableField<String> = ObservableField("")
    val dob : ObservableField<String> = ObservableField("")
    val cardStatusObservable = MutableLiveData<ProductCardModelCardStatus>()

    var debitCardId = INVALID_ID
    var userId = INVALID_ID

    init {
        titleObservable.value = ""
    }

    fun fetchUser() {
        // At this point, the params need to be set by the fragment, or we fail.
        if (debitCardId == INVALID_ID || userId == INVALID_ID) {
            throw IllegalStateException("debit card id or user id invalid.")
        }

        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().engageApiInterface.postGetSecondaryCards(CardRequest(debitCardId).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response.isSuccess && response is RelatedCardsResponse) {
                        if (response.accountList.isNotEmpty()) {
                            val user = response.accountList.find {
                                it.accountId == userId
                            }

                            user?.let {
                                val first = it.firstName
                                val last = it.lastName

                                titleObservable.value = "$first $last"
                                firstName.set(first)
                                lastName.set(last)
                                phoneNumber.set(it.phone)
                                dob.set(it.birthDate)
                                cardStatusObservable.value = CardStatusUtils.productCardModelStatusFromDebitCardInfo(it.debitCardInfo)
                            } ?: kotlin.run {
                                handleUnexpectedErrorResponse(BasicResponse(false, "Failed to find user for user details!"))
                            }
                        } else {
                            handleUnexpectedErrorResponse(BasicResponse(false, "viewing secondary user for card $debitCardId with empty list!"))
                        }
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }
}