package com.engageft.fis.pscu.feature

import androidx.databinding.ObservableField
import com.engageft.engagekit.EngageService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.rest.request.AchAccountValidateRequest


class VerifyAchBankAccountViewModel: BaseEngageViewModel() {

    var showButton: ObservableField<Boolean> = ObservableField(false)
    var amount1: ObservableField<String> = ObservableField("")
    var amount2: ObservableField<String> = ObservableField("")
    var achAccountInfoId: Long = 0L

    init {
        //TODO: verify account by account id passed. if not found, showDialog & popoff fragment
    }

    fun onVerifyAccount() {
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
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }
}