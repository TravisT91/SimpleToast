package com.engageft.fis.pscu.feature.achtransfer

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.AchAccountCreateRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AccountInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * AchBankAccountAddViewModel
 * </p>
 * ViewModel that manages adding of an ACH bank account.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountAddViewModel: BaseCardLoadViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    val accountName: ObservableField<String> = ObservableField("")
    val routingNumber: ObservableField<String> = ObservableField("")
    val accountNumber: ObservableField<String> = ObservableField("")
    val accountType: ObservableField<String> = ObservableField("")

    val bankAddSuccessObservable = SingleLiveEvent<Unit>()
    val buttonStateObservable = MutableLiveData<ButtonState>()
    val routingNumberShowErrorObservable = MutableLiveData<Boolean>()

    var currentAccountInfo: AccountInfo? = null

    init {
        buttonStateObservable.value = ButtonState.HIDE
        routingNumberShowErrorObservable.value = false

        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response.isSuccess && response is LoginResponse) {
                        currentAccountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )

        accountName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        routingNumber.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (routingNumberShowErrorObservable.value!!) {
                    validRoutingNumber()
                }
                updateButtonState()
            }
        })

        accountNumber.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        accountType.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

    }

    fun updateButtonState() {
        if (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                && routingNumber.get()!!.isNotEmpty() && accountType.get()!!.isNotEmpty()) {
            buttonStateObservable.value = ButtonState.SHOW
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
    }

    fun validRoutingNumber() {
        routingNumberShowErrorObservable.value = !(routingNumber.get()!!.isEmpty() || isRoutingNumberValid())
    }

    fun hasUnsavedChanges(): Boolean {
        if (accountName.get()!!.isNotEmpty() || routingNumber.get()!!.isNotEmpty()
                || accountNumber.get()!!.isNotEmpty() || accountType.get()!!.isNotEmpty()) {
            return true
        }
        return false
    }

    fun onAddAccount() {
        if (areAllFieldsValid()) {
            currentAccountInfo?.let { accountInfo ->
                val isChecking = accountType.get() == ACCOUNT_TYPE_CHECKING

                showProgressOverlayDelayed()

                val request = AchAccountCreateRequest(
                        isChecking,
                        accountName.get()!!,
                        accountNumber.get()!!,
                        routingNumber.get()!!,
                        String.format("%d", accountInfo.accountId),
                        false)
                compositeDisposable.add(engageApi().postAddAchAccount(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess) {
                                EngageService.getInstance().storageManager.removeLoginResponse()
                                refreshLoginResponse(bankAddSuccessObservable)
                            } else {
                                handleBackendErrorForForms(response, "$TAG: failed to add an ACH account")
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
                )
            } ?: kotlin.run {
                dialogInfoObservable.value = DialogInfo()
            }
        }
    }

    private fun isRoutingNumberValid(): Boolean {
        if (routingNumber.get()!!.isNotEmpty() && routingNumber.get()!!.length == 9) {
            return true
        }
        return false
    }

    private fun areAllFieldsValid(): Boolean {
        if (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                && isRoutingNumberValid() && accountType.get()!!.isNotEmpty()) {
            return true
        }
        return false
    }

    private companion object {
        const val ACCOUNT_TYPE_CHECKING = "Checking"
        const val TAG = "AchBankAccountAddViewModel"
    }
}