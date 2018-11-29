package com.engageft.fis.pscu.feature

import android.content.Context
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R

fun infoDialogGenericErrorTitleMessageNewInstance(context: Context,
                                                  title: String = context.getString(R.string.alert_error_title_generic),
                                                  message: String = context.getString(R.string.alert_error_message_generic),
                                                  buttonPositiveText: String = context.getString(R.string.dialog_information_ok_button)) : InformationDialogFragment {
    return InformationDialogFragment.newLotusInstance(
            title = title,
            message = message,
            buttonPositiveText = buttonPositiveText)
}

fun infoDialogSimpleMessageNoTitle(context: Context, message: String,
                                   buttonPositiveText: String = context.getString(R.string.dialog_information_ok_button)) : InformationDialogFragment {

    return InformationDialogFragment.newLotusInstance(message = message, buttonPositiveText = buttonPositiveText)
}

fun infoDialogGenericErrorTitleMessageConditionalNewInstance(context: Context, dialogInfo: DialogInfo) : InformationDialogFragment {
    dialogInfo.message?.let { msg ->
        return infoDialogGenericErrorTitleMessageNewInstance(context, message = msg)
    }

    return infoDialogGenericErrorTitleMessageNewInstance(context)
}

fun infoDialogGenericSuccessTitleMessageNewInstance(context: Context,
                                                    title: String = context.getString(R.string.alert_success_title_generic),
                                                    message: String = context.getString(R.string.alert_success_message_generic),
                                                    positiveButtonText: String = context.getString(R.string.dialog_information_ok_button),
                                                    listener: InformationDialogFragment.InformationDialogFragmentListener? = null) : InformationDialogFragment {
    return InformationDialogFragment.newLotusInstance(
            title = title,
            message = message,
            buttonPositiveText = positiveButtonText,
            listener = listener)
}