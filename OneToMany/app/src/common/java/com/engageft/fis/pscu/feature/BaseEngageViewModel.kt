package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.BuildConfig
import com.engageft.engagekit.rest.exception.NoConnectivityException
import com.ob.ws.dom.BasicResponse
import io.reactivex.Observable
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
    val compositeDisposable = CompositeDisposable()

    val dialogInfoObservable: MutableLiveData<DialogInfo> = MutableLiveData()

    fun handleUnexpectedErrorResponse(response: BasicResponse) {
        // This is a catch-all utility function to handle all unexpected responses to an API call
        // and report it essentially as a BUG. In debug builds, let's blow up in the user's face,
        // but in production, show a generic error, fail gracefully, and report it over crashlytics.
        if (BuildConfig.DEBUG) {
            dialogInfoObservable.postValue(DialogInfo(response.message))
        } else {
            dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_ERROR))
        }
        // Report this problem to Crashlytics in case a bug report is not made.
        Crashlytics.logException(IllegalStateException("handleUnexpectedErrorReponse: " + response.message))
    }

    fun handleThrowable(e: Throwable)  {
        when (e) {
            is UnknownHostException -> {
                dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION))
            }
            is NoConnectivityException -> {
                dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.NO_INTERNET_CONNECTION))
            }
            is SocketTimeoutException -> {
                dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.CONNECTION_TIMEOUT))
            }
            // Add more specific exceptions here, if needed
            else -> {
                // This is a catch-all for anything else. Anything caught here is a BUG and should
                // be reported as such. In Debug builds, we can just blow up in the user's face but
                // on production, we need to fail gracefully and report the error so we can fix it
                // later.
                if (BuildConfig.DEBUG) {
                    dialogInfoObservable.postValue(DialogInfo(message = e.message))
                    e.printStackTrace()
                    // Just in case the user at the time doesn't report a bug to us.
                } else {
                    dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_ERROR)
                }
                Crashlytics.logException(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}

inline fun <reified ExpectedClass> Observable<BasicResponse>.subscribeWithDefaultProgressAndErrorHandling(
        vm: BaseEngageViewModel,
        crossinline onNextSuccessful: (ExpectedClass) -> Unit,
        noinline onNextFailed: ((BasicResponse) -> Unit)? = null,
        noinline onError: ((e: Throwable) -> Unit)? = null,
        noinline onComplete: (() -> Unit?)? = null){
    vm.progressOverlayShownObservable.value = true
    vm.compositeDisposable.add(
            this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                    {
                        if (it.isSuccess && it is ExpectedClass) {
                            onNextSuccessful(it)
                        } else {
                            onNextFailed?.invoke(it) ?: run { vm.handleUnexpectedErrorResponse(it) }
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
                    }))
}