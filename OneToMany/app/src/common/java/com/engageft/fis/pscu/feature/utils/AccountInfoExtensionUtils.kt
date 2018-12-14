package com.engageft.fis.pscu.feature.utils

import com.ob.ws.dom.utility.AccountInfo

class AccountInfoExtensionUtils {

    companion object {
        const val MESSAGE_TYPE_NONE = "NONE"
        const val MESSAGE_TYPE_EMAIL = "EMAIL"
        const val MESSAGE_TYPE_PUSH = "PUSH_NOTIFICATION"
        const val MESSAGE_TYPE_SMS = "SMS"
        const val MESSAGE_TYPE_EMAIL_PUSH = "EMAIL_AND_PUSH"
        const val MESSAGE_TYPE_EMAIL_SMS = "EMAIL_AND_SMS"
    }
}
fun AccountInfo.smsEnabled(): Boolean {
    return AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL_SMS == this.messageType || AccountInfoExtensionUtils.MESSAGE_TYPE_SMS == this.messageType
}

fun AccountInfo.pushEnabled(): Boolean {
    return AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL_PUSH == this.messageType || AccountInfoExtensionUtils.MESSAGE_TYPE_PUSH == this.messageType
}

fun AccountInfo.emailEnabled(): Boolean {
    return AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL_SMS == this.messageType || AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL_PUSH == this.messageType || AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL == this.messageType
}

fun AccountInfo.getNotificationMessageType(push: Boolean, sms: Boolean, email: Boolean): String {
    return when {
        email && push -> AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL_PUSH
        email && sms -> AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL_SMS
        push -> AccountInfoExtensionUtils.MESSAGE_TYPE_PUSH
        sms -> AccountInfoExtensionUtils.MESSAGE_TYPE_SMS
        email -> AccountInfoExtensionUtils.MESSAGE_TYPE_EMAIL
        else -> AccountInfoExtensionUtils.MESSAGE_TYPE_NONE
    }
}