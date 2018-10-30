package utilGen1

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.engageft.onetomany.OneToManyApplication
import com.engageft.onetomany.R
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.MutableDateTime
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.DateFormatSymbols
import java.util.*

/**
 * DisplayDateTimeUtils
 *
 * Helper class encapsulating all DateTime formatting for the UI. Formatting for any backend values should
 * use the BackendDateTimeUtils instead. Formatters are organized around localization.
 * Some localization occurs automatically by the jodaTime objects, however some localization has to be
 * done manually (in strings.xml).
 *
 * Created by Kurt Mueller on 1/30/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object DisplayDateTimeUtils {

    private const val TAG = "DisplayDateTimeUtils"

    // Localized format patterns:
    private val fullFormatter = DateTimeFormat.fullDate() // "Thursday, November 27, 2016";
    private val longDateFormatter = DateTimeFormat.longDate() // February 10, 2017;
    private val mediumDateFormatter = DateTimeFormat.mediumDate() // "Feb 10, 2017";

    // strings.xml format patterns:
    val shortDateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_short)) // 02/10/2017;
    val hourMinuteSecondMillisFormatter: DateTimeFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_hour_minute_second_milli))
    private val monthYearFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_month_year)) // "Nov 2016";
    private val monthFullYearFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_fullmonth_year)) // "November 2016";
    private val monthAbbrDayTwoDigitsFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_month_day)) // "Nov 15"
    val yearMonthDayFormatter: DateTimeFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_year_shortmonth_day)) // "2017-03-07";
    val monthDayFormatter: DateTimeFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_longmonth_day)) // "November 27";
    private val monthDayDigitsFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_shortmonth_day)) // "01/15";
    private val expirationMonthYearFormatter = DateTimeFormat.forPattern(OneToManyApplication.sInstance.getString(R.string.format_datetime_shortmonth_year)) // "01/16"

    // Other format patterns:
    private val monthFullFormatter = DateTimeFormat.forPattern("MMMM") // "November";
    private val monthAbbrFormatter = DateTimeFormat.forPattern("MMM") // "Nov";
    private val dayTwoDigitsFormatter = DateTimeFormat.forPattern("dd") // "15";

    var MINIMUM_AGE_OF_USER_YEARS = 18

    val currentMonthFullName: String
        get() = monthFullFormatter.print(DateTime())

    // Feb 10, 2017
    val currentMonthDayYear: String
        get() = getMediumFormatted(DateTime())

    // July 2017
    val currentMonthFullYear: String
        get() {
            return monthFullYearFormatter.print(DateTime())
        }

    // February 10, 2017
    val currentLongDate: String
        get() = longDateFormatter.print(DateTime())

    val monthTwoDigitKeysAndValuesForCardExpDisplay: Map<String, String>
        get() {
            val map = LinkedHashMap<String, String>()
            val dateFormatSymbols = DateFormatSymbols(Locale.getDefault())
            var i = 0
            for (value in Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")) {
                map[String.format("%s %s", value, dateFormatSymbols.months[i++])] = value
            }

            return map
        }

    val yearTwoDigitKeysAndValuesForCardExpDisplay: Map<String, String>
        get() {
            val map = LinkedHashMap<String, String>()
            val now = DateTime.now()
            val thisYear = now.year
            for (i in 0..19) {
                map[(thisYear + i).toString()] = (thisYear + i - 2000).toString()
            }

            return map
        }

    fun fractionOfCurrentMonthPassed(): Float {
        val dateTime = DateTime.now()
        val dayOfMonth = dateTime.dayOfMonth.toFloat()
        val daysInMonth = dateTime.dayOfMonth().maximumValue.toFloat()
        return (dayOfMonth - 1) / (daysInMonth - 1) // on 1st, returns 0.0f; on last, returns 1.0f
    }

    /**
     * Constructs a string to display in the left column of a trend row for a given month and year
     *
     * @param month  The int representing the month (1 is January)
     * @param year  The int representing the year
     * @return  The date represented as "MMM YYYY" (i.e., "Jan 2016")
     */
    fun formatTrendLabelDate(month: Int, year: Int): String {
        try {
            val dateTime = DateTime(year, month, 1, 0, 0)
            return monthYearFormatter.print(dateTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting a month/year data string: " + e.message)
            return ""
        }

    }

    // returns "November 2016" for year 2016 and month 11
    fun getMonthFullYear(year: Int, month: Int): String {
        val dateTime = MutableDateTime()
        dateTime.year = year
        dateTime.monthOfYear = month
        return monthFullYearFormatter.print(dateTime)
    }

    // returns "Nov 2016" for year 2016 and month 11
    fun getMonthAbbrYear(year: Int, month: Int): String {
        val dateTime = MutableDateTime()
        dateTime.year = year
        dateTime.monthOfYear = month
        return monthYearFormatter.print(dateTime)
    }

    fun getMediumFormatted(dateTime: DateTime): String {
        return mediumDateFormatter.print(dateTime)
    }

    fun getMonthAbbr(dateTime: DateTime): String {
        return monthAbbrFormatter.print(dateTime)
    }

    fun getDayTwoDigits(dateTime: DateTime): String {
        return dayTwoDigitsFormatter.print(dateTime)
    }

    fun getMonthDayDigits(dateTime: DateTime): String {
        return monthDayDigitsFormatter.print(dateTime)
    }

    fun getMonthAbbrDayTwoDigits(dateTime: DateTime): String {
        return monthAbbrDayTwoDigitsFormatter.print(dateTime)
    }

    fun getExpirationMonthYear(dateTime: DateTime): String {
        return expirationMonthYearFormatter.print(dateTime)
    }

    fun getLongDateFromDateTime(dateTime: DateTime): String {
        return longDateFormatter.print(dateTime)
    }

    fun getAlertAgeFromDateTime(context: Context, dateTime: DateTime): String {
        var result = ""
        val now = DateTime.now()
        val seconds = Seconds.secondsBetween(dateTime, now).seconds
        if (seconds < 60) {
            result = context.getString(R.string.DATE_JUST_NOW)
        } else {
            // convert to minutes
            val minutes = Minutes.minutesBetween(dateTime, now).minutes
            if (minutes < 60) {
                result = context.resources.getQuantityString(R.plurals.TIME_INTERVAL_FORMAT_MINUTES, minutes, minutes)
            } else {
                // convert to hours
                val hours = Hours.hoursBetween(dateTime, now).hours
                if (hours < 24) {
                    result = context.resources.getQuantityString(R.plurals.TIME_INTERVAL_FORMAT_HOURS, hours, hours)
                } else {
                    val days = Days.daysBetween(dateTime, now).days
                    result = String.format(context.getString(R.string.TIME_INTERVAL_FORMAT_DAYS), days)
                }
            }
        }

        return result
    }

    fun daysOfWeekList(): List<String> {
        val daysOfWeek = DateFormatSymbols.getInstance(Locale.getDefault()).weekdays
        val days = ArrayList<String>()
        for (i in daysOfWeek.indices) {
            if (!TextUtils.isEmpty(daysOfWeek[i])) {    // daysOfWeek[0] is empty string
                days.add(daysOfWeek[i])
            }
        }

        return days
    }

    /**
     * This method converts a backend dayOfWeek number to a localized String representing that day of week.
     */
    fun getDayOfWeekStringForNumber(number: Int): String {
        // Days of week in the following array come from Calendar object. That object maps days of week
        // exactly the same as Engage backend.
        val daysOfWeek = DateFormatSymbols.getInstance(Locale.getDefault()).weekdays
        return daysOfWeek[number]
    }

    /**
     * Rather convoluted convenience method to convert a Day of Week that might be localized to
     * a numeric integer the backend will know how to process. This method only supports strings
     * provided from daysOfWeekList() or getDayOfWeekStringForNumber() methods.
     */
    fun getDayOfWeekNumber(dayOfWeek: String): Int {
        // Days of week in the following array come from Calendar object. That object maps days of week
        // exactly the same as Engage backend.
        val daysOfWeek = DateFormatSymbols.getInstance(Locale.getDefault()).weekdays
        return if (daysOfWeek[1] == dayOfWeek) {
            // This is Sunday
            1
        } else if (daysOfWeek[2] == dayOfWeek) {
            // This is Monday
            2
        } else if (daysOfWeek[3] == dayOfWeek) {
            // This is Tuesday
            3
        } else if (daysOfWeek[4] == dayOfWeek) {
            // This is Wednesday
            4
        } else if (daysOfWeek[5] == dayOfWeek) {
            // This is Thursday
            5
        } else if (daysOfWeek[6] == dayOfWeek) {
            // This is Friday
            6
        } else if (daysOfWeek[7] == dayOfWeek) {
            // This is Saturday
            7
        } else {
            -1
        }
    }

    fun minutesSinceDateTime(dateTime: DateTime?): Int {
        var minutes = 0
        if (dateTime != null) {
            minutes = Seconds.secondsBetween(dateTime, DateTime.now()).seconds
        }

        return minutes
    }

    fun daysSinceDateTime(dateTime: DateTime?): Int {
        var days = 0
        if (dateTime != null) {
            days = Days.daysBetween(dateTime, DateTime.now()).days
        }

        return days
    }

    /**
     * Use this method to validate if the customer selected DOB is less than or greater than 18 (legal age)
     * @param dateOfBirth
     * @param formatter
     * @return true if above legal age, false if not.
     */
    fun isUserDateOfBirthAboveLegalAge(dateOfBirth: String, formatter: DateTimeFormatter): Boolean {
        try {
            val date = formatter.parseDateTime(dateOfBirth)
            val now = DateTime()
            return now.minusYears(MINIMUM_AGE_OF_USER_YEARS).isAfter(date)
        } catch (e: IllegalArgumentException) {
            Log.e("DisplayDateTimeUtils", "Error validating entered date: " + e.message)
            return false
        }

    }

    fun getFullMonthAndDayOrdinal(context: Context, dateTime: DateTime): String {
        // returns "October 20th"
        return String.format("%s%s", monthDayFormatter.print(dateTime), getOrdinal(context, dateTime.dayOfMonth))
    }

    fun getDayOrdinal(context: Context, dateTime: DateTime): String {
        // Returns "20th"
        // Note that this only works for English. For other languages, provide ORDINAL_TH etc. in localized strings.xml as blank values
        // to fall through gracefully to not having ordinals, just numbers.
        val day = dateTime.dayOfMonth
        val dayString = day.toString()

        return String.format("%s%s", dayString, getOrdinal(context, day))
    }

    private fun getOrdinal(context: Context, day: Int): String {
        val dayString = day.toString()
        val lastDigitString = dayString.substring(dayString.length - 1)
        val lastDigitInt = Integer.valueOf(lastDigitString)
        val ordinal: String
        if (day == 11 || day == 12 || day == 13) {
            ordinal = context.getString(R.string.ORDINAL_TH)
        } else if (lastDigitInt == 1) {
            ordinal = context.getString(R.string.ORDINAL_ST)
        } else if (lastDigitInt == 2) {
            ordinal = context.getString(R.string.ORDINAL_ND)
        } else if (lastDigitInt == 3) {
            ordinal = context.getString(R.string.ORDINAL_RD)
        } else {
            ordinal = context.getString(R.string.ORDINAL_TH)
        }

        return ordinal
    }

    fun datesAreSameMonthAndYear(date1: DateTime?, date2: DateTime?): Boolean {
        return date1 != null && date2 != null && date1.year == date2.year && date1.monthOfYear == date2.monthOfYear
    }
}
