package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalsAddStep1Binding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.PayPlanUtils
import java.math.BigDecimal

/**
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsAddStep1Fragment : BaseEngagePageFragment() {

    private lateinit var addGoalViewModel: GoalsAddStep1ViewModel

    override fun createViewModel(): BaseViewModel? {
        addGoalViewModel = ViewModelProviders.of(this).get(GoalsAddStep1ViewModel::class.java)
        return addGoalViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var binding: FragmentGoalsAddStep1Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalsAddStep1Binding.inflate(inflater, container, false).apply {
            viewModel = addGoalViewModel
            palette = Palette

            nextButton.setOnClickListener {
                navigateToNextStep()
            }

            val frequencyOptionsList = PayPlanUtils.getRecurrenceTypeDisplayStringsForGoals(context!!)
            frequencyBottomSheet.dialogOptions = ArrayList(frequencyOptionsList)

            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())
            startDateBottomSheet.minimumDate = DateTime.now()

            goalDateInMindBottomSheet.dialogOptions = ArrayList(listOf("Yes", "No"))
        }

        return binding.root
    }

    private fun navigateToNextStep() {
        binding.root.findNavController().navigate(R.id.action_goalsAddStep1Fragment_to_goalsAddStep2Fragment,
                GoalsAddStep2Fragment.createBundle(
                        goalName = "testing HEH",
                        goalAmount = BigDecimal(20),
                        recurrenceType = PayPlanInfoUtils.PAY_PLAN_WEEK,
                        startDate = DateTime.now().plusDays(1),
                        dayOfWeek = 4,
                        goalDateInMind = addGoalViewModel.hasGoalDateInMind))
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
                navigateToNextStep()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}