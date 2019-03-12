package com.engageft.fis.pscu.feature

import android.content.Context
import com.engageft.apptoolbox.ViewUtils.newLotusButtonsStackedInstance
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles

fun infoDialogGenericErrorTitleMessageNewInstance(context: Context,
                                                  title: String = context.getString(R.string.alert_error_title_generic),
                                                  message: String = context.getString(R.string.alert_error_message_generic),
                                                  buttonPositiveText: String = context.getString(R.string.dialog_information_ok_button),
                                                  listener: InformationDialogFragment.InformationDialogFragmentListener? = null) : InformationDialogFragment {
    return newInfoDialogInstance(context = context, title = title,
            message = message,
            buttonPositiveText = buttonPositiveText,
            listener = listener)
}

fun infoDialogSimpleMessageNoTitle(context: Context, message: String,
                                   buttonPositiveText: String = context.getString(R.string.dialog_information_ok_button)) : InformationDialogFragment {

    return newInfoDialogInstance(context = context, message = message, buttonPositiveText = buttonPositiveText)
}

fun infoDialogGenericErrorTitleMessageConditionalNewInstance(context: Context, dialogInfo: DialogInfo) : InformationDialogFragment {
    dialogInfo.message?.let { msg ->
        return infoDialogGenericErrorTitleMessageNewInstance(context, message = msg)
    }

    return infoDialogGenericErrorTitleMessageNewInstance(context)
}

fun infoDialogYesNoNewInstance(context: Context, title: String?, message: String?,
                               buttonPositiveText: String = context.getString(R.string.dialog_information_yes_button),
                               buttonNegativeText: String = context.getString(R.string.dialog_information_no_button),
                               listener: InformationDialogFragment.InformationDialogFragmentListener? = null): InformationDialogFragment {
    return newInfoDialogInstance(context = context,
            title = title,
            message = message,
            buttonPositiveText = buttonPositiveText,
            buttonNegativeText = buttonNegativeText,
            listener = listener)
}

fun infoDialogGenericSuccessTitleMessageNewInstance(context: Context,
                                                    title: String = context.getString(R.string.alert_success_title_generic),
                                                    message: String = context.getString(R.string.alert_success_message_generic),
                                                    buttonPositiveText: String = context.getString(R.string.dialog_information_ok_button),
                                                    listener: InformationDialogFragment.InformationDialogFragmentListener? = null) : InformationDialogFragment {
    return newInfoDialogInstance(context = context,
            title = title,
            message = message,
            buttonPositiveText = buttonPositiveText,
            listener = listener)
}

fun infoDialogGenericUnsavedChangesNewInstance(context: Context,
                                                    title: String = context.getString(R.string.dialog_unsaved_changes_title),
                                                    message: String = context.getString(R.string.dialog_unsaved_changes_message),
                                                    buttonPositiveText: String = context.getString(R.string.dialog_unsaved_changes_positive_text),
                                                    buttonNegativeText: String = context.getString(R.string.dialog_unsaved_changes_negative_text),
                                                    listener: InformationDialogFragment.InformationDialogFragmentListener? = null) : InformationDialogFragment {
    return newInfoDialogInstance(context = context,
            title = title,
            message = message,
            buttonPositiveText = buttonPositiveText,
            buttonNegativeText = buttonNegativeText,
            listener = listener)
}

fun newInfoDialogStackedInstance(context: Context,
        title: String? = null, message: String?, buttonPositiveText: String?, buttonNegativeText: String? = null,
        listener: InformationDialogFragment.InformationDialogFragmentListener? = null): InformationDialogFragment {
    val dialog = InformationDialogFragment.newLotusButtonsStackedInstance(title = title,
            message = message, buttonPositiveText = buttonPositiveText, buttonNegativeText = buttonNegativeText, listener = listener)
    dialog.applyPaletteStyles(context)
    return dialog
}

fun newInfoDialogInstance(context: Context,
                          title: String? = null, message: String?, buttonPositiveText: String?, buttonNegativeText: String? = null,
                          listener: InformationDialogFragment.InformationDialogFragmentListener? = null) : InformationDialogFragment {
    val dialog = InformationDialogFragment.newLotusInstance(title = title, message = message, buttonPositiveText = buttonPositiveText,
            buttonNegativeText = buttonNegativeText, listener = listener)
    dialog.applyPaletteStyles(context)
    return dialog
}