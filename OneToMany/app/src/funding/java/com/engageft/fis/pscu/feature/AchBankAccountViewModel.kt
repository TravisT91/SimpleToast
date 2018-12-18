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

    private companion object {
        const val ACCOUNT_NUMBER_FORMAT = "*******%s"
    }
//    enum class ButtonState {
//        SHOW,
//        HIDE
//    }

    enum class AccountStatus {
        VERIFIED,
        UNVERIFIED
    }

    enum class FormState {
        CREATE,
        EDIT
    }

    enum class NavigationEvent {
        VERIFY_ACCOUNT
    }

    val accountName: ObservableField<String> = ObservableField("")
    val routingNumber: ObservableField<String> = ObservableField("")
    val accountNumber: ObservableField<String> = ObservableField("")
    val accountType: ObservableField<String> = ObservableField("")
    var showButton: ObservableField<Boolean> = ObservableField(false)
    //    var buttonText: ObservableField<String> = ObservableField("")
    //TODO: not sure how I feel about this
    var isEditMode: ObservableField<Boolean> = ObservableField(false)
    var isChecking: Boolean = false

    val navigationEventObservable = MutableLiveData<NavigationEvent>()
    val formStateObservable = MutableLiveData<FormState>()
    //    var formState = FormState.CREATE
    val bankAccountStatusObservable = MutableLiveData<AccountStatus>()

    var achAccountInfoId: Long = 0L
    var achAccountInfo: AchAccountInfo? = null
    var currentAccountInfo: AccountInfo? = null
    val populateAccountTypeObservable = MutableLiveData<Boolean>()
    val checkingAccountTypeObservable = MutableLiveData<String>()

    init {
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
                checkingAccountTypeObservable.value = accountType.get()!!
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
                            formStateObservable.value = FormState.EDIT
                            isEditMode.set(true)
                            // populate data
                            accountName.set(account.bankName)
                            accountNumber.set(String.format(ACCOUNT_NUMBER_FORMAT, account.accountLastDigits))
                            routingNumber.set(account.routeNumber)
                            // TODO(aHashimi): can we get the account types from backend?
                            populateAccountTypeObservable.value = account.isChecking
                        } ?: kotlin.run {
                            formStateObservable.value = FormState.CREATE
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
        //
        showButton.set(if (formStateObservable.value == FormState.CREATE) {
            (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                    && routingNumber.get()!!.isNotEmpty() && accountType.get()!!.isNotEmpty())
        } else {
            !isEditMode.get()!! // show button if account is not verified
        })

        if (formStateObservable.value == FormState.CREATE) {
            if (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                    && routingNumber.get()!!.isNotEmpty() && accountType.get()!!.isNotEmpty()) {
                showButton.set(true)
            }
        } else {
            achAccountInfo?.let {
                if (it.achAccountStatus == AchAccountStatus.UNVERIFIED) {
                    showButton.set(true)
                }
            }
        }
    }

    fun deleteAccount() {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(engageApi().postDeleteAchAccount(AchAccountRequest(
                EngageService.getInstance().authManager.authToken, achAccountInfoId).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response.isSuccess) {
                        // todo must complete
//                                    showSuccessDialog(getString(R.string.BANKACCOUNT_DELETED_ALERT), true)
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
            FormState.EDIT -> {
                if (achAccountInfo?.achAccountStatus == AchAccountStatus.UNVERIFIED) {
                    navigationEventObservable.value = NavigationEvent.VERIFY_ACCOUNT
                }
                // else technically button should not be visible
            }
            FormState.CREATE -> {
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
                            //                                showSuccessDialog(getString(R.string.BANKACCOUNT_CREATED_ALERT), true)
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
        }

    }
}