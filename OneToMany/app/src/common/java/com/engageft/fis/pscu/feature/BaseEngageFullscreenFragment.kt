package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.crashlytics.android.Crashlytics
import com.engageft.apptoolbox.BuildConfig
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles

/**
 * BaseEngageFullscreenFragment
 * <p>
 * Base Fragment paired with BaseEngageViewModel that automatically handles error logging for generic and
 * unhandled cases.
 * </p>
 * Created by joeyhutchins on 11/15/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseEngageFullscreenFragment : LotusFullScreenFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel?.let {
            if (it is BaseEngageViewModel) {
                it.dialogInfoObservable.observe(this, Observer { dialogInfo ->
                    when (dialogInfo.dialogType) {
                        DialogInfo.DialogType.GENERIC_ERROR -> {
                            showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                            showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(
                                    context!!, it)
                                    .apply { applyPaletteStyles(this@BaseEngageFullscreenFragment.context!!) })
                        }
                        DialogInfo.DialogType.SERVER_ERROR -> {
                            showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                            showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(
                                    context!!, it)
                                    .apply { applyPaletteStyles(this@BaseEngageFullscreenFragment.context!!) })
                        }
                        DialogInfo.DialogType.NO_INTERNET_CONNECTION -> {
                            showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                                    context!!,
                                    message = getString(R.string.alert_error_message_no_internet_connection))
                                    .apply { applyPaletteStyles(this@BaseEngageFullscreenFragment.context!!) })
                        }
                        DialogInfo.DialogType.CONNECTION_TIMEOUT -> {
                            showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                                    context!!,
                                    getString(R.string.alert_error_message_connection_timeout))
                                    .apply { applyPaletteStyles(this@BaseEngageFullscreenFragment.context!!) })
                        }
                        DialogInfo.DialogType.OTHER -> {
                            // Do nothing
                        }
                    }
                })
            }
        }
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

        showDialog(infoDialogGenericSuccessTitleMessageNewInstance(context!!, listener = listener))
    }

    fun handleGenericThrowable(e: Throwable) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        } else {
             Crashlytics.logException(e)
        }
    }
}