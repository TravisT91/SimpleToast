package utilGen1

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.apptoolbox.util.CustomTypefaceSpan
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.DebitCardInfoUtils
import com.engageft.fis.pscu.OneToManyApplication
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.AddressInfo
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.PayPlanInfo
import org.joda.time.DateTime
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

/**
 * StringUtils
 *
 * Gen1 string utilities.
 *
 * Created by Kurt Mueller on 1/30/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
object StringUtils {

    private val TAG = "StringUtils"

    // these must match formatter enum defined in attrs.xml exactly
    val FORMATTER_PHONE = 0

    private var numberFormat: NumberFormat? = null
    private var numberFormatWithTwoFractionDigits: NumberFormat? = null

    /**
     * Initialize NumberFormat for currency formatting
     *
     * @return
     */
    init {
        numberFormat = NumberFormat.getCurrencyInstance()
        // TODO(jhutchins): Refactor this : https://engageft.atlassian.net/browse/SHOW-385
        numberFormat!!.currency = Currency.getInstance("USD")
//        numberFormat!!.currency = Currency.getInstance(EngageConfig.CURRENCY)
        numberFormat!!.maximumFractionDigits = 0

        numberFormatWithTwoFractionDigits = NumberFormat.getCurrencyInstance()
        // TODO(jhutchins): Refactor this : https://engageft.atlassian.net/browse/SHOW-385
        numberFormatWithTwoFractionDigits!!.currency = Currency.getInstance("USD")
//        numberFormatWithTwoFractionDigits!!.currency = Currency.getInstance(EngageConfig.CURRENCY)
        numberFormatWithTwoFractionDigits!!.minimumFractionDigits = 2
        numberFormatWithTwoFractionDigits!!.maximumFractionDigits = 2
    }

    /**
     * Formats an input string of digits with a currency symbol and separators as needed
     *
     * @param input  the string of digits representing a currency amount
     * @return the updated input string, with currency symbol and separators added
     */
    fun formatCurrencyString(input: String): String {
        return formatCurrencyString(input, false, false)
    }

    /**
     * Formats an input string of digits with a currency symbol and separators as needed, with two fraction digits
     *
     * @param input  the string of digits representing a currency amount
     * @return the updated input string, with currency symbol and separators added
     */
    fun formatCurrencyStringWithFractionDigits(input: String, showZeroDigits: Boolean): String {
        return formatCurrencyString(input, true, showZeroDigits)
    }

    /**
     * Formats an input string of digits with a currency symbol and separators as needed, with two fraction digits,
     * making it negative if it is positive.
     *
     * @param input  the string of digits representing a currency amount
     * @return the updated input string, with currency symbol and separators added
     */
    fun formatCurrencyStringWithFractionDigitsMakeNegative(input: String, showZeroDigits: Boolean): String {
        var amount = getFloatFromString(input)
        if (amount > 0) {
            amount = amount * -1
        }
        return formatCurrencyString(amount, true, showZeroDigits)
    }

    /**
     * Formats an input float with a currency symbol and separators as needed
     *
     * @param input  the float representing a currency amount
     * @return the updated input string, with currency symbol and separators added
     */
    fun formatCurrencyStringWithFractionDigits(input: Float, showZeroDigits: Boolean): String {
        return formatCurrencyString(input, true, showZeroDigits)
    }

    /**
     *
     * @param input  the string of digits representing a currency amount
     * @param withFractionDigits  whether to return a String with fraction digits
     * @return the updated input string, with currency symbol and separators added
     */
    private fun formatCurrencyString(input: String, withFractionDigits: Boolean, showZeroDigits: Boolean): String {
        var result = input
        if (!TextUtils.isEmpty(input)) {
            try {
                val inputAsFloat = java.lang.Float.parseFloat(input)
                result = if (withFractionDigits && (showZeroDigits || inputAsFloat != inputAsFloat.toInt().toFloat()))
                    numberFormatWithTwoFractionDigits!!.format(inputAsFloat.toDouble())
                else
                    numberFormat!!.format(inputAsFloat.toDouble())
            } catch (e: NumberFormatException) {
                // input wasn't a string representation of a long.
            }

        }
        return result
    }

    /**
     * Formats an input float with a currency symbol and separators as needed
     *
     * @param input  the float representing a currency amount
     * @return the updated input string, with currency symbol and separators added
     */
    @JvmOverloads
    fun formatCurrencyString(input: Float, withFractionDigits: Boolean = false, showZeroDigits: Boolean = false): String {
        var input = input
        val result: String
        if (input == -0.0f) {
            input = 0.0f
        }
        if (input < 0) {
            input = Math.abs(input)
            result = "-" + if (withFractionDigits && (showZeroDigits || input != input.toInt().toFloat()))
                numberFormatWithTwoFractionDigits!!.format(input.toDouble())
            else
                numberFormat!!.format(input.toDouble())
        } else {
            result = if (withFractionDigits && (showZeroDigits || input != input.toInt().toFloat()))
                numberFormatWithTwoFractionDigits!!.format(input.toDouble())
            else
                numberFormat!!.format(input.toDouble())
        }
        return result
    }

    fun formatCurrencyString(input: Int): String {
        return formatCurrencyString(input.toFloat())
    }

    fun removeFractionDigitsFromNumericString(numericString: String): String? {
        var result = numericString
        if (!TextUtils.isEmpty(numericString)) {
            val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
            if (numericString.indexOf(decimalSeparator) != -1) {
                result = numericString.substring(0, numericString.indexOf(decimalSeparator))
            }
        }

        return result
    }

    /**
     * Parses a float from a String. Defaults to 0 if parsing fails.
     *
     * Note that this means that errors (missing values, or non-numeric values) in the webservice
     * response will be ignored, and 0 will be returned. This is to maintain consistency with
     * iOS app, which uses [NSString floatValue] extensively to get a float from a String, also
     * defaulting to 0 upon parse failure.
     *
     * @param input the String that may represent a Float
     * @return the float created from the String, if possible, or 0.
     */
    fun getFloatFromString(input: String): Float {
        var result = 0f
        if (input.isNotEmpty()) {
            try {
                result = java.lang.Float.parseFloat(input)
            } catch (e: NumberFormatException) {
                // string does not represent a number
            }

        }
        return result
    }

    /**
     * Parses a positive float from a String. Defaults to 0 if parsing fails.
     *
     * @param input the String that may represent a Float
     * @return the positive float created from the String, if possible, or 0.
     */
    fun getPositiveFloatFromString(input: String): Float {
        var result = 0f
        if (input != null && input.length > 0) {
            try {
                result = java.lang.Float.parseFloat(input.replace("-", ""))
            } catch (e: NumberFormatException) {
                // string does not represent a number
            }

        }
        return result
    }

    /**
     * Parses an int from a String. Defaults to 0 if parsing fails.
     *
     * @param input the String that may represent a number
     * @return the int created from the String, if possible, or 0.
     */
    fun getIntFromString(input: String): Int {
        val resultAsFloat = getFloatFromString(input)
        return resultAsFloat.toInt()
    }

    /**
     * Parses a positive int from a String. Defaults to 0 if parsing fails.
     *
     * @param input the String that may represent a Float
     * @return the positive int created from the String, if possible, or 0.
     */
    fun getPositiveIntFromString(input: String): Int {
        val resultAsPositiveFloat = getPositiveFloatFromString(input)
        return resultAsPositiveFloat.toInt()
    }

    //    // Card row
    //    public static String formatCardDetailString(@NonNull Context context, @NonNull DebitCardInfo debitCardInfo) {
    //        return String.format("%s %s *%s",
    //                context.getString(R.string.card_display_name),
    //                context.getString(R.string.CARD_SUMMARY_CARD_LABEL),
    //                debitCardInfo.getLastFour());
    //    }

    // Dashboard date amount status row, when give input like $225.00, make .00 some percent height of rest of text
    // TODO(kurt) This will need to be updated to deal with other currencies, some of which may be at end of string. Currently assumes that last two chars are fraction digits.
    fun formatCurrencyStringFractionDigitsReducedHeight(amount: Float, fractionDigitsPercentHeight: Float, showZeroDigits: Boolean): CharSequence {
        val amountStringWithCurrency = formatCurrencyStringWithFractionDigits(amount, showZeroDigits)
        // TODO(jhutchins): Refactor this : https://engageft.atlassian.net/browse/SHOW-385
        if (amountStringWithCurrency.contains(CurrencyUtils.getCurrencySeparator("USD")) && amountStringWithCurrency.length >= 3) {
//        if (amountStringWithCurrency.contains(CurrencyUtils.getCurrencySeparator(EngageConfig.CURRENCY)) && amountStringWithCurrency.length >= 3) {
            val spannableString = SpannableString(amountStringWithCurrency)
            spannableString.setSpan(RelativeSizeSpan(fractionDigitsPercentHeight), amountStringWithCurrency.length - 3, amountStringWithCurrency.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableString
        } else {
            return amountStringWithCurrency
        }
    }

    fun formatCurrencyStringFractionDigitsReducedHeight(amountString: String, fractionDigitsPercentHeight: Float, showZeroDigits: Boolean): CharSequence {
        return formatCurrencyStringFractionDigitsReducedHeight(getFloatFromString(amountString), fractionDigitsPercentHeight, showZeroDigits)
    }

    // Dashboard daily living top row title (like "BUDGET OCTOBER" with "OCTOBER" smaller height);
    fun formatDashboardBudgetMonthWithMonthReducedHeight(context: Context): CharSequence {
        val budget = context.getString(R.string.BUDGET_SUMMARY_LABEL).toUpperCase()
        val monthFull = DisplayDateTimeUtils.currentMonthFullName.toUpperCase()
        return formatStringWithEndHalfHeight(budget, monthFull)
    }

    // Dashboard all expenses top row title (like "TOTAL SPENT OCTOBER" with "OCTOBER" smaller height);
    fun formatDashboardTotalSpentMonthWithMonthReducedHeight(context: Context): CharSequence {
        val totalSpent = context.getString(R.string.TOTAL_SPENT_LABEL).toUpperCase()
        val monthFull = DisplayDateTimeUtils.currentMonthFullName.toUpperCase()
        return formatStringWithEndHalfHeight(totalSpent, monthFull)
    }

    private fun formatStringWithEndHalfHeight(startString: String, endString: String): CharSequence {
        val spannableString = SpannableString(String.format("%s %s",
                startString,
                endString))
        spannableString.setSpan(RelativeSizeSpan(0.7f), startString.length + 1, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    fun formatDateMonthDayForTransactionRow(dateString: String): CharSequence? {
        val transactionDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(dateString)
        return if (transactionDate != null) formatDateMonthDayForTransactionRow(transactionDate) else null
    }

    fun formatDateMonthDayForTransactionRow(transactionDate: DateTime): CharSequence {
        val spannableString = SpannableString(String.format("%s\n%s",
                DisplayDateTimeUtils.getDayTwoDigits(transactionDate),
                DisplayDateTimeUtils.getMonthAbbr(transactionDate).toUpperCase()))
        spannableString.setSpan(RelativeSizeSpan(0.7f), 2, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    // My Accounts
    fun formatAddressString(addressInfo: AddressInfo): String {
        var addressString: String
        if (TextUtils.isEmpty(addressInfo.address2)) {
            addressString = addressInfo.address1
        } else {
            addressString = String.format("%s, %s", addressInfo.address1, addressInfo.address2)
        }
        addressString += String.format("\n%s, %s %s", addressInfo.city, addressInfo.state, addressInfo.zip)

        return addressString
    }

    // CardView
    fun formatCardNumberString(cardNumber: String): String {
        val buff = StringBuffer()
        for (i in 0 until cardNumber.length) {
            buff.append(cardNumber[i])
            if ((i + 1) % 4 == 0) {
                buff.append(' ')
            }
        }

        return buff.toString().trim { it <= ' ' }
    }

    // Settings / Profile
    fun formatPhoneNumberString(phoneNumber: String, countryCode: String): String {
        var result: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = PhoneNumberUtils.formatNumber(phoneNumber, countryCode)
        } else {
            result = PhoneNumberUtils.formatNumber(phoneNumber)
        }

        // PhoneNumberUtils does not format the "+" character into the string, so we check if this is
        // an 11 digit number here and prepend the + if it is. This is necessary because of maxLength
        // settings on our EditTexts is set to phone number length + 4. This seemingly arbitrary number
        // is determined from how many typical formatting characters PhoneNumberFormattingTextWatcher
        // adds to phone number strings during user input.
        if (phoneNumber.length == 11) {
            result = "+$phoneNumber"
        }
        return result
    }

    // from DebitCardInfoUtils
    fun getDebitCardInfoFriendlyStatus(debitCardInfo: DebitCardInfo): String {
        val resId = when {
            DebitCardInfoUtils.hasVirtualCard(debitCardInfo) && EngageService.getInstance().engageConfig.virtualCardEnabled ->
                R.string.CARD_STATUS_DISPLAY_VIRTUAL
            DebitCardInfoUtils.isLocked(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_LOCKED
            DebitCardInfoUtils.isPendingActivation(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_PENDING
            DebitCardInfoUtils.isLostStolen(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_REPLACED
            DebitCardInfoUtils.isCancelled(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_CANCELLED
            DebitCardInfoUtils.isSuspended(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_SUSPENDED
            DebitCardInfoUtils.isFraudStatus(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_CLOSED
            DebitCardInfoUtils.isOrdered(debitCardInfo) ->
                R.string.CARD_STATUS_DISPLAY_ORDERED
            else ->
                R.string.CARD_STATUS_DISPLAY_ACTIVE // active if status = ACTIVE, REPLACEMENT ORDERED, or REPLACED
        }

        return OneToManyApplication.sInstance.getString(resId)
    }

    @JvmOverloads
    fun capitalize(input: String?, delimiters: CharArray? = null): String? {
        val delimLen = delimiters?.size ?: -1
        if (input == null || input.length == 0 || delimLen == 0) {
            return input
        }
        val strLen = input.length
        val buffer = StringBuffer(strLen)
        var capitalizeNext = true
        for (i in 0 until strLen) {
            val ch = input[i]

            if (isDelimiter(ch, delimiters)) {
                buffer.append(ch)
                capitalizeNext = true
            } else if (capitalizeNext) {
                buffer.append(Character.toTitleCase(ch))
                capitalizeNext = false
            } else {
                buffer.append(Character.toLowerCase(ch))
            }
        }
        return buffer.toString()
    }

    private fun isDelimiter(ch: Char, delimiters: CharArray?): Boolean {
        if (delimiters == null) {
            return Character.isWhitespace(ch) || Character.isDigit(ch)
        }
        var i = 0
        val isize = delimiters.size
        while (i < isize) {
            if (ch == delimiters[i]) {
                return true
            }
            i++
        }
        return false
    }

    fun removeRedundantWhitespace(input: String?): String {
        input?.let {
            return it.trim { it <= ' ' }.replace(" +".toRegex(), " ")
        } ?: run {
            return ""
        }
    }

    fun fromHtml(htmlString: String): Spanned {
        return if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(htmlString)
        }
    }

    fun applyTypefaceToString(context: Context, typefaceResId: Int, target: String): SpannableString {
        return applyTypefaceAndColorToString(context, typefaceResId, 0, target)
    }

    fun applyColorToString(context: Context, colorResId: Int, target: String): SpannableString {
        return applyTypefaceAndColorToString(context, 0, colorResId, target)
    }

    fun applyTypefaceAndColorToString(context: Context, typefaceResId: Int, colorResId: Int, target: String): SpannableString {
        val spannableString = SpannableString(target)
        if (typefaceResId != 0) {
            val typeface = ResourcesCompat.getFont(context, typefaceResId)
            if (typeface != null) {
                val customTypefaceSpan = CustomTypefaceSpan(typeface)
                spannableString.setSpan(customTypefaceSpan, 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        if (colorResId != 0) {
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, colorResId))
            spannableString.setSpan(colorSpan, 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableString
    }

    fun applyTypefaceAndColorToSubString(color: Int, typeface: Typeface, target: String, substring: String): SpannableString {
        val indexOfSubstring = target.indexOf(substring)

        val spannableString = applyTypefaceToSubstring(typeface, target, substring)

        val colorSpan = ForegroundColorSpan(color)
        spannableString.setSpan(colorSpan, indexOfSubstring, indexOfSubstring + substring.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    fun applyColorToSubstring(context: Context, colorResId: Int, target: String, substring: String): SpannableString {
        val color = ContextCompat.getColor(context, colorResId)
        return applyColorToSubstring(color, target, substring)
    }

    fun applyColorToSubstring(color: Int, target: String?, substring: String): SpannableString {
        val spannableString = SpannableString(target)
        val indexOfSubstring = target!!.indexOf(substring)
        if (color != 0 && target != null && indexOfSubstring != -1) {
            val colorSpan = ForegroundColorSpan(color)
            spannableString.setSpan(colorSpan, indexOfSubstring, indexOfSubstring + substring.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableString
    }

    fun applyTypefaceToSubstring(typeface: Typeface?, target: String, substring: String): SpannableString {
        val spannableString = SpannableString(target)
        if (typeface != null && target.contains(substring)) {
            val indexOfSubstring = target.indexOf(substring)
            val customTypefaceSpan = CustomTypefaceSpan(typeface)
            spannableString.setSpan(customTypefaceSpan, indexOfSubstring, indexOfSubstring + substring.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannableString
    }

    fun applyTypefaceToSubstring(context: Context, typefaceResId: Int, target: String, substring: String): SpannableString {
        val typeface = ResourcesCompat.getFont(context, typefaceResId)
        return applyTypefaceToSubstring(typeface, target, substring)
    }

    fun applyTypefacesToSubstrings(context: Context, target: String, typefaceAndSubstringPairs: List<Pair<Int, String>>): SpannableString {
        val spannableString = SpannableString(target)
        for ((first, second) in typefaceAndSubstringPairs) {
            val typeface = ResourcesCompat.getFont(context, first)
            if (typeface != null && target.contains(second)) {
                val indexOfSubstring = target.indexOf(second)
                val customTypefaceSpan = CustomTypefaceSpan(typeface)
                spannableString.setSpan(customTypefaceSpan, indexOfSubstring, indexOfSubstring + second.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return spannableString
    }

    fun applyHtmlFormatTagsToString(context: Context?, target: String?): SpannableString {
        val boldTagOpen = "<b>"
        val boldTagClosed = "</b>"
        val italicTagOpen = "<i>"
        val italicTagClosed = "</i>"
        val typefaceAndSubstringPairs = ArrayList<Pair<Int, String>>()
        if (context != null && target != null) {
            // bold
            if (target.contains(boldTagOpen) && target.contains(boldTagClosed)) {
                val substring = target.substring(target.indexOf(boldTagOpen) + boldTagOpen.length, target.indexOf(boldTagClosed))
                if (substring.length > 0) {
                    typefaceAndSubstringPairs.add(Pair(R.font.font_bold, substring))
                }
            }

            // italic
            if (target.contains(italicTagOpen) && target.contains(italicTagClosed)) {
                val substring = target.substring(target.indexOf(italicTagOpen) + italicTagOpen.length, target.indexOf(italicTagClosed))
                if (substring.length > 0) {
                    typefaceAndSubstringPairs.add(Pair(R.font.font_italic, substring))
                }
            }

            if (typefaceAndSubstringPairs.size > 0) {
                val cleanedTarget = target.replace(boldTagOpen, "").replace(boldTagClosed, "").replace(italicTagOpen, "").replace(italicTagClosed, "")
                return applyTypefacesToSubstrings(context, cleanedTarget, typefaceAndSubstringPairs)
            }
        }

        return SpannableString(target)
    }

    // if paused, "paused", or like "$5.14/Daily"
    // TODO(travis): This would be nice as a GoalInfo extension
    fun getGoalInfoContributionString(context: Context, goalInfo: GoalInfo?): String? {
        var result: String? = null

        if (goalInfo != null && GoalInfoUtils.isCompleted(goalInfo)) {
            result = context.getString(R.string.GOALS_COMPLETE)
        } else if (goalInfo != null && goalInfo.payPlan != null) { // allow null payPlan.amount, which will be handled in getPayPlanInfoContributionString()
            result = getPayPlanInfoContributionString(context, goalInfo.payPlan)
        }

        return result
    }

    // if paused, "paused", or like "$5.14/day"
    // TODO(travis): This would be nice as a PayPlanInfo extension
    fun getPayPlanInfoContributionString(context: Context, payPlan: PayPlanInfo?): String? {
        val result: String

        if (payPlan == null) {
            return null
        } else if (payPlan.isPaused) {
            result = context.getString(R.string.GOALS_PAUSED)
        } else {
            val planAmount = if (payPlan.amount != null) payPlan.amount.toPlainString() else "0"
            result = String.format(context.getString(R.string.GOALS_RECURRENCE_FORMAT),
                    formatCurrencyStringWithFractionDigits(planAmount, true),
                    PayPlanUtils.getPayPlanFrequencyDisplayStringForRecurrenceType(context, payPlan.recurrenceType))
        }

        return result
    }

    // like "by Feb 29, 2020"
    // TODO(travis): This would be nice as a GoalInfo extension
    fun getGoalInfoCompletionDateString(context: Context, goalInfo: GoalInfo): String? {
        var result: String? = null

        val completeDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate)
        if (completeDate != null) {
            val completeDateString = DisplayDateTimeUtils.getMediumFormatted(completeDate)
            if (GoalInfoUtils.isCompleted(goalInfo)) {
                result = String.format(context.getString(R.string.GOALS_ON_DATE_FORMAT), completeDateString)
            } else {
                result = String.format(context.getString(R.string.GOALS_BY_DATE_FORMAT), completeDateString)
            }
        }

        return result
    }

    // "$6 of $29"
    // TODO(travis): This would be nice as a GoalInfo extension
    fun getGoalInfoProgressString(context: Context, goalInfo: GoalInfo?): String? {
        var result: String? = null

        if (goalInfo != null) {
            result = String.format(context.getString(R.string.GOALS_PROGRESS_FORMAT),
                    formatCurrencyString(if (goalInfo.fundAmount != null) goalInfo.fundAmount.toPlainString() else "0"),
                    formatCurrencyString(if (goalInfo.amount != null) goalInfo.amount.toPlainString() else "0"))
        }

        return result
    }

    // "Last updated 2/7"
    fun getLastUpdatedString(dateTime: DateTime?): String {
        return if (dateTime != null) {
            String.format(OneToManyApplication.sInstance.getString(R.string.LAST_UPDATED_FORMAT), DisplayDateTimeUtils.getMonthDayDigits(dateTime))
        } else {
            ""
        }
    }

    fun getSpannableStringForDashboardPagerTitle(context: Context, titleTextWithMarkup: String): CharSequence {
        val markupStart = titleTextWithMarkup.indexOf('[')
        val markupEnd = titleTextWithMarkup.indexOf(']')
        return if (markupStart != -1 && markupEnd != -1 && markupStart < markupEnd) {
            SpannableStringBuilder()
                    .append(titleTextWithMarkup.substring(0, markupStart))
                    .append(applyTypefaceAndColorToString(context, R.font.font_bold, R.color.primary, titleTextWithMarkup.substring(markupStart + 1, markupEnd)))
                    .append(titleTextWithMarkup.substring(markupEnd + 1, titleTextWithMarkup.length))
        } else {
            titleTextWithMarkup
        }
    }

    fun getCurrencyTextColorForAmount(amount: Float): Int {
        return if (amount < 0f) {
            R.color.error
        } else if (amount == 0f) {
            R.color.currencySymbolTint
        } else {
            R.color.currencySymbolTint
        }
    }

    fun getStringForId(context: Context, stringName: String): String? {
        val resId = context.resources.getIdentifier(stringName, "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            null
        }
    }

    fun convertEmojis(string: String): String {
        return string.replace("&#128077;", "👍")
                .replace("&#128513;", "😁")
                .replace("&#128552;", "😨")
                .replace("&#128562;", "😲")
    }

    fun stripNonNumerics(str: String?): String? {
        return str?.replace("[^0-9]".toRegex(), "") ?: str
    }
}
/**
 * Formats an input float with a currency symbol and separators as needed
 *
 * @param input  the float representing a currency amount
 * @return the updated input string, with currency symbol and separators added
 */// Convert a string to title case (each word capitalized) -- allows reuse of iOS Localizable.strings that has many all uppercase strings
// Taken from Apache WordUtils, courtesy of SO:
