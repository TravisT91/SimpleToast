package utilGen1

import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.ob.ws.dom.utility.GoalInfo
import com.ob.ws.dom.utility.GoalTargetInfo
import com.ob.ws.dom.utility.PayPlanInfo
import org.joda.time.DateTime
import java.math.BigDecimal
import java.util.*

/**
 * TODO: CLASS NAME
 *
 * TODO: CLASS DESCRIPTION
 *
 * Created by Kurt Mueller on 5/26/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
object GoalInfoUtils {

    fun isCompleted(goalInfo: GoalInfo?): Boolean {
        return goalInfo != null && (goalInfo.isAchieved || goalInfo.payPlan == null)
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
}
