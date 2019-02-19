package com.engageft.fis.pscu.feature.achtransfer

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AchAccountValidateRequest
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
/**
 * AchBankAccountVerifyViewModel
 * </p>
 * ViewModel that handles verifying an ACH bank account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountVerifyViewModel: BaseEngageViewModel() {

    enum class ButtonState {
        SHOW,
        HIDE
    }

    private val minAmount = 0.01
    private val maxAmount = 0.99
    val amount1: ObservableField<String> = ObservableField("")
    val amount2: ObservableField<String> = ObservableField("")
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
            showProgressOverlayDelayed()
            //TODO(aHashimi): When ThreatMatrix is setUp pass sessionID
            val request = AchAccountValidateRequest(
                    achAccountInfoId,
                    amount1.get()!!,
                    amount2.get()!!, "")

            compositeDisposable.add(
                    EngageService.getInstance().engageApiInterface.postValidateAchAccount(request.fieldMap)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                dismissProgressOverlay()
                                if (response.isSuccess) {
                                    EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
                                    navigationEventObservable.value = AchBankAccountNavigationEvent.BANK_VERIFIED_SUCCESS
                                    navigationEventObservable.value = AchBankAccountNavigationEvent.NONE
                                } else {
                                    handleBackendErrorForForms(response, "$TAG: failed to verify ACH account")
                                }
                            }, { e ->
                                dismissProgressOverlay()
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

    private companion object {
        const val TAG = "AchBankAccountVerifyViewModel"
    }
}