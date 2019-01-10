package com.engageft.fis.pscu.feature.utils

import android.content.Context
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.infoDialogYesNoNewInstance

fun showAlertConfirmationDialog(context: Context, title: String, message: String, listener: InformationDialogFragment.InformationDialogFragmentListener? = null) : InformationDialogFragment {
    val completeMessage = message + "\n" + context.getString(R.string.alert_continue_confirmation_message)

    return infoDialogYesNoNewInstance(context,
            title = title,
            message = completeMessage,
            listener = listener)
}