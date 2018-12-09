package utilGen1


import android.content.Context
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.AchAccountInfo

/**
 * AchAccountInfoUtils
 *
 * Provides utility methods for displaying ACH info.
 *
 * Created by Kurt Mueller on 6/19/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
object AchAccountInfoUtils {

    fun accountDescriptionForDisplay(context: Context, achAccountInfo: AchAccountInfo?): String {
        // achAccountInfo can be null if the accountId passed in the AchLoadInfo doesn't match an existing account
        if (achAccountInfo == null) {
            return String.format("%s %s", context.getString(R.string.BANKACCOUNT_NAME_UNKNOWN), context.getString(R.string.BANKACCOUNT_DESCRIPTION))
        }
        // TODO(aHashimi): maybe this should be format string?
        return String.format("%s %s %s", achAccountInfo.bankName, context.getString(R.string.BANKACCOUNT_DESCRIPTION), achAccountInfo.accountLastDigits)

    }

    fun accountTypeForDisplay(context: Context, achAccountInfo: AchAccountInfo): String {
        return accountTypeForDisplay(context, achAccountInfo.isChecking)
    }

    fun accountTypeForDisplay(context: Context, isChecking: Boolean): String {
        return if (isChecking) {
            context.getString(R.string.TEXT_CHECKING)
        } else {
            context.getString(R.string.TEXT_SAVINGS)
        }
    }

    fun accountTypeDisplayStrings(context: Context): List<String> {
        val accountTypeStrings = ArrayList<String>()
        accountTypeStrings.add(context.getString(R.string.TEXT_CHECKING))
        accountTypeStrings.add(context.getString(R.string.TEXT_SAVINGS))
        return accountTypeStrings
    }
}
