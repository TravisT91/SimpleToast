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


class AchBankAccountViewModel: BaseEngageViewModel() {
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
    var formState = FormState.CREATE
    val bankAccountStatusObservable = MutableLiveData<AccountStatus>()

    var achAccountInfoId: Long = 0L
    var achAccountInfo: AchAccountInfo? = null
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

                        achAccountInfo?.let { account ->
                            formStateObservable.value = FormState.EDIT
                            isEditMode.set(true)
                            // populate data
                            accountName.set(account.bankName)
                            accountNumber.set(account.accountLastDigits)
                            routingNumber.set(account.routeNumber)
                            // TODO(aHashimi): can we get the account types from backend?
                            populateAccountTypeObservable.value = account.isChecking
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
        showButton.set(if (formState == FormState.CREATE) {
            (accountName.get()!!.isNotEmpty() && accountNumber.get()!!.isNotEmpty()
                    && routingNumber.get()!!.isNotEmpty() && accountType.get()!!.isNotEmpty())
        } else {
            !isEditMode.get()!! // show button if account is not verified
        })
    }

    fun deleteAccount() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        progressOverlayShownObservable.value = true
        val request = AchAccountCreateRequest(
                EngageService.getInstance().authManager.authToken,
                isChecking,
                accountName.get()!!,
                accountNumber.get()!!,
                routingNumber.get()!!,
                String.format("%d", achAccountInfoId),
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
    }
}