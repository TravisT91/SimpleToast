package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.BuildConfig
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
                if (BuildConfig.DEBUG) {
                    dialogInfoObservable.value = DialogInfo(message = e.message)
                    e.printStackTrace()
                } else {
                    Crashlytics.logException(e)
                }
            }
        }

    }
}