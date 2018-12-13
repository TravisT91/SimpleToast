package com.engageft.fis.pscu.feature

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.EngageService
import com.ob.domain.lookup.AchAccountStatus
import com.ob.ws.dom.utility.AchAccountInfo


class AddDeleteAchBankAccountViewModel: BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    enum class AccountStatus {
        VERIFIED,
        UNVERIFIED
    }

    enum class FormState {
        CREATE,
        EDIT
    }

    val bankName: ObservableField<String> = ObservableField("")
    val routingNumber: ObservableField<String> = ObservableField("")
    val accountNumber: ObservableField<String> = ObservableField("")
    val accountType: ObservableField<String> = ObservableField("")
//    val showButton: ObservableField<Boolean> = ObservableField(false)
    val showButton: Boolean = false

    val formStateObservable = MutableLiveData<FormState>()
    val bankAccountStatusObservable = MutableLiveData<AccountStatus>()

    var achAccountInfoId: Long = -1L
    var achAccountInfo: AchAccountInfo? = null

    init {
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
                        if (achAccountInfoId != -1L) {
                            achAccountInfo = LoginResponseUtils.getAchAccountInfoById(response, achAccountInfoId)
                        }

                        achAccountInfo?.let { account ->
                            formStateObservable.value = FormState.EDIT

                            if (account.achAccountStatus == AchAccountStatus.VERIFIED) {
                                bankAccountStatusObservable.value = AccountStatus.VERIFIED
                            } else {
                                bankAccountStatusObservable.value = AccountStatus.UNVERIFIED
                            }
                        } ?: run {
                            // button state
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


}