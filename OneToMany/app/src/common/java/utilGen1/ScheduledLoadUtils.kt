package utilGen1

import android.content.Context
import android.text.TextUtils
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.R
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.ScheduledLoadsResponse
import com.ob.ws.dom.utility.ScheduledLoadInfo
import java.math.BigDecimal
import java.util.Locale

/**
 * ScheduledLoadUtils
 *
 * Provides convenience methods for displaying scheduled load Info
 *
 * Created by Kurt Mueller on 4/15/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
object ScheduledLoadUtils {

    fun getIncomeScheduledLoads(scheduledLoadsResponse: ScheduledLoadsResponse): MutableList<ScheduledLoad> {
        val incomeLoads = mutableListOf<ScheduledLoadInfo>()

        if (scheduledLoadsResponse.scheduledLoads != null) {
            for (scheduledLoad in scheduledLoadsResponse.scheduledLoads) {
                if (scheduledLoad.isExternal) {
                    incomeLoads.add(scheduledLoad)
                }
            }
        }

        return if (incomeLoads.size > 0) {
            mergeTwiceMonthlyLoads(incomeLoads)
        } else {
            ArrayList()
        }
    }

    fun getIncomeScheduledLoadWithId(scheduledLoadsResponse: ScheduledLoadsResponse, id: Long): ScheduledLoad? {
        var result: ScheduledLoad? = null

        val scheduledLoadList = getIncomeScheduledLoads(scheduledLoadsResponse)
        for (scheduledLoad in scheduledLoadList) {
            if (scheduledLoad.scheduledLoadId == id) {
                result = scheduledLoad
                break
            }
        }

        return result
    }

    fun getScheduledLoads(scheduledLoadsResponse: ScheduledLoadsResponse?): List<ScheduledLoad> {
        val scheduledLoads = ArrayList<ScheduledLoadInfo>()

        if (scheduledLoadsResponse != null && scheduledLoadsResponse.scheduledLoads != null) {
            for (scheduledLoad in scheduledLoadsResponse.scheduledLoads) {
                if (!scheduledLoad.isExternal) {
                    scheduledLoads.add(scheduledLoad)
                }
            }
        }

        return if (scheduledLoads.size > 0) {
            mergeTwiceMonthlyLoads(scheduledLoads)
        } else {
            ArrayList()
        }
    }

    private fun mergeTwiceMonthlyLoads(scheduledLoadInfoList: MutableList<ScheduledLoadInfo>): MutableList<ScheduledLoad> {
        val uniqueLoadsList = ArrayList<ScheduledLoad>()
        if (scheduledLoadInfoList.isNotEmpty()) {
            if (scheduledLoadInfoList.size == 1) {
                uniqueLoadsList.add(ScheduledLoad(scheduledLoadInfoList[0]))
            } else {
                // sort by amount
                scheduledLoadInfoList.sortBy { it.amount }

                var firstLoad: ScheduledLoadInfo
                var firstLoadCustom: ScheduledLoad
                var nextLoad: ScheduledLoadInfo
                var firstLoadAmount: Float
                var nextLoadAmount: Float
                var i = 0
                while (i < scheduledLoadInfoList.size) {
                    // see if once a month is repeated
                    firstLoad = scheduledLoadInfoList[i]
                    firstLoadCustom = ScheduledLoad(firstLoad)

                    if (i < scheduledLoadInfoList.size - 1) {
                        nextLoad = scheduledLoadInfoList[i + 1]
                        if (ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY == firstLoad.type.name && ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY == nextLoad.type.name) {
                            firstLoadAmount = StringUtils.getFloatFromString(firstLoad.amount)
                            nextLoadAmount = StringUtils.getFloatFromString(nextLoad.amount)
                            if (firstLoadAmount == nextLoadAmount) {
                                // they are the same so assume twice monthly
                                firstLoadCustom.isHasDuplicate = true
                                firstLoadCustom.scheduledLoadIdDup = nextLoad.scheduledLoadId
                                firstLoadCustom.nextRunDateDup = nextLoad.nextRunDate
                                firstLoadCustom.isoNextRunDateDup = nextLoad.isoNextRunDate
                                firstLoadCustom.scheduleDate2 = nextLoad.nextRunDate
                                firstLoadCustom.typeString = ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY

                                // skip nextLoad (duplicate)
                                ++i
                            }
                        }
                    }

                    uniqueLoadsList.add(firstLoadCustom)
                    ++i
                }
            }
        }

        return uniqueLoadsList
    }

    fun getFrequencyDisplayStringForType(context: Context, type: String): String {
        return when (type) {
            ScheduledLoad.SCHED_LOAD_TYPE_ONCE -> context.getString(R.string.TRANSFER_ONCE_TEXT)
            ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> context.getString(R.string.TRANSFER_MONTHLY_TEXT)
            ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY -> context.getString(R.string.TRANSFER_TWICE_MONTHLY_TEXT)
            ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> context.getString(R.string.TRANSFER_WEEKLY_TEXT)
            ScheduledLoad.SCHED_LOAD_TYPE_ALT_WEEKLY -> context.getString(R.string.TRANSFER_ALT_WEEKLY_TEXT)
            else -> context.getString(R.string.TRANSFER_ONCE_TEXT)
        }
    }

    fun getFrequencyTypeStringForDisplayString(context: Context, displayString: String): String {
        return when (displayString) {
            context.getString(R.string.TRANSFER_ONCE_TEXT) -> ScheduledLoad.SCHED_LOAD_TYPE_ONCE
            context.getString(R.string.TRANSFER_MONTHLY_TEXT) -> ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY
            context.getString(R.string.TRANSFER_TWICE_MONTHLY_TEXT) -> ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY
            context.getString(R.string.TRANSFER_WEEKLY_TEXT) -> ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY
            else -> ScheduledLoad.SCHED_LOAD_TYPE_ALT_WEEKLY
        }
    }

    fun getFrequencyDisplayStringsForIncome(context: Context): List<String> {
        val displayStrings = ArrayList<String>()
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY))
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY))
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY))
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_ALT_WEEKLY))

        return displayStrings
    }

    fun getFrequencyDisplayStringsForTransfer(context: Context): List<String> {
        val displayStrings = ArrayList<String>()
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_ONCE))
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY))
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY))
        displayStrings.add(getFrequencyDisplayStringForType(context, ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY))

        return displayStrings
    }

    fun getProcessingMethodDisplayStringFromMethod(context: Context, method: String): String {
        return when (method) {
            ScheduledLoad.PLANNED_LOAD_METHOD_DD -> context.getString(R.string.DIRECT_DEPOSIT)
            ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER -> context.getString(R.string.BANK_TRANSFER)
            ScheduledLoad.PLANNED_LOAD_METHOD_CASH -> context.getString(R.string.CASH)
            else -> context.getString(R.string.CASH)
        }
    }

    fun getProcessingMethodFromDisplayString(context: Context, displayString: String): String {
        return if (displayString == context.getString(R.string.DIRECT_DEPOSIT)) {
            ScheduledLoad.PLANNED_LOAD_METHOD_DD
        } else if (displayString == context.getString(R.string.BANK_TRANSFER)) {
            ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER
        } else {
            ScheduledLoad.PLANNED_LOAD_METHOD_CASH
        }
    }

    fun getProcessingMethodDisplayStrings(context: Context): List<String> {
        val displayStrings = ArrayList<String>()
        displayStrings.add(getProcessingMethodDisplayStringFromMethod(context, ScheduledLoad.PLANNED_LOAD_METHOD_DD))
        displayStrings.add(getProcessingMethodDisplayStringFromMethod(context, ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER))
        displayStrings.add(getProcessingMethodDisplayStringFromMethod(context, ScheduledLoad.PLANNED_LOAD_METHOD_CASH))

        return displayStrings
    }


    fun createScheduledLoadFromTransactionDetails(transactionAmount: String, transactionDate: String, scheduledLoadType: String): ScheduledLoad {
        val scheduledLoad = ScheduledLoad()

        // from iOS ScheduledLoad.m, initWithTransaction:
        scheduledLoad.amount = transactionAmount
        scheduledLoad.typeString = ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY // iOS note: default to monthly, should be smarter if multiple loads of same amount
        scheduledLoad.scheduleDate = transactionDate
        scheduledLoad.scheduledLoadType = scheduledLoadType

        return scheduledLoad
    }

    fun getTransferDetailSimpleText(context: Context, scheduledLoad: ScheduledLoad): String {
        val nextRunDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(scheduledLoad.isoNextRunDate)

        if (ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY == scheduledLoad.typeString) {
            if (scheduledLoad.isHasDuplicate) {
                // show as twice a month
                val nextRunDateDup = BackendDateTimeUtils.parseDateTimeFromIso8601String(scheduledLoad.isoNextRunDateDup)
                return String.format(context.getString(R.string.TRANSFER_TWICE_MONTHLY_SIMPLE_LOAD_DESCRIPTION),
                        DisplayDateTimeUtils.getDayOrdinal(context, nextRunDate!!),
                        DisplayDateTimeUtils.getDayOrdinal(context, nextRunDateDup!!))
            } else {
                return String.format(context.getString(R.string.TRANSFER_MONTHLY_SIMPLE_LOAD_DESCRIPTION), DisplayDateTimeUtils.getDayOrdinal(context, nextRunDate!!))
            }
        } else if (ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY == scheduledLoad.typeString) {
            val scheduledDate = BackendDateTimeUtils.getDateTimeForMDYString(scheduledLoad.scheduleDate)
            val scheduledDate2 = BackendDateTimeUtils.getDateTimeForMDYString(scheduledLoad.scheduleDate2)
            return String.format(context.getString(R.string.TRANSFER_TWICE_MONTHLY_SIMPLE_LOAD_DESCRIPTION),
                    DisplayDateTimeUtils.getDayOrdinal(context, scheduledDate!!),
                    DisplayDateTimeUtils.getDayOrdinal(context, scheduledDate2!!))
        } else if (ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY == scheduledLoad.typeString) {
            return String.format(context.getString(R.string.TRANSFER_WEEKLY_SIMPLE_LOAD_DESCRIPTION), nextRunDate!!.dayOfWeek().getAsText(Locale.getDefault()))
        }

        return ""
    }

    fun getAccountDetailText(context: Context, scheduledLoad: ScheduledLoad, loginResponse: LoginResponse): String {
        var accountDetailText = ""
        if (!TextUtils.isEmpty(scheduledLoad.achAccountId)) {
            // It's an ACH source
            try {
                val achAccountInfo = LoginResponseUtils.getAchAccountInfoById(loginResponse, java.lang.Long.valueOf(scheduledLoad.achAccountId))
                accountDetailText = AchAccountInfoUtils.accountDescriptionForDisplay(context, achAccountInfo)
                accountDetailText = if (BigDecimal(scheduledLoad.amount) < BigDecimal.ZERO) {
                    // it's an outgoing transaction: "to ..."
                    String.format(context.getString(R.string.PAY_TO_FMT), accountDetailText)
                } else {
                    // it's an incoming transactions: "from ..."
                    String.format(context.getString(R.string.PAY_FROM_FMT), accountDetailText)
                }
            } catch (e: NumberFormatException) {
                // intentionally left blank
            }

        } else if (!TextUtils.isEmpty(scheduledLoad.ccAccountId)) {
            // It's a debit source
            try {
                val fundDebitSource = LoginResponseUtils.getFundDebitSourceById(loginResponse, java.lang.Long.valueOf(scheduledLoad.ccAccountId))
                accountDetailText = String.format(context.getString(R.string.TRANSFER_ACCOUNT_DESCRIPTION_FORMAT), context.getString(R.string.FUND_DEBIT_CARD), fundDebitSource!!.lastFour)
            } catch (e: NumberFormatException) {
                // intentionally left blank
            }

        }

        return accountDetailText
    }
}
