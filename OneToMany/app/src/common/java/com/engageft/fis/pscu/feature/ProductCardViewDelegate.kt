package com.engageft.fis.pscu.feature

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.DebitCardInfoUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.R
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.SecureCardInfoResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProductCardViewDelegate(private val parentViewModel: BaseEngageViewModel) {

    var expirationDateFormatString = "%1\$d/%2\$d" // provide a sensible default, and allow to be overridden

    var cardInfoModelObservable: MutableLiveData<ProductCardModel> = MutableLiveData()
    var cardStateObservable: MutableLiveData<ProductCardViewCardState> = MutableLiveData()

    fun updateCardView(secureCardInfoResponse: SecureCardInfoResponse? = null) {
        cardStateObservable.postValue(ProductCardViewCardState.LOADING)
        parentViewModel.compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val productCardModel = productCardModelFromLoginResponse(response)
                                secureCardInfoResponse?.apply {
                                    productCardModel.cardCvv = cvv
                                    BackendDateTimeUtils.parseDateTimeFromIso8601String(expiration)?.let { expirationDate ->
                                        productCardModel.cardExpirationMonthYear = String.format(expirationDateFormatString, expirationDate.monthOfYear, expirationDate.year)
                                    }
                                    productCardModel.cardNumberFull = pan
                                    cardStateObservable.postValue(ProductCardViewCardState.DETAILS_SHOWN)
                                } ?: run {
                                    cardStateObservable.postValue(ProductCardViewCardState.DETAILS_HIDDEN)
                                }
                                cardInfoModelObservable.postValue(productCardModel)
                            } else {
                                cardStateObservable.postValue(ProductCardViewCardState.ERROR)
                                parentViewModel.handleUnexpectedErrorResponse(response)
                            }
                        }) { e ->
                            cardStateObservable.postValue(ProductCardViewCardState.ERROR)
                            parentViewModel.handleThrowable(e)
                        }
        )
    }

    fun showCardDetails() {
        cardStateObservable.value = ProductCardViewCardState.LOADING
        parentViewModel.compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                LoginResponseUtils.getCurrentCard(response)?.let { debitCardInfo ->

                                    val observable = if (DebitCardInfoUtils.hasVirtualCard(debitCardInfo))
                                        EngageService.getInstance().debitVirtualCardInfoObservable(debitCardInfo)
                                    else
                                        EngageService.getInstance().debitSecureCardInfoObservable(debitCardInfo)

                                    parentViewModel.compositeDisposable.add(observable
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ response1 ->
                                                if (response1.isSuccess && response1 is SecureCardInfoResponse) {
                                                    updateCardView(response1)
                                                } else {
                                                    cardStateObservable.postValue(ProductCardViewCardState.DETAILS_HIDDEN)
                                                    parentViewModel.handleUnexpectedErrorResponse(response1)
                                                }
                                            }) { e ->
                                                cardStateObservable.postValue(ProductCardViewCardState.DETAILS_HIDDEN)
                                                parentViewModel.handleThrowable(e)
                                            }
                                    )
                                } ?: kotlin.run {
                                    // No DebitCardInfo in LoginResponse
                                    cardStateObservable.postValue(ProductCardViewCardState.ERROR)
                                    parentViewModel.dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_ERROR))
                                }
                            } else {
                                cardStateObservable.postValue(ProductCardViewCardState.ERROR)
                                parentViewModel.handleUnexpectedErrorResponse(response)
                            }
                        }) { e ->
                            cardStateObservable.postValue(ProductCardViewCardState.ERROR)
                            parentViewModel.handleThrowable(e)
                        }
        )
    }

    fun hideCardDetails() {
        updateCardView(null)
    }

    fun isShowingCardDetails(): Boolean {
        return cardStateObservable.value == ProductCardViewCardState.DETAILS_SHOWN
    }

    fun isLocked(): Boolean {
        return cardInfoModelObservable.value?.cardLocked ?: false
    }
}

enum class ProductCardViewCardState {
    LOADING,
    DETAILS_HIDDEN,
    DETAILS_SHOWN,
    ERROR
}

@StringRes
fun ProductCardModelCardStatus.cardStatusStringRes(): Int {
    return when (this) {
        ProductCardModelCardStatus.CARD_STATUS_ACTIVE -> R.string.CARD_STATUS_DISPLAY_ACTIVE
        ProductCardModelCardStatus.CARD_STATUS_VIRTUAL -> R.string.CARD_STATUS_DISPLAY_VIRTUAL
        ProductCardModelCardStatus.CARD_STATUS_PENDING -> R.string.CARD_STATUS_DISPLAY_PENDING
        ProductCardModelCardStatus.CARD_STATUS_LOCKED -> R.string.CARD_STATUS_DISPLAY_LOCKED
        ProductCardModelCardStatus.CARD_STATUS_REPLACED -> R.string.CARD_STATUS_DISPLAY_REPLACED
        ProductCardModelCardStatus.CARD_STATUS_CANCELED -> R.string.CARD_STATUS_DISPLAY_CANCELLED
        ProductCardModelCardStatus.CARD_STATUS_SUSPENDED -> R.string.CARD_STATUS_DISPLAY_SUSPENDED
        ProductCardModelCardStatus.CARD_STATUS_CLOSED -> R.string.CARD_STATUS_DISPLAY_CLOSED
        ProductCardModelCardStatus.CARD_STATUS_ORDERED -> R.string.CARD_STATUS_DISPLAY_ORDERED
    }
}

private fun productCardModelFromLoginResponse(loginResponse: LoginResponse): ProductCardModel {
    val productCardModel = ProductCardModel()
    productCardModel.cardholderName = LoginResponseUtils.getUserFullname(loginResponse)
    LoginResponseUtils.getCurrentCard(loginResponse)?.let { cardInfo ->
        productCardModel.cardStatus = productCardModelStatusFromDebitCardInfo(cardInfo)
        productCardModel.cardStatusOkay = DebitCardInfoUtils.displayCardStatusAsOkay(cardInfo)
        productCardModel.cardLocked = DebitCardInfoUtils.isLocked(cardInfo)
        productCardModel.cardPendingActivation = DebitCardInfoUtils.isPendingActivation(cardInfo)
        productCardModel.cardNumberPartial = cardInfo.lastFour
    }

    return productCardModel
}

private fun productCardModelStatusFromDebitCardInfo(debitCardInfo: DebitCardInfo): ProductCardModelCardStatus {
    return if (DebitCardInfoUtils.hasVirtualCard(debitCardInfo) && EngageService.getInstance().engageConfig.virtualCardEnabled)
        ProductCardModelCardStatus.CARD_STATUS_VIRTUAL
    else if (DebitCardInfoUtils.isLocked(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_LOCKED
    else if (DebitCardInfoUtils.isPendingActivation(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_PENDING
    else if (DebitCardInfoUtils.isLostStolen(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_REPLACED
    else if (DebitCardInfoUtils.isCancelled(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_CANCELED
    else if (DebitCardInfoUtils.isSuspended(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_SUSPENDED
    else if (DebitCardInfoUtils.isFraudStatus(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_CLOSED
    else if (DebitCardInfoUtils.isOrdered(debitCardInfo))
        ProductCardModelCardStatus.CARD_STATUS_ORDERED
    else
        ProductCardModelCardStatus.CARD_STATUS_ACTIVE
}