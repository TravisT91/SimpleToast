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

    enum class AccountStatus {
        VERIFIED,
        UNVERIFIED
    }

//    enum class FormState {
//        CREATE,
//        EDIT
//    }

    enum class NavigationEvent {
        VERIFY_ACCOUNT
    }

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
    val formStateObservable = MutableLiveData<FormState>()
    //    var formState = FormState.CREATE
    val bankAccountStatusObservable = MutableLiveData<AccountStatus>()

    var achAccountInfoId: Long = 0L
    var achAccountInfo: AchAccountInfo? = null
    var currentAccountInfo: AccountInfo? = null
//    val populateAccountTypeObservable = MutableLiveData<Boolean>()
//    val checkingAccountTypeObservable = MutableLiveData<String>()

    init {
        buttonStateObservable.value = ButtonState.HIDE

        accountName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        routingNumber.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
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
//                checkingAccountTypeObservable.value = accountType.get()!!
            }
        })

        initData()
    }

    private fun initData() {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response.isSuccess && response is LoginResponse) {
                        if (achAccountInfoId != 0L) {
                            achAccountInfo = LoginResponseUtils.getAchAccountInfoById(response, achAccountInfoId)
                        }
                        currentAccountInfo = LoginResponseUtils.getCurrentAccountInfo(response)

                        achAccountInfo?.let { account ->
                            val editState = FormState.EditState()
//                            formStateObservable.value = FormState.EditState
//                            isEditMode.set(true)
                            // populate data
                            accountName.set(account.bankName)
                            accountNumber.set(String.format(ACCOUNT_NUMBER_FORMAT, account.accountLastDigits))
                            routingNumber.set(account.routeNumber)
                            editState.isChecking = account.isChecking
                            editState.isAccountVerified = account.achAccountStatus == AchAccountStatus.VERIFIED
                            formStateObservable.value = editState

                            showDeleteButton.set(true)
                        } ?: kotlin.run {
                            formStateObservable.value = FormState.CreateState()
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

    fun updateButtonState() {
        if (formStateObservable.value is FormState.CreateState) {
            if (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                    && routingNumber.get()!!.isNotEmpty() && accountType.get()!!.isNotEmpty()) {
//                buttonStateObservable.set(true)
                buttonStateObservable.value = ButtonState.SHOW
            }
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
//        else {
//            achAccountInfo?.let {
//                if (it.achAccountStatus == AchAccountStatus.UNVERIFIED) {
//                    showButton.set(true)
//                }
//            }
//        }
    }

    fun onDeleteAccount() {

        progressOverlayShownObservable.value = true
        compositeDisposable.add(engageApi().postDeleteAchAccount(AchAccountRequest(
                EngageService.getInstance().authManager.authToken, achAccountInfoId).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response.isSuccess) {
                        val loginResponse = EngageService.getInstance().storageManager.loginResponse
                        if (loginResponse != null) {
                            EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
                            val currentCard = LoginResponseUtils.getCurrentCard(loginResponse)
                            if (currentCard != null) {
                                EngageService.getInstance().storageManager.clearScheduledLoadsResponse(currentCard)
                            }
                        }
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

    fun onAddOrVerifyAccount() {
        when (formStateObservable.value) {
            is FormState.EditState -> {
                if (achAccountInfo?.achAccountStatus == AchAccountStatus.UNVERIFIED) {
                    navigationEventObservable.value = AchBankAccountNavigationEvent.VERIFY_ACCOUNT
                }
                // else technically button should not be visible
            }
            is FormState.CreateState -> {
                addBankAccount()
            }
        }
    }

    private fun addBankAccount() {
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

    }
}

sealed class FormState {
    class CreateState: FormState()
    class EditState(var isChecking: Boolean = false, var isAccountVerified: Boolean = false): FormState()
}

class AchBankAccountDialogInfo(title: String? = null,
                           message: String? = null,
                           tag: String? = null,
                           dialogType: DialogType = DialogType.GENERIC_ERROR,
                           var achBankAccountDialogType: AchBankAccountType) : DialogInfo(title, message, tag, dialogType) {
    enum class AchBankAccountType {
        DEPOSIT_AMOUNT_MISMATCH,
        DEPOSIT_AMOUNT_INVALID,
        DELETE_ACCOUNT_CONFIRMATION,
    }
}

enum class AchBankAccountNavigationEvent {
    BANK_ADDED_SUCCESS,
    BANK_VERIFIED_SUCCESS,
    VERIFY_ACCOUNT

}