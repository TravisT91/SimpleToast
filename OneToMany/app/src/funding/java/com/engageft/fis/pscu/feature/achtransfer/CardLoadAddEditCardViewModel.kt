package com.engageft.fis.pscu.feature.achtransfer

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.apptoolbox.util.isDigitsOnly
import com.engageft.apptoolbox.util.removeWhitespaces
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.FundingAddDebitRequest
import com.engageft.engagekit.rest.request.FundingDeleteDebitRequest
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CARD_NUMBER_FORMAT
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CARD_NUMBER_REQUIRED_LENGTH
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CC_ACCOUNT_ID
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CVV_NUMBER_MAX_LENGTH
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.CVV_NUMBER_MIN_LENGTH
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import java.lang.Exception

class CardLoadAddEditCardViewModel(val ccAccountId: Long): BaseEngageViewModel() {
    enum class EventType {
        ADD, EDIT
    }
    enum class ButtonState {
        SHOW, HIDE
    }
    enum class ValidationType {
        ON_TEXT_CHANGED, ON_FOCUS_LOST,
    }
    enum class Validation {
        EMPTY, VALID, INVALID,
    }

    val eventTypeObservable = MutableLiveData<EventType>()
    val cardNumberValidationObservable = MutableLiveData<Validation>()
    val cvvValidationObservable = MutableLiveData<Validation>()
    val cardExpirationValidationObservable = MutableLiveData<Validation>()
    val buttonStateObservable = MutableLiveData<ButtonState>()

    var eventType = EventType.ADD

    var cardNumber = ObservableField("")
    var cvvNumber = ObservableField("")
    var expirationDate = ObservableField("")
    var showCvvNumber = ObservableField(true)
    var showDeleteLayout = ObservableField(false)

    private val cardNumberPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateCardNumber(ValidationType.ON_TEXT_CHANGED)
            validateButtonState()
        }
    }

    private val cvvNumberPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateCvv(ValidationType.ON_TEXT_CHANGED)
            validateButtonState()
        }
    }

    private val expirationDateNumberPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateExpirationDate(ValidationType.ON_TEXT_CHANGED)
            validateButtonState()
        }
    }

    init {
        cardNumber.addOnPropertyChangedCallback(cardNumberPropertyChangedCallback)
        cvvNumber.addOnPropertyChangedCallback(cvvNumberPropertyChangedCallback)
        expirationDate.addOnPropertyChangedCallback(expirationDateNumberPropertyChangedCallback)

        cardNumberValidationObservable.value = Validation.EMPTY
        cvvValidationObservable.value = Validation.EMPTY
        cardExpirationValidationObservable.value = Validation.EMPTY

        if (ccAccountId != CC_ACCOUNT_ID) {
            eventType = EventType.EDIT
            populateData(ccAccountId)
        }
        eventTypeObservable.value = eventType
    }

    fun hasUnsavedChanges(): Boolean {
        return false
    }

    fun validateCardNumber(validationType: ValidationType) {
        val shouldValidate = cardNumberValidationObservable.value == Validation.INVALID
        if (cardNumber.get()!!.isNotEmpty()) {
            when (validationType) {
                ValidationType.ON_TEXT_CHANGED -> {
                    if (shouldValidate) {
                        validateCardNumber()
                    }
                }
                ValidationType.ON_FOCUS_LOST -> {
                    if (!shouldValidate) {
                        validateCardNumber()
                    }
                }
            }
        } else {
            cardNumberValidationObservable.value = Validation.EMPTY
        }
    }

    private fun validateButtonState() {
        if (isCardNumberValid(cardNumber.get()!!)
                && isCvvValid(cvvNumber.get()!!)
                && isExpirationDateValid(expirationDate.get()!!)) {
            buttonStateObservable.value = ButtonState.SHOW
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
    }
    private fun isCardNumberValid(number: String): Boolean {
        val newValue = number.removeWhitespaces()
        return newValue.isDigitsOnly() && newValue.length == CARD_NUMBER_REQUIRED_LENGTH
    }

    private fun isCvvValid(cvv: String): Boolean {
        return cvv.isDigitsOnly() && (cvv.length == CVV_NUMBER_MIN_LENGTH || cvv.length == CVV_NUMBER_MAX_LENGTH)
    }

    private fun isExpirationDateValid(expirationDate: String): Boolean {
        val formatDate: DateTime? = try {
           DisplayDateTimeUtils.expirationMonthYearFormatter.parseDateTime(expirationDate)
        } catch(e: Exception) {
            null
        }

        formatDate?.let { date ->  return date.isAfter(System.currentTimeMillis()) }
        return false
    }

    private fun validateCardNumber() {
        if (isCardNumberValid(cardNumber.get()!!)) {
            cardNumberValidationObservable.value = Validation.VALID
        } else {
            cardNumberValidationObservable.value = Validation.INVALID
        }
    }

    private fun validateCvv() {
        if (isCvvValid(cvvNumber.get()!!)) {
            cvvValidationObservable.value = Validation.VALID
        } else {
            cvvValidationObservable.value = Validation.INVALID
        }
    }

    fun validateCvv(validationType: ValidationType) {
        val shouldValidate = cvvValidationObservable.value == Validation.INVALID
        if (cvvNumber.get()!!.isNotEmpty()) {
            when (validationType) {
                ValidationType.ON_TEXT_CHANGED -> {
                    if (shouldValidate) {
                        validateCvv()
                    }
                }
                ValidationType.ON_FOCUS_LOST -> {
                    if (!shouldValidate) {
                        validateCvv()
                    }
                }
            }
        } else {
            cvvValidationObservable.value = Validation.EMPTY
        }
    }

    fun validateExpirationDate(validationType: ValidationType) {
        val shouldValidate = cardExpirationValidationObservable.value == Validation.INVALID
        if (expirationDate.get()!!.isNotEmpty()) {
            when (validationType) {
                ValidationType.ON_TEXT_CHANGED -> {
                    if (shouldValidate) {
                        validateExpirationDate()
                    }
                }
                ValidationType.ON_FOCUS_LOST -> {
                    if (!shouldValidate) {
                        validateExpirationDate()
                    }
                }
            }
        } else {
            cardExpirationValidationObservable.value = Validation.EMPTY
        }
    }

    private fun validateExpirationDate() {
        if (isExpirationDateValid(expirationDate.get()!!)) {
            cardExpirationValidationObservable.value = Validation.VALID
        } else {
            cardExpirationValidationObservable.value = Validation.INVALID
        }
    }

    fun addCard() {
        showProgressOverlayDelayed()
        val request = FundingAddDebitRequest("4851687452698865", "1524", "05", "22")
        val observable = engageApi().postFundingAddDebit(request.fieldMap)
        compositeDisposable.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.isSuccess) {

                    } else {
                        handleBackendErrorForForms(response, "TAG: adding a debit/credit card failed")
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private fun populateData(ccAccountId: Long) {
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.isSuccess && response is LoginResponse) {
                        response.ccAccountList.find { ccAccountInfo ->
                            ccAccountInfo.ccAccountId == ccAccountId
                        }?.let { ccAccount ->
                            cardNumber.set(String.format(CARD_NUMBER_FORMAT, ccAccount.lastDigits))
                            expirationDate.set(ccAccount.expiration)
                            showCvvNumber.set(false)
                            showDeleteLayout.set(true)
                        } ?: run {
                            throw IllegalStateException("CcAccountInfo ID not found!")
                        }
                    } else {
                        handleBackendErrorForForms(response, "$TAG: adding a debit/credit card failed")
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    fun deleteACard() {
        val request = FundingDeleteDebitRequest(ccAccountId)
        compositeDisposable.add(engageApi().postFundingDeleteDebit(request.fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.isSuccess) {
//                        response.ccAccountList.forEach { ccAccountInfo ->
//                            ccAccountInfo.ccAccountId = 451
//                        }
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    companion object {
        const val TAG = "CardLoadAddEditCardViewModel"
    }
}

class CardLoadAddEditCardViewModelFactory(private val ccAccountId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CardLoadAddEditCardViewModel(ccAccountId) as T
    }
}