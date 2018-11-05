package com.engageft.feature


import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.DebitCardInfoUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.SecureCardInfoResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import utilGen1.StringUtils

/**
 *  CardViewViewModel
 *  </p>
 *  ViewModel to support CardView widget for displaying credit/debit card info.
 *  </p>
 *  Created by Kurt Mueller on 5/1/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardViewViewModel : BaseViewModel() {

    var cardInfoModelObservable: MutableLiveData<ProductCardModel> = MutableLiveData()
    var cardStateObservable: MutableLiveData<CardState> = MutableLiveData()

    var expirationDateFormatString = "%1\$d/%2\$d" // provide a sensible default, and allow to be overridden

    private val compositeDisposable = CompositeDisposable()

    val dialogInfoObservable: MutableLiveData<DashboardDialogInfo> = MutableLiveData()
    val navigationObservable = MutableLiveData<OverviewNavigationEvent>()

    fun isShowingCardDetails(): Boolean {
        return cardStateObservable.value == CardState.DETAILS_SHOWN
    }

    fun isLocked(): Boolean {
        return cardInfoModelObservable.value?.cardLocked ?: false
    }

    fun initCardView() {
        updateCardView()
    }

    private fun updateCardView(secureCardInfoResponse: SecureCardInfoResponse? = null) {
        cardStateObservable.postValue(CardState.LOADING)
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                var productCardModel = ProductCardModel()
                                productCardModel.cardholderName = LoginResponseUtils.getUserFullname(response)
                                debitCardInfo?.let { cardInfo ->
                                    /**
                                     * StringUtils accesses app context to get the app-specific status String. In pure MVVM this may be
                                     * frowned upon, but the view that this populates (ProductCardView) is in apptoolbox so doesn't have
                                     * access to the app's string resources. So, view model must set the actual string, not
                                     * pass an enum to the view that it can use to set the string.
                                     */
                                    productCardModel.cardStatusText = StringUtils.getDebitCardInfoFriendlyStatus(cardInfo)
                                    productCardModel.cardStatusOkay = DebitCardInfoUtils.displayCardStatusAsOkay(cardInfo)
                                    productCardModel.cardLocked = DebitCardInfoUtils.isLocked(cardInfo)
                                    productCardModel.cardPendingActivation = DebitCardInfoUtils.isPendingActivation(cardInfo)
                                    secureCardInfoResponse?.let { response ->
                                        productCardModel.cardCvv = response.cvv
                                        BackendDateTimeUtils.parseDateTimeFromIso8601String(response.expiration)?.let { expirationDate ->
                                            productCardModel.cardExpirationMonthYear = String.format(expirationDateFormatString, expirationDate.monthOfYear, expirationDate.year)
                                        }
                                        productCardModel.cardNumberFull = response.pan
                                        cardStateObservable.postValue(CardState.DETAILS_SHOWN)
                                    } ?: run {
                                        productCardModel.cardNumberPartial = cardInfo.lastFour
                                        cardStateObservable.postValue(CardState.DETAILS_HIDDEN)
                                    }
                                    cardInfoModelObservable.postValue(productCardModel)
                                }
                            } else {
                                cardStateObservable.postValue(CardState.ERROR)
                            }
                        }) {_ ->
                            cardStateObservable.postValue(CardState.ERROR)
                        }

        )
    }

    fun showCardDetails() {
        val debitCardInfo = LoginResponseUtils.getCurrentCard(EngageService.getInstance().storageManager.loginResponse)
        if (debitCardInfo != null) {
            cardStateObservable.value = CardState.LOADING
            val observable = if (DebitCardInfoUtils.hasVirtualCard(debitCardInfo))
                EngageService.getInstance().debitVirtualCardInfoObservable(debitCardInfo)
            else
                EngageService.getInstance().debitSecureCardInfoObservable(debitCardInfo)

            compositeDisposable.add(observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        if (response.isSuccess && response is SecureCardInfoResponse) {
                            updateCardView(response)
                        } else {
                            cardStateObservable.value = CardState.DETAILS_HIDDEN

                            // TODO(jhutchins): Proper error handling.
                            dialogInfoObservable.value = DashboardDialogInfo()
                        }
                    }) { e ->
                        cardStateObservable.value = CardState.DETAILS_HIDDEN

                        // TODO(jhutchins): Proper error handling.
                        dialogInfoObservable.value = DashboardDialogInfo()
                    }
            )
        }
    }

    fun hideCardDetails() {
        updateCardView(null)
    }

    enum class CardState {
        LOADING,
        DETAILS_HIDDEN,
        DETAILS_SHOWN,
        ERROR
    }
}