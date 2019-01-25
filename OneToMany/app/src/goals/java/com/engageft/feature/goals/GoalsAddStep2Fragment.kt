package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.DAY_OF_WEEK
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.GOAL_AMOUNT
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.GOAL_NAME
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.RECURRENCE_TYPE
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.START_DATE
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.createBundleWithAmountFrequency
import com.engageft.feature.goals.GoalsAddEditConfirmationFragment.Companion.createBundleWithGoalCompleteDate
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalsAddStep2Binding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import org.joda.time.DateTime
import java.math.BigDecimal

/**
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsAddStep2Fragment : BaseEngagePageFragment() {

    private lateinit var addGoalViewModel: GoalsAddStep2ViewModel

    override fun createViewModel(): BaseViewModel? {
        addGoalViewModel = ViewModelProviders.of(this).get(GoalsAddStep2ViewModel::class.java)
        return addGoalViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var binding: FragmentGoalsAddStep2Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalsAddStep2Binding.inflate(inflater, container, false).apply {
            viewModel = addGoalViewModel
            palette = Palette

            arguments?.let { bundle ->
                bundle.apply {
                    addGoalViewModel.apply {
                        goalName = getString(GOAL_NAME, "")
                        goalAmount = getSerializable(GOAL_AMOUNT) as BigDecimal
                        recurrenceType = getString(RECURRENCE_TYPE, "")
                        startDate = getSerializable(START_DATE) as DateTime
                        dayOfWeek = getInt(DAY_OF_WEEK, -1)
                        hasGoalDateInMind = getBoolean(HAS_GOAL_DATE_IN_MIND, false)
                    }
                }
            }

            nextButton.setOnClickListener {
                navigateToConfirmation()
            }

//            startDatePicker.minimumDate = DateTime.now()
        }

        return binding.root
    }

    private fun navigateToConfirmation() {
        var bundle: Bundle
        addGoalViewModel.apply {
            if (hasGoalDateInMind) {
                bundle = createBundleWithGoalCompleteDate(
                        goalName = goalName,
                        goalAmount = goalAmount,
                        recurrenceType = recurrenceType,
                        dayOfWeek = dayOfWeek,
                        startDate = startDate,
                        goalCompleteDate = DateTime(goalSaveByDate)
                )
            } else {
                bundle = createBundleWithAmountFrequency(
                        goalName = goalName,
                        goalAmount = goalAmount,
                        recurrenceType = recurrenceType,
                        dayOfWeek = dayOfWeek,
                        startDate = startDate,
                        frequencyAmount = frequencyAmountBigDecimal
                )
            }
        }
        // navigate action
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_next, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.next)
        menuItem.isVisible = addGoalViewModel.nextButtonStateObservable.value == GoalsAddStep1ViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.next -> run {
                navigateToConfirmation()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val HAS_GOAL_DATE_IN_MIND = "HAS_GOAL_DATE_IN_MIND"

        fun createBundle(goalName: String, goalAmount: BigDecimal, recurrenceType: String,
                         startDate: DateTime, dayOfWeek: Int, goalDateInMind: Boolean): Bundle {
            return Bundle().apply {
                putString(GOAL_NAME, goalName)
                putSerializable(GOAL_AMOUNT, goalAmount)
                putString(RECURRENCE_TYPE, recurrenceType)
                putSerializable(START_DATE, startDate)
                putInt(DAY_OF_WEEK, dayOfWeek)
                putBoolean(HAS_GOAL_DATE_IN_MIND, goalDateInMind)
            }
        }
    }
}