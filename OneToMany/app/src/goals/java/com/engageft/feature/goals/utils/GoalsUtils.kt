package com.engageft.feature.goals.utils

import android.content.Context
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.PayPlanInfo
import utilGen1.DisplayDateTimeUtils
import utilGen1.PayPlanUtils
import utilGen1.StringUtils
import java.math.BigDecimal

// "$6 of $29"
fun GoalInfo.getGoalInfoProgressString(context: Context): String {
    return String.format(context.getString(R.string.GOALS_PROGRESS_FORMAT),
                StringUtils.formatCurrencyString(if (this.fundAmount != null) this.fundAmount.toPlainString() else "0"),
                StringUtils.formatCurrencyString(if (this.amount != null) this.amount.toPlainString() else "0"))

}

// if paused, "Paused", or like "$5/Daily", or if completed, total contributed like "$300"
fun GoalInfo.getGoalInfoContributionString(context: Context): String {
    return if (isAchieved) {
        StringUtils.formatCurrencyStringWithFractionDigits(amount.toString(), false)
    } else {
        // technically payPlan shouldn't ever be null
        payPlan.getPayPlanInfoContributionString(context)
    }

}

//todo rename to getPayPlanContributionOrPausedString
// if paused, "paused", or like "$5/day"
fun PayPlanInfo.getPayPlanInfoContributionString(context: Context): String {
    return if (isPaused) {
        context.getString(R.string.GOALS_PAUSED)
    } else {
        var planAmount = "0"
        amount?.let {
            planAmount = it.toPlainString()
        }
        String.format(context.getString(R.string.GOALS_RECURRENCE_FORMAT),
                StringUtils.formatCurrencyStringWithFractionDigits(planAmount, false),
                PayPlanUtils.getPayPlanFrequencyDisplayStringForRecurrenceType(context, recurrenceType))
    }
}

fun PayPlanInfo.getPayPlanInfoContribution(context: Context): String {
    var planAmount = "0"
    amount?.let {
        planAmount = it.toPlainString()
    }
    return String.format(context.getString(R.string.GOALS_RECURRENCE_FORMAT),
            StringUtils.formatCurrencyStringWithFractionDigits(planAmount, false),
            PayPlanUtils.getPayPlanFrequencyDisplayStringForRecurrenceType(context, recurrenceType))
}

// like "by Feb 29, 2020", or "Completed"
fun GoalInfo.getGoalInfoCompletionDateString(context: Context): String {
    val completeDate = BackendDateTimeUtils.getDateTimeForYMDString(estimatedCompleteDate)
    return if (isAchieved) {
        context.getString(R.string.GOALS_COMPLETE)
    } else {
        val completeDateString = DisplayDateTimeUtils.getMediumFormatted(completeDate)
        String.format(context.getString(R.string.GOALS_BY_DATE_FORMAT), completeDateString)
    }
}

fun sumNonCompleteGoalTotals(goalInfoList: List<GoalInfo>): Float {
    var sum = 0f
    for (goalInfo in goalInfoList) {
        if (!(goalInfo.isAchieved && goalInfo.fundAmount.compareTo(BigDecimal.ZERO) == 0)) {
            sum += goalInfo.amount.toFloat()
        }
    }

    return sum
}