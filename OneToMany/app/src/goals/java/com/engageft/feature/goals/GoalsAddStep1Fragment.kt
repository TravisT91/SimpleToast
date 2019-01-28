package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentGoalsAddStep1Binding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.PayPlanUtils

/**
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsAddStep1Fragment : BaseEngagePageFragment() {

    private lateinit var addGoalViewModel: GoalsAddStep1ViewModel

    private val unsavedChangesDialogListener = object : InformationDialogFragment.InformationDialogFragmentListener {
        override fun onDialogFragmentPositiveButtonClicked() {
            findNavController().navigateUp()
        }
        override fun onDialogFragmentNegativeButtonClicked() {
            // Do nothing.
        }
        override fun onDialogCancelled() {
            // Do nothing.
        }
    }

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return if (addGoalViewModel.hasUnsavedChanges()) {
                fragmentDelegate.showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                true
            } else {
                false
            }
        }
    }

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

            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            nextButton.setOnClickListener {
                navigateToNextStep()
            }

            val frequencyOptionsList = PayPlanUtils.getRecurrenceTypeDisplayStringsForGoals(context!!)
            frequencyBottomSheet.dialogOptions = ArrayList(frequencyOptionsList)

            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())

            goalCompleteDateBottomSheet.dialogOptions = ArrayList(listOf(
                    getString(R.string.GOALS_ADD_COMPLETE_DATE_YES),
                    getString(R.string.GOALS_ADD_COMPLETE_DATE_NO)))

            startDateBottomSheet.minimumDate = DateTime.now()
            // todo test what backend allows 30-days?
            startDateBottomSheet.maximumDate = DateTime.now().plusDays(30)

            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode

            addGoalViewModel.nextButtonStateObservable.observe(viewLifecycleOwner, Observer {
                if (it == GoalsAddStep1ViewModel.ButtonState.SHOW) {
                    nextButton.visibility = View.VISIBLE
                } else {
                    nextButton.visibility = View.GONE
                }
                activity!!.invalidateOptionsMenu()
            })
        }

        return binding.root
    }

    private fun navigateToNextStep() {
        binding.root.findNavController().navigate(R.id.action_goalsAddStep1Fragment_to_goalsAddStep2Fragment,
                Bundle().apply {
                    putParcelable(GOAL_DATA_PARCELABLE_KEY, addGoalViewModel.getGoalInfoModel())
                })
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

    companion object {
        const val GOAL_DATA_PARCELABLE_KEY = "GOAL_DATA_PARCELABLE_KEY"
    }
}