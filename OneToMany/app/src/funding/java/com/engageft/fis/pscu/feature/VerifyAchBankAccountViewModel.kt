package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.rest.request.AchAccountValidateRequest
import com.engageft.fis.pscu.feature.login.LoginDialogInfo
import com.engageft.engagekit.tools.Profiler
import com.engageft.fis.pscu.config.BaseAppConfig


class VerifyAchBankAccountViewModel: BaseEngageViewModel() {

    val minAmount = 0.01
    val maxAmount = 0.99
    val TAG = "VerifyAchBankAccountVM"
    var showButton: ObservableField<Boolean> = ObservableField(false)
    var amount1: ObservableField<String> = ObservableField("")
    var amount2: ObservableField<String> = ObservableField("")
    var achAccountInfoId: Long = 0L

    val amount1ShowErrorObservable = MutableLiveData<Boolean>()
    val amount2ShowErrorObservable = MutableLiveData<Boolean>()

    init {
        amount1ShowErrorObservable.value = false
        amount2ShowErrorObservable.value = false
        //TODO: verify account by account id passed. if not found, showDialog & popoff fragment
        amount1.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (amount1ShowErrorObservable.value!!) {
                    validateAmount1()
                }
                updateButtonState()
            }
        })

        amount2.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (amount2ShowErrorObservable.value!!) {
                    validateAmount2()
                }
                updateButtonState()
            }
        })
    }

    fun onVerifyAccount() {
        if (isAmountValid(amount1.get()!!) && isAmountValid(amount2.get()!!)) {
            progressOverlayShownObservable.value = true
            //TODO(aHashimi): should send sessionID like gen1?
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
                                } else {
//                                    handleUnexpectedErrorResponse(response)
                                    dialogInfoObservable.value = AchBankAccountDialogInfo(
                                            dialogType = DialogInfo.DialogType.OTHER,
                                            achBankAccountDialogType = AchBankAccountDialogInfo.AchBankAccountType.DEPOSIT_AMOUNT_MISMATCH)
                                }
                            }, { e ->
                                progressOverlayShownObservable.value = false
                                handleThrowable(e)
                            })
            )
        } else {
            dialogInfoObservable.value = AchBankAccountDialogInfo(
                    dialogType = DialogInfo.DialogType.OTHER,
                    achBankAccountDialogType = AchBankAccountDialogInfo.AchBankAccountType.DEPOSIT_AMOUNT_INVALID)
        }
    }

    fun validateAmount1() {
        amount1ShowErrorObservable.value = !(amount1.get()!!.isEmpty() || isAmountValid(amount1.get()!!))
    }

    fun validateAmount2() {
        amount2ShowErrorObservable.value = !(amount2.get()!!.isEmpty() || isAmountValid(amount2.get()!!))
    }

    private fun isAmountValid(amount: String): Boolean {
        //todo remove hard-coded currency code
        val extractedAmount = CurrencyUtils.getNonFormattedDecimalAmountString(
                currencyCode = "USD",
                stringWithCurrencySymbol = amount)
        if (extractedAmount.isNotEmpty()) {
            return extractedAmount.toDouble() in minAmount..maxAmount
        }

        return false
    }

    private fun updateButtonState() {
        if (amount1.get()!!.isNotEmpty() && amount2.get()!!.isNotEmpty()) {
            showButton.set(true)
        } else {
            showButton.set(false)
        }
    }
}