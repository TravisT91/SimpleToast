package com.engageft.fis.pscu.feature

import com.engageft.engagekit.tools.MixpanelEvent
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import android.text.TextUtils
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.utils.*
import io.reactivex.schedulers.Schedulers
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AccountInfo


class AccountNotificationsViewModel: BaseEngageViewModel() {

    val TAG = "AccountNotificationsVM"
    enum class SaveButtonState {
        HIDE,
        SHOW
    }

    private var accountInfo: AccountInfo? = null
//    var phoneNumber: String = "8589254498"
    var phoneNumber: ObservableField<String> = ObservableField("")

    var pushObservable = MutableLiveData<Boolean>()
    var smsObservable = MutableLiveData<Boolean>()
    var emailObservable = MutableLiveData<Boolean>()

    var saveButtonStateObservable = MutableLiveData<SaveButtonState>()

    init {
        saveButtonStateObservable.value = SaveButtonState.HIDE
        pushObservable.value = false
        smsObservable.value = false
        emailObservable.value = false

        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response is LoginResponse) {
                        setUpData(response)
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )

        phoneNumber.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            }
        })
    }

    fun onPushCheckChanged(isChecked: Boolean) {
        pushObservable.value = isChecked

        if (isChecked) smsObservable.value = false

        updateSaveButtonState()
    }

    fun onSmsCheckChanged(isChecked: Boolean) {
        smsObservable.value = isChecked

        if (isChecked) pushObservable.value = false

        updateSaveButtonState()
    }

    fun onEmailCheckChanged(isChecked: Boolean) {
        emailObservable.value = isChecked

        updateSaveButtonState()
    }

    fun onSaveClicked() {
        accountInfo?.let { currentAccountInfo ->
            val messageType = currentAccountInfo.getNotificationMessageType(pushObservable.value!!,
                    smsObservable.value!!, emailObservable.value!!) ?: ""

            progressOverlayShownObservable.value = true

            compositeDisposable.add(
                    EngageService.getInstance().getUpdateMessageTypeObservable(messageType)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                progressOverlayShownObservable.value = false
                                if (response.isSuccess) {
                                    dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_SUCCESS)
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

    private fun setUpData(loginResponse: LoginResponse) {
        val currentAccount = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        currentAccount?.let { currentAccountInfo ->
            accountInfo = currentAccountInfo
            pushObservable.value = currentAccountInfo.pushEnabled()

            if (currentAccountInfo.smsEnabled()) {
                smsObservable.value = true
            }

            phoneNumber.set(currentAccountInfo.phone)

            emailObservable.value = currentAccountInfo.emailEnabled()

        } ?: run {
            // show generic error message
            dialogInfoObservable.value = DialogInfo()
        }
    }

    private fun updateSaveButtonState() {
        if (pushObservable.value == accountInfo!!.pushEnabled()
                && smsObservable.value == accountInfo!!.smsEnabled()
                && emailObservable.value == accountInfo!!.emailEnabled()) {
            saveButtonStateObservable.value = SaveButtonState.HIDE
        } else {
            saveButtonStateObservable.value = SaveButtonState.SHOW
        }
    }
}