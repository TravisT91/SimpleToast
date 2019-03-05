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
import com.engageft.feature.goals.utils.GoalConstants.GOAL_DATA_PARCELABLE_KEY
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID_KEY
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentGoalEditBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.PayPlanUtils
import java.lang.IllegalArgumentException

class GoalEditFragment: BaseEngagePageFragment() {

    private lateinit var viewModelGoalEdit: GoalEditViewModel
    private lateinit var binding: FragmentGoalEditBinding

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
            return if (viewModelGoalEdit.hasUnsavedChanges()) {
                val dialog = infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener)
                dialog.applyPaletteStyles(activity!!)
                fragmentDelegate.showDialog(dialog)
                true
            } else {
                false
            }
        }
    }

    override fun createViewModel(): BaseViewModel? {
        val goalId = arguments!!.getLong(GOAL_ID_KEY, GOAL_ID)
        if (goalId == GOAL_ID) {
            throw IllegalArgumentException("Goal Id is not valid")
        } else {
            viewModelGoalEdit = ViewModelProviders.of(this, GoalEditViewModelFactory(goalId)).get(GoalEditViewModel::class.java)
        }
        return viewModelGoalEdit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalEditBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelGoalEdit
            palette = Palette

            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            toolbarController.setToolbarTitle(getString(R.string.GOAL_DETAIL_EDIT))

            val frequencyOptionsList = PayPlanUtils.getRecurrenceTypeDisplayStringsForGoals(context!!)
            frequencyBottomSheet.dialogOptions = ArrayList(frequencyOptionsList)

            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())

            val minimumDate = DateTime.now().plusDays(1)
            nextRunDateDatePicker.minimumDate = minimumDate
            nextRunDateDatePicker.maximumDate = DateTime.now().plusMonths(1)
            nextRunDateDatePicker.setDateFormat(getString(R.string.format_datetime_short))

            goalCompleteDateDatePicker.minimumDate = minimumDate
            goalCompleteDateDatePicker.setDateFormat(getString(R.string.format_datetime_short))

            goalAmountInputWithLabel.currencyCode = EngageAppConfig.currencyCode
            frequencyAmountInputWithLabel.currencyCode = EngageAppConfig.currencyCode

            nextButton.setOnClickListener {
                navigateToNextStep()
            }

            viewModelGoalEdit.apply {

                nextButtonStateObservable.observe(viewLifecycleOwner, Observer {
                    if (it == GoalEditViewModel.ButtonState.SHOW) {
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_goal_next, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.menu_item_next)
        menuItem.isVisible = viewModelGoalEdit.nextButtonStateObservable.value == GoalEditViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_item_next -> run {
                navigateToNextStep()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToNextStep() {
        binding.root.findNavController().navigate(R.id.action_goalEditFragment_to_goalEditConfirmationFragment,
                Bundle().apply {
                    putParcelable(GOAL_DATA_PARCELABLE_KEY, viewModelGoalEdit.goalInfoModel)
                })
    }
}