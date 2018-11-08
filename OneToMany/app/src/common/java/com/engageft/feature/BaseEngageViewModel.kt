package com.engageft.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


open class BaseEngageViewModel: BaseViewModel() {

    val dialogInfoObservable: MutableLiveData<DialogInfo> = MutableLiveData()

    fun handleThrowable(e: Throwable)  {
        when (e) {
            is UnknownHostException -> {
                dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION)
            }
            is ConnectException -> {
                dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION)
            }
            is SocketTimeoutException -> {
                dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.CONNECTION_TIMEOUT)
            }
            // Add more specific exceptions here, if needed
            else -> {
                dialogInfoObservable.value = DialogInfo()
            }
        }
    }
}