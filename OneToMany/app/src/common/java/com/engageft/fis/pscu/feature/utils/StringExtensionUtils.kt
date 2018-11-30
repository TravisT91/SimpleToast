package com.engageft.fis.pscu.feature.utils

fun String.isValidPassword(): Boolean {
    // TODO: this could be constructed or modified on the fly based on params in engageconfig.plist, though that would
    // require modifying regex to accommodate all password params in engageconfig.plist (upper-case, special chars, etc).
    // Or do it like iOS and verify each requirement individually, allowing for accurate error messages.
    // See https://engageft.atlassian.net/browse/CTB-179
    return this.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{6,500}$".toRegex())
}