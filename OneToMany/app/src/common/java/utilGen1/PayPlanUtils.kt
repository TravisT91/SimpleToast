package utilGen1

import android.content.Context
import android.text.TextUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils.PAY_PLAN_ANNUAL
import com.engageft.engagekit.utils.PayPlanInfoUtils.PAY_PLAN_DAY
import com.engageft.engagekit.utils.PayPlanInfoUtils.PAY_PLAN_MONTH
import com.engageft.engagekit.utils.PayPlanInfoUtils.PAY_PLAN_PAYCHECK
import com.engageft.engagekit.utils.PayPlanInfoUtils.PAY_PLAN_QUARTER
import com.engageft.engagekit.utils.PayPlanInfoUtils.PAY_PLAN_WEEK
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.BillInfo
import com.ob.ws.dom.utility.GoalInfo
import java.util.*

/**
 * PayPlanUtils
 *
 * Utility methods for setting goal/bill frequencies in a PayPlan
 *
 * Created by Kurt Mueller on 3/31/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
object PayPlanUtils {

    fun getPayPlanRecurrenceDisplayStringForRecurrenceType(context: Context, recurrenceType: String): String {
        var displayString = ""
        if (!TextUtils.isEmpty(recurrenceType)) {
            when (recurrenceType) {
                PAY_PLAN_ANNUAL -> displayString = context.getString(R.string.PAY_PLAN_ANNUAL)
                PAY_PLAN_QUARTER -> displayString = context.getString(R.string.PAY_PLAN_QUARTER)
                PAY_PLAN_MONTH -> displayString = context.getString(R.string.PAY_PLAN_MONTH)
                PAY_PLAN_WEEK -> displayString = context.getString(R.string.PAY_PLAN_WEEK)
                PAY_PLAN_DAY -> displayString = context.getString(R.string.PAY_PLAN_DAILY)
                PAY_PLAN_PAYCHECK -> displayString = context.getString(R.string.PAY_PLAN_PAYCHECK)
            }
        }

        return displayString
    }

    fun getPayPlanFrequencyDisplayStringForRecurrenceType(context: Context, recurrenceType: String): String {
        var displayString = ""
        if (!TextUtils.isEmpty(recurrenceType)) {
            when (recurrenceType) {
                PAY_PLAN_ANNUAL -> displayString = context.getString(R.string.PAY_PER_YEAR)
                PAY_PLAN_QUARTER -> displayString = context.getString(R.string.PAY_PER_QUARTER)
                PAY_PLAN_MONTH -> displayString = context.getString(R.string.PAY_PER_MONTH)
                PAY_PLAN_WEEK -> displayString = context.getString(R.string.PAY_PER_WEEK)
                PAY_PLAN_DAY -> displayString = context.getString(R.string.PAY_PER_DAY)
                PAY_PLAN_PAYCHECK -> displayString = context.getString(R.string.PAY_PER_PAYCHECK)
            }
        }

        return displayString
    }

    fun getPayPlanRecurrenceTypeFromDisplayString(context: Context, displayString: String): String? {
        var recurrenceType: String? = null
        if (displayString == context.getString(R.string.PAY_PLAN_ANNUAL)) {
            recurrenceType = PAY_PLAN_ANNUAL
        } else if (displayString == context.getString(R.string.PAY_PLAN_QUARTER)) {
            recurrenceType = PAY_PLAN_QUARTER
        } else if (displayString == context.getString(R.string.PAY_PLAN_MONTH)) {
            recurrenceType = PAY_PLAN_MONTH
        } else if (displayString == context.getString(R.string.PAY_PLAN_WEEK)) {
            recurrenceType = PAY_PLAN_WEEK
        } else if (displayString == context.getString(R.string.PAY_PLAN_DAILY)) {
            recurrenceType = PAY_PLAN_DAY
        } else if (displayString == context.getString(R.string.PAY_PLAN_PAYCHECK)) {
            recurrenceType = PAY_PLAN_PAYCHECK
        }

        return recurrenceType
    }

    fun getPayPlanRecurrenceDisplayStringForGoalInfo(context: Context, goalInfo: GoalInfo): String {
        var displayString = ""
        if (goalInfo.payPlan != null && !TextUtils.isEmpty(goalInfo.payPlan.recurrenceType)) {
            displayString = getPayPlanRecurrenceDisplayStringForRecurrenceType(context, goalInfo.payPlan.recurrenceType)
        }

        return displayString
    }

    fun getPayPlanFrequencyDisplayStringForGoalInfo(context: Context, goalInfo: GoalInfo): String {
        var displayString = ""
        if (goalInfo.payPlan != null && !TextUtils.isEmpty(goalInfo.payPlan.recurrenceType)) {
            displayString = getPayPlanFrequencyDisplayStringForRecurrenceType(context, goalInfo.payPlan.recurrenceType)
        }

        return displayString
    }

    fun getPayPlanRecurrenceDisplayStringForBillInfo(context: Context, billInfo: BillInfo): String {
        var displayString = ""
        if (billInfo.payPlan != null && !TextUtils.isEmpty(billInfo.payPlan.recurrenceType)) {
            displayString = getPayPlanRecurrenceDisplayStringForRecurrenceType(context, billInfo.payPlan.recurrenceType)
        }

        return displayString
    }


    fun getRecurrenceTypeDisplayStringsForGoals(context: Context): List<String> {
        val displayStrings = ArrayList<String>()
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_DAY))
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_WEEK))
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_MONTH))
        // CARE-844 specified removing quarterly and yearly goal frequencies from the list of available frequencies, for all apps.

        // TODO(jhutchins): Refactor PayPlanUtils SHOW-388
//        if (EngageService.getInstance().engageConfig.getEnablePaycheck()) {
//            displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_PAYCHECK))
//        }

        return displayStrings
    }

    fun getRecurrenceTypeDisplayStringsForBills(context: Context): List<String> {
        val displayStrings = ArrayList<String>()
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_MONTH))
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_WEEK))
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_QUARTER))
        displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_ANNUAL))

        // TODO(jhutchins): Refactor PayPlanUtils SHOW-388
//        if (EngageService.getInstance().engageConfig.getEnablePaycheck()) {
//            displayStrings.add(getPayPlanRecurrenceDisplayStringForRecurrenceType(context, PAY_PLAN_PAYCHECK))
//        }

        return displayStrings
    }
}
