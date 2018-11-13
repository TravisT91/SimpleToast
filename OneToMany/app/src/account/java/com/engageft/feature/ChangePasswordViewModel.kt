package com.engageft.feature

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.tools.MixpanelEvent



class ChangePasswordViewModel: BaseEngageViewModel() {
    var currentPassword : ObservableField<String> = ObservableField("")
    var newPassword1 : ObservableField<String> = ObservableField("")
    var newPassword2 : ObservableField<String> = ObservableField("")
    //TODO button state: both new fields are a match and oldPassword field is not empty
    //
    var validation = MutableLiveData<Boolean>()

    fun updatePassword() {
        if (EngageService.getInstance().authManager.isLoggedIn && !EngageService.getInstance().authManager.checkSecuritySession()) {
            progressOverlayShownObservable.value = true

            EngageService.getInstance().mixpanel.track(MixpanelEvent.mpEventUpdatePassword)

            compositeDisposable.add(
                    EngageService.getInstance().getUpdatePasswordObservable(EngageService.getInstance().authManager.authToken, newPassword1.get()!!, currentPassword.get()!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                progressOverlayShownObservable.value = false
                                if (response.isSuccess) {
                                    dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_SUCCESS)
                                } else {
                                    dialogInfoObservable.value = DialogInfo(response.message, dialogType = DialogInfo.DialogType.SERVER_ERROR)
                                }
                            }, { e ->
                                progressOverlayShownObservable.value = true
                                handleThrowable(e)
                            })
            )
        } else {
            EngageService.getInstance().authManager.logout()
        }
    }
}