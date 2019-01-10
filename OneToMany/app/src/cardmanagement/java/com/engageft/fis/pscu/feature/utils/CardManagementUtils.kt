package com.engageft.fis.pscu.feature.utils

import android.content.Context
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.infoDialogYesNoNewInstance

fun informationDialogAlertConfirmation(context: Context, title: String, message: String, onPositiveButtonClicked: () -> Unit) : InformationDialogFragment {
    val completeMessage = message + "\n" + context.getString(R.string.alert_continue_confirmation_message)

    return infoDialogYesNoNewInstance(context,
            title = title,
            message = completeMessage,
            listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                override fun onDialogFragmentNegativeButtonClicked() {}

                override fun onDialogFragmentPositiveButtonClicked() = onPositiveButtonClicked()

                override fun onDialogCancelled() {}

            })
}