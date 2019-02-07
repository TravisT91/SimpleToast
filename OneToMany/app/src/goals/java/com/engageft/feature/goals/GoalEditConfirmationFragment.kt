package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyRelativeSizeToSubstring
import com.engageft.apptoolbox.view.PillButton
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalEditConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils

class GoalEditConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var confirmationViewModel: GoalsAddEditConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        confirmationViewModel = ViewModelProviders.of(this).get(GoalsAddEditConfirmationViewModel::class.java)
        return confirmationViewModel
    }

    private lateinit var binding: FragmentGoalEditConfirmationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalEditConfirmationBinding.inflate(inflater, container, false).apply {
            viewModel = confirmationViewModel
            palette = Palette

            arguments!!.get(GoalsAddStep1Fragment.GOAL_DATA_PARCELABLE_KEY)?.let { goalInfoModel ->
                confirmationViewModel.goalInfoModel = goalInfoModel as GoalInfoModel
            } ?: kotlin.run {
                throw IllegalArgumentException("Must pass GoalInfoModel data")
            }

            cancelButton.setOnClickListener {
                root.findNavController().popBackStack()
            }
        }

        confirmationViewModel.successStateObservable.observe(viewLifecycleOwner, Observer {
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
            val imageLayout = confirmationLayout.findViewById<View>(R.id.imageViewLayout)
            val saveButton = confirmationLayout.findViewById<PillButton>(R.id.saveButton)
            imageLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_plant)
            saveButton.text = getString(R.string.GOAL_EDIT_CONFIRMATION_SAVE)

            confirmationLayout.findViewById<TextView>(R.id.headerTextView).text = getString(R.string.GOAL_EDIT_CONFIRMATION_HEADER)
            val subHeaderTextView = confirmationLayout.findViewById<TextView>(R.id.subHeaderTextView)
            val recurrenceDescriptionTextView = confirmationLayout.findViewById<TextView>(R.id.recurrenceDescriptionTextView)
            recurrenceDescriptionTextView.visibility = View.VISIBLE

            when (goalInfoModel.recurrenceType) {
                PayPlanInfoUtils.PAY_PLAN_WEEK -> {
                    recurrenceDescriptionTextView.text = String.format(
                            getString(R.string.GOAL_EDIT_CONFIRMATION_RECURRENCE_WEEK_FORMAT),
                            DisplayDateTimeUtils.getDayOfWeekStringForNumber(goalInfoModel.dayOfWeek))
                }
                PayPlanInfoUtils.PAY_PLAN_MONTH -> {
                    goalInfoModel.startDate?.let {
                        recurrenceDescriptionTextView.text = String.format(
                                getString(R.string.GOALS_ADD_MONTHLY_LABEL),
                                DisplayDateTimeUtils.getMediumFormatted(it))
                    } ?: run {
                        throw IllegalArgumentException("monthly startDate is null. Must have a startDate!")
                    }

                }
                else -> recurrenceDescriptionTextView.visibility = View.GONE
            }

            val amountWithCurrencySymbol = StringUtils.formatCurrencyStringWithFractionDigits(goalInfoModel.goalAmount.toString(), true)
            val amountPerRecurrenceFormat = String.format(getString(R.string.GOALS_RECURRENCE_FORMAT), amountWithCurrencySymbol, goalInfoModel.recurrenceType.toLowerCase())
            val splitStringArray = amountPerRecurrenceFormat.split(".")
            if (splitStringArray.size == 2) {
                subHeaderTextView.text = amountPerRecurrenceFormat.applyRelativeSizeToSubstring(.7f, splitStringArray[1])
            } else {
                subHeaderTextView.text = amountPerRecurrenceFormat
            }

            confirmationLayout.findViewById<TextView>(R.id.descriptionTextView).text = String.format(getString(R.string.GOAL_EDIT_CONFIRMATION_COMPLETE_DATE_EFFECT_DESCRIPTION), goalInfoModel.goalName.capitalize())
        }
    }
}