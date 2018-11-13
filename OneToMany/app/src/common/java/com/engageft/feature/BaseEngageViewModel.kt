package com.engageft.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.BuildConfig
import io.reactivex.disposables.CompositeDisposable
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * BaseEngageViewModel
 * <p>
 * BaseEngageViewModel for all other viewModel to inherit from
 * </p>
 * Created by Atia Hashimi on 11/7/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
open class BaseEngageViewModel: BaseViewModel() {

    enum class ButtonState {
        SHOW,
        HIDE
    }

    protected val compositeDisposable = CompositeDisposable()

    val dialogInfoObservable: MutableLiveData<DialogInfo> = MutableLiveData()

    val buttonState: MutableLiveData<ButtonState> = MutableLiveData()

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
                if (BuildConfig.DEBUG) {
                    dialogInfoObservable.value = DialogInfo(message = e.message)
                    e.printStackTrace()
                } else {
                    // TODO(aHashimi): https://engageft.atlassian.net/browse/FOTM-26
                    // Crashlytics.logException(e)
                }
            }
        }

    }
}