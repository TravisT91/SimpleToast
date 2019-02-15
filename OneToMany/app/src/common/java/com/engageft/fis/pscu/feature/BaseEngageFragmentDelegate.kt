package com.engageft.fis.pscu.feature

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.findNavController
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BaseFragmentDelegate
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.BuildConfig
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles

/**
 * BaseEngageFragmentDelegate
 * <p>
 * Shared features for the PageFragment, SubFragment, and DialogFragment. These features include unexpected
 * errors and repsonse handling and a few generic success dialogs. 
 * </p>
 * Created by joeyhutchins on 1/8/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BaseEngageFragmentDelegate(private val engageFragmentIm: BaseEngageFragmentIm) {
    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStartListener() {
            engageFragmentIm.getBaseFragmentDelegate().viewModel?.let { baseViewModel ->
                if (baseViewModel is BaseEngageViewModel) {
                    baseViewModel.dialogInfoObservable.observe(engageFragmentIm.getAndroidLifecycleOwner(), Observer { dialogInfo ->
                        when (dialogInfo.dialogType) {
                            DialogInfo.DialogType.GENERIC_ERROR -> {
                                engageFragmentIm.getBaseFragmentDelegate().showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(
                                        engageFragmentIm.getAndroidContext(),
                                        dialogInfo)
                                        .apply { applyPaletteStyles(engageFragmentIm.getAndroidContext()) })
                            }
                            DialogInfo.DialogType.SERVER_ERROR -> {
                                engageFragmentIm.getBaseFragmentDelegate().showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(
                                        engageFragmentIm.getAndroidContext(),
                                        dialogInfo)
                                        .apply { applyPaletteStyles(engageFragmentIm.getAndroidContext()) })
                            }
                            DialogInfo.DialogType.NO_INTERNET_CONNECTION -> {
                                engageFragmentIm.getBaseFragmentDelegate().showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                                        engageFragmentIm.getAndroidContext(),
                                        message = engageFragmentIm.getAndroidContext().getString(R.string.alert_error_message_no_internet_connection))
                                        .apply { applyPaletteStyles(engageFragmentIm.getAndroidContext()) })
                            }
                            DialogInfo.DialogType.UNKNOWN_HOST -> {
                                engageFragmentIm.getBaseFragmentDelegate().showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                                        engageFragmentIm.getAndroidContext(),
                                        message = engageFragmentIm.getAndroidContext().getString(R.string.alert_error_message_unknown_host))
                                        .apply { applyPaletteStyles(engageFragmentIm.getAndroidContext()) })
                            }
                            DialogInfo.DialogType.CONNECTION_TIMEOUT -> {
                                engageFragmentIm.getBaseFragmentDelegate().showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                                        engageFragmentIm.getAndroidContext(),
                                        engageFragmentIm.getAndroidContext().getString(R.string.alert_error_message_connection_timeout))
                                        .apply { applyPaletteStyles(engageFragmentIm.getAndroidContext()) })
                            }
                            DialogInfo.DialogType.OTHER -> {
                                // Do nothing
                            }
                        }
                    })
                }
            }
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStopListener() {
            engageFragmentIm.getBaseFragmentDelegate().viewModel?.let { baseViewModel ->
                if (baseViewModel is BaseEngageViewModel) {
                    baseViewModel.dialogInfoObservable.removeObservers(engageFragmentIm.getAndroidLifecycleOwner())
                }
            }
        }
    }

    init {
        engageFragmentIm.getAndroidLifecycle().addObserver(lifecycleObserver)
    }


    fun showGenericSuccessDialogMessageAndPopBackstack(view: View) {
        val listener = object: InformationDialogFragment.InformationDialogFragmentListener {
            override fun onDialogFragmentNegativeButtonClicked() {
            }

            override fun onDialogFragmentPositiveButtonClicked() {
                view.findNavController().popBackStack()
            }

            override fun onDialogCancelled() {
                view.findNavController().popBackStack()
            }
        }

        engageFragmentIm.getBaseFragmentDelegate().showDialog(infoDialogGenericSuccessTitleMessageNewInstance(view.context!!, listener = listener))
    }

    fun handleGenericThrowable(e: Throwable) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        } else {
            Crashlytics.logException(e)
        }
    }
}

interface BaseEngageFragmentIm {
    fun getBaseFragmentDelegate(): BaseFragmentDelegate
    fun getAndroidLifecycleOwner(): LifecycleOwner
    fun getAndroidContext(): Context
    fun getAndroidLifecycle(): Lifecycle
}