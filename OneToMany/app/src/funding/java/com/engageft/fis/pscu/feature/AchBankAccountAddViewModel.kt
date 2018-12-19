package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.EngageService
import com.ob.ws.dom.utility.AchAccountInfo
import com.engageft.engagekit.rest.request.AchAccountCreateRequest
import com.engageft.engagekit.utils.engageApi
import com.ob.domain.lookup.AchAccountStatus
import com.engageft.engagekit.rest.request.AchAccountRequest
import com.ob.ws.dom.utility.AccountInfo

class AchBankAccountViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }
    private companion object {
        const val ACCOUNT_NUMBER_FORMAT = "*******%s"
    }

//    enum class AccountStatus {
//        VERIFIED,
//        UNVERIFIED
//    }

    val accountName: ObservableField<String> = ObservableField("")
    val routingNumber: ObservableField<String> = ObservableField("")
    val accountNumber: ObservableField<String> = ObservableField("")
    val accountType: ObservableField<String> = ObservableField("")
//    var showButton: ObservableField<Boolean> = ObservableField(false)
    var showDeleteButton: ObservableField<Boolean> = ObservableField(false)
    //    var buttonText: ObservableField<String> = ObservableField("")
    //TODO: not sure how I feel about this
//    var isEditMode: ObservableField<Boolean> = ObservableField(false)
    var isChecking: Boolean = false

    val navigationEventObservable = MutableLiveData<AchBankAccountNavigationEvent>()
    val buttonStateObservable = MutableLiveData<ButtonState>()
//    val formStateObservable = MutableLiveData<FormState>()
    //    var formState = FormState.CREATE
//    val bankAccountStatusObservable = MutableLiveData<AccountStatus>()
    val routingNumberShowErrorObservable = MutableLiveData<Boolean>()

    var currentAccountInfo: AccountInfo? = null

    init {
        buttonStateObservable.value = ButtonState.HIDE
        routingNumberShowErrorObservable.value = false

        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response.isSuccess && response is LoginResponse) {
                        currentAccountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
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

    fun isRoutingNumberValid(): Boolean {
        if (routingNumber.get()!!.isNotEmpty() && routingNumber.get()!!.length == 9) {
            return true
        }
        return false
    }

    fun validRoutingNumber() {
        routingNumberShowErrorObservable.value = !(routingNumber.get()!!.isEmpty() || isRoutingNumberValid())
    }

    fun onAddAccount() {
        if (areAllFieldsValid()) {
            currentAccountInfo?.let { accountInfo ->

                progressOverlayShownObservable.value = true

                val request = AchAccountCreateRequest(
                        EngageService.getInstance().authManager.authToken,
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
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess) {
                                EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
                                navigationEventObservable.value = AchBankAccountNavigationEvent.BANK_ADDED_SUCCESS
                                navigationEventObservable.postValue(AchBankAccountNavigationEvent.NONE)
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
                )
            } ?: kotlin.run {
                //todo show dialog
                dialogInfoObservable.value = DialogInfo()
            }
        } else {
            // todo show dialog
        }
    }

    private fun areAllFieldsValid(): Boolean {
        if (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                && isRoutingNumberValid() && accountType.get()!!.isNotEmpty()) {
            return true
        }
        return false
    }
}

class AchBankAccountDialogInfo(title: String? = null,
                           message: String? = null,
                           tag: String? = null,
                           dialogType: DialogType = DialogType.GENERIC_ERROR,
                           var achBankAccountDialogType: AchBankAccountType) : DialogInfo(title, message, tag, dialogType) {
    enum class AchBankAccountType {
        DEPOSIT_AMOUNT_MISMATCH,
//        DEPOSIT_AMOUNT_INVALID,
        DELETE_ACCOUNT_CONFIRMATION,
    }
}

enum class AchBankAccountNavigationEvent {
    BANK_ADDED_SUCCESS,
    BANK_VERIFIED_SUCCESS,
    VERIFY_ACCOUNT,
    DELETED_BANK_SUCCESS,
    NONE
}