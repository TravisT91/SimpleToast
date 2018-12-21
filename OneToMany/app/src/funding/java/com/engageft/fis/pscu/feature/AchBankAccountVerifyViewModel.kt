package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AchAccountValidateRequest
import com.engageft.fis.pscu.config.EngageAppConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AchBankAccountVerifyViewModel: BaseEngageViewModel() {

    enum class ButtonState {
        SHOW,
        HIDE
    }

    private val minAmount = 0.01
    private val maxAmount = 0.99
    var amount1: ObservableField<String> = ObservableField("")
    var amount2: ObservableField<String> = ObservableField("")
    var achAccountInfoId: Long = 0L

    val navigationEventObservable = MutableLiveData<AchBankAccountNavigationEvent>()
    val buttonStateObservable = MutableLiveData<ButtonState>()
    val amount1ShowErrorObservable = MutableLiveData<Boolean>()
    val amount2ShowErrorObservable = MutableLiveData<Boolean>()

    init {
        buttonStateObservable.value = ButtonState.HIDE
        amount1ShowErrorObservable.value = false
        amount2ShowErrorObservable.value = false

        amount1.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (amount1ShowErrorObservable.value!!) {
                    validateNonEmptyAmount1AndShowError()
                }
                updateButtonState()
            }
        })

        amount2.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (amount2ShowErrorObservable.value!!) {
                    validateNonEmptyAmount2AndShowError()
                }
                updateButtonState()
            }
        })
    }

    fun onVerifyAccount() {
        if (getValidationStatusAndShowErrors()) {
            progressOverlayShownObservable.value = true
            //TODO(aHashimi): When ThreatMatrix is setUp pass sessionID
            val request = AchAccountValidateRequest(
                    EngageService.getInstance().authManager.authToken,
                    achAccountInfoId,
                    amount1.get()!!,
                    amount2.get()!!, "")

            compositeDisposable.add(
                    EngageService.getInstance().engageApiInterface.postValidateAchAccount(request.fieldMap)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                progressOverlayShownObservable.value = false
                                if (response.isSuccess) {
                                    EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
                                    navigationEventObservable.value = AchBankAccountNavigationEvent.BANK_VERIFIED_SUCCESS
                                    navigationEventObservable.value = AchBankAccountNavigationEvent.NONE
                                } else {
                                    // show backend error message
                                    if (response.message.isNotEmpty()) {
                                        dialogInfoObservable.value = DialogInfo(
                                                message = response.message,
                                                dialogType = DialogInfo.DialogType.SERVER_ERROR)
                                    } else {
                                        handleUnexpectedErrorResponse(response)
                                    }
                                }
                            }, { e ->
                                progressOverlayShownObservable.value = false
                                handleThrowable(e)
                            })
            )
        }
    }

    fun validateNonEmptyAmount1AndShowError() {
        // don't show errors if field is left empty
        amount1ShowErrorObservable.value = !(amount1.get()!!.isEmpty() || isAmountValid(amount1.get()!!))
    }

    fun validateNonEmptyAmount2AndShowError() {
        // don't show errors if field is left empty
        amount2ShowErrorObservable.value = !(amount2.get()!!.isEmpty() || isAmountValid(amount2.get()!!))
    }

    private fun isAmountValid(amount: String): Boolean {
        val extractedAmount = CurrencyUtils.getNonFormattedDecimalAmountString(
                currencyCode = EngageAppConfig.currencyCode,
                stringWithCurrencySymbol = amount)
        if (extractedAmount.isNotEmpty()) {
            return extractedAmount.toDouble() in minAmount..maxAmount
        }

        return false
    }

    private fun getValidationStatusAndShowErrors(): Boolean {
        if (isAmountValid(amount1.get()!!) && isAmountValid(amount2.get()!!)) {
            return true
        } else {
            if (!isAmountValid(amount1.get()!!)) {
                amount1ShowErrorObservable.value = true
            }
            if (!isAmountValid(amount2.get()!!)) {
                amount2ShowErrorObservable.value = true
            }
        }
        return false
    }

    private fun updateButtonState() {
        if (amount1.get()!!.isNotEmpty() && amount2.get()!!.isNotEmpty()) {
            buttonStateObservable.value = ButtonState.SHOW
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
    }
}