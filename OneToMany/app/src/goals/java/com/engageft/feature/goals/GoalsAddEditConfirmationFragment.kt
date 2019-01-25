package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentGoalsAddEditConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import org.joda.time.DateTime
import java.math.BigDecimal
import java.sql.Date

class GoalsAddEditConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var confirmationViewModel: GoalsAddEditConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        confirmationViewModel = ViewModelProviders.of(this).get(GoalsAddEditConfirmationViewModel::class.java)
        return confirmationViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentGoalsAddEditConfirmationBinding.inflate(inflater, container, false).apply {
            viewModel = confirmationViewModel
            palette = Palette

            arguments?.let { bundle ->
                bundle.apply {
                    confirmationViewModel.apply {
                        goalName = getString(GOAL_NAME, "")
                        goalAmount = getSerializable(GOAL_AMOUNT) as BigDecimal
                        recurrenceType = getString(RECURRENCE_TYPE, "")
                        startDate = getSerializable(START_DATE) as DateTime
                        dayOfWeek = getInt(DAY_OF_WEEK, -1)
                        hasGoalDateInMind = getBoolean(HAS_GOAL_DATE_IN_MIND, false)
                        if (hasGoalDateInMind) {
                            goalCompleteDate = getSerializable(GOAL_COMPLETE_DATE) as DateTime
                        } else {
                            goalFrequencyAmount = getSerializable(GOAL_AMOUNT_PER_FREQUENCY) as BigDecimal
                        }
                    }
                }

                if (confirmationViewModel.hasGoalDateInMind) {

                }
            }
        }

        return binding.root
    }

    companion object {

        const val GOAL_NAME = "GOAL_NAME"
        const val GOAL_AMOUNT = "GOAL_AMOUNT"
        const val RECURRENCE_TYPE = "RECURRENCE_TYPE"
        const val START_DATE = "START_DATE"
        const val DAY_OF_WEEK = "DAY_OF_WEEK"
        const val HAS_GOAL_DATE_IN_MIND = "HAS_GOAL_DATE_IN_MIND"
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
                putBoolean(HAS_GOAL_DATE_IN_MIND, true)
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
                putBoolean(HAS_GOAL_DATE_IN_MIND, false)
            }
        }
    }
}