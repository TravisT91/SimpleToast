package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.feature.goals.utils.GoalConstants.GOAL_DATA_PARCELABLE_KEY
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentGoalsAddStep2Binding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import org.joda.time.DateTime
import utilGen1.PayPlanUtils

/**
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsAddStep2Fragment : BaseEngagePageFragment() {

    private lateinit var addGoalViewModel: GoalsAddStep2ViewModel

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

            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            arguments!!.get(GOAL_DATA_PARCELABLE_KEY)?.let {
                addGoalViewModel.goalInfoModel = it as GoalInfoModel
            } ?: kotlin.run {
                throw IllegalArgumentException("Must pass GoalInfoModel data")
            }

            nextButton.setOnClickListener {
                navigateToConfirmation()
            }

            addGoalViewModel.apply {
                if (goalInfoModel.hasCompleteDate) {
                    goalCompleteDatePicker.minimumDate = DateTime.now().plusDays(1)
                    headerTextView.text = getString(R.string.GOALS_ADD_COMPLETE_DATE_HEADER)
                    subHeaderTextView.text = getString(R.string.GOALS_ADD_COMPLETE_DATE_SUB_HEADER)
                    frequencyAmountInputWithLabel.visibility = View.GONE
                } else {
                    headerTextView.text = getString(R.string.GOALS_ADD_FREQUENCY_AMOUNT_HEADER)

                    val frequency = PayPlanUtils.getPayPlanRecurrenceDisplayStringForRecurrenceType(context!!,
                            goalInfoModel.recurrenceType.toString()).toLowerCase()
                    val subHeaderString = String.format(
                            getString(R.string.GOALS_ADD_FREQUENCY_AMOUNT_SUB_HEADER_FORMAT),
                            frequency)

                    subHeaderTextView.text = subHeaderString.applyTypefaceToSubstring(
                            ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            frequency)

                    frequencyAmountInputWithLabel.currencyCode = EngageAppConfig.currencyCode
                    goalCompleteDatePicker.visibility = View.GONE
                }

                nextButtonStateObservable.observe(viewLifecycleOwner, Observer {
                    if (it == GoalsAddStep2ViewModel.ButtonState.SHOW) {
                        nextButton.visibility = View.VISIBLE
                    } else {
                        nextButton.visibility = View.GONE
                    }
                    activity!!.invalidateOptionsMenu()
                })
            }
        }

        return binding.root
    }

    private fun navigateToConfirmation() {
        binding.root.findNavController().navigate(R.id.action_goalsAddStep2Fragment_to_goalsAddEditConfirmationFragment,
                Bundle().apply {
                    putParcelable(GOAL_DATA_PARCELABLE_KEY, addGoalViewModel.getGoalData())
                })

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_goal_next, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.menu_item_next)
        menuItem.isVisible = addGoalViewModel.nextButtonStateObservable.value == GoalsAddStep2ViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_item_next -> run {
                navigateToConfirmation()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}