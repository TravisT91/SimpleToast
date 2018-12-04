package com.engageft.fis.pscu.feature

open class DialogInfo(var title: String? = null,
                      var message: String? = null,
                      var tag: String? = null,
                      var dialogType: DialogType = DialogType.GENERIC_ERROR) {
    enum class DialogType {
        GENERIC_SUCCESS,
        GENERIC_ERROR,
        SERVER_ERROR,
        GENERIC_SUCCESS,
        NO_INTERNET_CONNECTION,
        CONNECTION_TIMEOUT,
        OTHER
    }
}