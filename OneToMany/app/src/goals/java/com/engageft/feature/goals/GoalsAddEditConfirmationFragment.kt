package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import org.joda.time.DateTime
import java.math.BigDecimal

class GoalsAddEditConfirmationFragment: BaseEngagePageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {

        const val GOAL_NAME = "GOAL_NAME"
        const val GOAL_AMOUNT = "GOAL_AMOUNT"
        const val RECURRENCE_TYPE = "RECURRENCE_TYPE"
        const val START_DATE = "START_DATE"
        const val DAY_OF_WEEK = "DAY_OF_WEEK"
        private const val GOAL_COMPLETE_DATE = "GOAL_COMPLETE_DATE"
        private const val GOAL_AMOUNT_PER_FREQUENCY = "GOAL_AMOUNT_PER_FREQUENCY"

        fun createBundleWithGoalCompleteDate(goalName: String, goalAmount: BigDecimal, recurrenceType: String,
                         startDate: DateTime, dayOfWeek: Int, goalCompleteDate: DateTime): Bundle {
            return Bundle().apply {
                putString(GOAL_NAME, goalName)
                putSerializable(GOAL_AMOUNT, goalAmount)
                putString(RECURRENCE_TYPE, recurrenceType)
                putSerializable(START_DATE, startDate)
                putInt(DAY_OF_WEEK, dayOfWeek)
                putSerializable(GOAL_COMPLETE_DATE, goalCompleteDate)
            }
        }

        fun createBundleWithAmountFrequency(goalName: String, goalAmount: BigDecimal, recurrenceType: String,
                         startDate: DateTime, dayOfWeek: Int, frequencyAmount: BigDecimal): Bundle {
            return Bundle().apply {
                putString(GOAL_NAME, goalName)
                putSerializable(GOAL_AMOUNT, goalAmount)
                putString(RECURRENCE_TYPE, recurrenceType)
                putSerializable(START_DATE, startDate)
                putInt(DAY_OF_WEEK, dayOfWeek)
                putSerializable(GOAL_AMOUNT_PER_FREQUENCY, frequencyAmount)
            }
        }
    }
}