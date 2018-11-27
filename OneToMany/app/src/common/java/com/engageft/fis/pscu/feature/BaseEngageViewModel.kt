package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.BuildConfig
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.exception.NoConnectivityException
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
    var loginResponse: LoginResponse? = null
    val compositeDisposable = CompositeDisposable()

    init {
        loginResponse.let {
            compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        if (response is LoginResponse) {
                            loginResponse = response
                        } else {
                            dialogInfoObservable.value = DialogInfo()
                        }
                    }, { e ->
                        handleThrowable(e)
                    })
            )
        }

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun handleUnexpectedErrorResponse(response: BasicResponse) {
        if (BuildConfig.DEBUG) {
            dialogInfoObservable.value = DialogInfo(message = response.message)
        } else {
            dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_ERROR)
            Crashlytics.log(response.message)
        }
    }

    fun handleThrowable(e: Throwable)  {
        when (e) {
            is UnknownHostException -> {
                dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION)
            }
            is NoConnectivityException -> {
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