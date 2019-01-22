package com.engageft.fis.pscu.feature.utils

import android.content.Context
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.GoalTargetInfo
import com.ob.ws.dom.utility.PayPlanInfo
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.PayPlanUtils
import utilGen1.StringUtils
import java.math.BigDecimal
import java.util.ArrayList

// "$6 of $29"
fun GoalInfo.getGoalInfoProgressString(context: Context): String {
    return String.format(context.getString(R.string.GOALS_PROGRESS_FORMAT),
                StringUtils.formatCurrencyString(if (this.fundAmount != null) this.fundAmount.toPlainString() else "0"),
                StringUtils.formatCurrencyString(if (this.amount != null) this.amount.toPlainString() else "0"))

}

fun GoalInfo.isCompleted(): Boolean {
    return this.isAchieved || this.payPlan == null
}

// if paused, "Paused", or like "$5/Daily", or if completed, total contributed like "$300"
fun GoalInfo.getGoalInfoContributionString(context: Context): String? {
    var result: String? = null

    if (isCompleted()) {
        result = StringUtils.formatCurrencyStringWithFractionDigits(amount.toString(), false)
    } else {
        payPlan?.let {
            result = it.getPayPlanInfoContributionString(context)
        }
    }

    return result
}

// if paused, "paused", or like "$5/day"
fun PayPlanInfo.getPayPlanInfoContributionString(context: Context): String {
    return if (this.isPaused) {
        context.getString(R.string.GOALS_PAUSED)
    } else {
        var planAmount = "0"
        this.amount?.let {
            planAmount = it.toPlainString()
        }
        String.format(context.getString(R.string.GOALS_RECURRENCE_FORMAT),
                StringUtils.formatCurrencyStringWithFractionDigits(planAmount, false),
                PayPlanUtils.getPayPlanFrequencyDisplayStringForRecurrenceType(context, this.recurrenceType))
    }

}

// like "by Feb 29, 2020", or "Completed"
fun GoalInfo.getGoalInfoCompletionDateString(context: Context): String? {
    var result: String? = null

    val completeDate = BackendDateTimeUtils.getDateTimeForYMDString(this.estimatedCompleteDate)
    completeDate?.let { dateComplete ->
        val completeDateString = DisplayDateTimeUtils.getMediumFormatted(completeDate)
        result = if (this.isCompleted()) {
            context.getString(R.string.GOALS_COMPLETE)
        } else {
            String.format(context.getString(R.string.GOALS_BY_DATE_FORMAT), completeDateString)
        }
    }

    return result
}

fun createGoalInfo(goalName: String, goalAmount: String, recurrenceType: String, startDate: DateTime, dayOfWeek: Int, purseId: Long): GoalInfo {
    val goalInfo = GoalInfo()
    goalInfo.isAchieved = false
    goalInfo.isActive = true
    goalInfo.amount = BigDecimal.valueOf(StringUtils.getFloatFromString(goalAmount).toDouble())
    goalInfo.fundAmount = BigDecimal.valueOf(0.0)
    goalInfo.name = StringUtils.removeRedundantWhitespace(goalName)
    goalInfo.purseId = purseId

    val goalTargetInfo = GoalTargetInfo()
    goalTargetInfo.name = goalInfo.name
    goalTargetInfo.amount = goalInfo.amount

    val goalTargetInfoList = ArrayList<GoalTargetInfo>()
    goalTargetInfoList.add(goalTargetInfo)
    goalInfo.goalTargets = goalTargetInfoList

    val payPlanInfo = PayPlanInfo()
    payPlanInfo.recurrenceType = recurrenceType

    when (payPlanInfo.recurrenceType) {
        PayPlanInfoUtils.PAY_PLAN_ANNUAL, PayPlanInfoUtils.PAY_PLAN_QUARTER -> {
            payPlanInfo.dayOfMonth = startDate.dayOfMonth
            payPlanInfo.monthOfYear = startDate.monthOfYear
            payPlanInfo.dayOfWeek = null
        }
        PayPlanInfoUtils.PAY_PLAN_MONTH -> {
            payPlanInfo.dayOfMonth = startDate.dayOfMonth
            payPlanInfo.monthOfYear = null
            payPlanInfo.dayOfWeek = null
        }
        PayPlanInfoUtils.PAY_PLAN_WEEK -> {
            payPlanInfo.dayOfMonth = null
            payPlanInfo.monthOfYear = null
            payPlanInfo.dayOfWeek = dayOfWeek
        }
        PayPlanInfoUtils.PAY_PLAN_DAY, PayPlanInfoUtils.PAY_PLAN_PAYCHECK -> {
            payPlanInfo.dayOfMonth = null
            payPlanInfo.monthOfYear = null
            payPlanInfo.dayOfWeek = null
        }
    }

    goalInfo.payPlan = payPlanInfo

    return goalInfo
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