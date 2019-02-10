package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyRelativeSizeToSubstring
import com.engageft.feature.goals.utils.GoalConstants.FREQUENCY_SUBSTRING_RELATIVE_HEIGHT
import com.engageft.feature.goals.utils.GoalConstants.GOAL_DATA_PARCELABLE_KEY
import com.engageft.feature.goals.utils.PayPlanType
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalEditConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import utilGen1.DisplayDateTimeUtils
import utilGen1.PayPlanUtils
import utilGen1.StringUtils

class GoalEditConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var confirmationViewModel: GoalAddEditConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        confirmationViewModel = ViewModelProviders.of(this).get(GoalAddEditConfirmationViewModel::class.java)
        return confirmationViewModel
    }

    private lateinit var binding: FragmentGoalEditConfirmationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalEditConfirmationBinding.inflate(inflater, container, false).apply {
            viewModel = confirmationViewModel
            palette = Palette

            arguments!!.apply {
                val goalInfoModel = get(GOAL_DATA_PARCELABLE_KEY) as? GoalInfoModel
                goalInfoModel?.let {
                    confirmationViewModel.initGoalInfo(goalInfoModel)
                } ?: kotlin.run {
                    throw IllegalArgumentException("Must pass GoalInfoModel data")
                }
            }

            cancelButton.setOnClickListener {
                root.findNavController().popBackStack()
            }
        }

        confirmationViewModel.addEditSuccessObservable.observe(viewLifecycleOwner, Observer {
            binding.root.findNavController().popBackStack(R.id.goalDetailFragment, true)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewData(confirmationViewModel.goalInfoModel)
    }

    private fun setUpViewData(goalInfoModel: GoalInfoModel) {
        binding.apply {
            imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_plant)

            if (goalInfoModel.hasCompleteDate) {
                subHeaderTextView.text = PayPlanUtils.getPayPlanRecurrenceDisplayStringForRecurrenceType(context!!,
                        goalInfoModel.recurrenceType.toString())

                goalInfoModel.goalCompleteDate?.let { goalDate ->
                    val formattedDate = goalDate.toString(DisplayDateTimeUtils.shortDateFormatter)

                    recurrenceDescriptionTextView.text = when (goalInfoModel.recurrenceType) {
                        PayPlanType.WEEK -> {
                             String.format(getString(R.string.GOAL_EDIT_CONFIRMATION_GOAL_COMPLETE_DATE_WEEKLY_FORMAT),
                                    DisplayDateTimeUtils.getDayOfWeekStringForNumber(goalInfoModel.dayOfWeek), formattedDate)
                        }
                        else -> {
                            String.format(getString(R.string.GOAL_EDIT_CONFIRMATION_GOAL_COMPLETE_DATE_FORMAT), formattedDate)
                        }
                    }
                }
            } else {
                when (goalInfoModel.recurrenceType) {
                    PayPlanType.WEEK -> {
                        recurrenceDescriptionTextView.text = String.format(
                                getString(R.string.GOAL_EDIT_CONFIRMATION_RECURRENCE_WEEK_FORMAT),
                                DisplayDateTimeUtils.getDayOfWeekStringForNumber(goalInfoModel.dayOfWeek))
                    }
                    PayPlanType.MONTH -> {
                        goalInfoModel.startDate?.let { nextRunDate ->
                            recurrenceDescriptionTextView.text = String.format(
                                    getString(R.string.GOAL_EDIT_CONFIRMATION_RECURRENCE_MONTHLY_FORMAT),
                                    nextRunDate.toString(DisplayDateTimeUtils.shortDateFormatter))
                        } ?: run {
                            throw IllegalArgumentException("monthly startDate is null. Must have a startDate!")
                        }
                    }
                    else -> {
                        recurrenceDescriptionTextView.visibility = View.GONE
                    }
                }

                val amountWithCurrencySymbol = StringUtils.formatCurrencyStringWithFractionDigits(goalInfoModel.frequencyAmount.toString(), true)
                val amountPerRecurrenceFormat = String.format(getString(R.string.GOALS_RECURRENCE_FORMAT), amountWithCurrencySymbol,
                        goalInfoModel.recurrenceType.toString().toLowerCase())
                val splitStringArray = amountPerRecurrenceFormat.split(".")
                if (splitStringArray.size == 2) {
                    subHeaderTextView.text = amountPerRecurrenceFormat.applyRelativeSizeToSubstring(FREQUENCY_SUBSTRING_RELATIVE_HEIGHT, splitStringArray[1])
                } else {
                    subHeaderTextView.text = amountPerRecurrenceFormat
                }
            }
        }
    }
}