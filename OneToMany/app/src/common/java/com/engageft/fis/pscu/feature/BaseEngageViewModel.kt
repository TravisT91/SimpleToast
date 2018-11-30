package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.BuildConfig
import com.engageft.engagekit.rest.exception.NoConnectivityException
import com.ob.ws.dom.BasicResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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

inline fun <reified ExpectedClass> Observable<BasicResponse>.subscribeWithProgressAndDefaultErrorHandling(
        vm: BaseEngageViewModel,
        crossinline onNext: (ExpectedClass) -> Unit,
        noinline onError: ((e: Throwable) -> Unit)? = null,
        noinline onComplete: (() -> Unit?)? = null) : Disposable {
    return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    {
                        if(it.isSuccess && it is ExpectedClass){
                            onNext(it)
                        } else{
                            vm.handleUnexpectedErrorResponse(it)
                        }
                    },
                    {
                        vm.progressOverlayShownObservable.value = false
                        vm.handleThrowable(it)
                        onError?.invoke(it)
                    },
                    {
                        vm.progressOverlayShownObservable.value = false
                        onComplete?.invoke()
                    })
}