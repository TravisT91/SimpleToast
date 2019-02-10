package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyRelativeSizeToSubstring
import com.engageft.apptoolbox.util.setTextSizeAndFont
import com.engageft.feature.goals.utils.GoalConstants.GOAL_DATA_PARCELABLE_KEY
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalAddConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils

class GoalAddConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var confirmationViewModel: GoalAddEditConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        confirmationViewModel = ViewModelProviders.of(this).get(GoalAddEditConfirmationViewModel::class.java)
        return confirmationViewModel
    }

    private lateinit var binding: FragmentGoalAddConfirmationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalAddConfirmationBinding.inflate(inflater, container, false).apply {
            viewModel = confirmationViewModel
            palette = Palette

            arguments!!.get(GOAL_DATA_PARCELABLE_KEY)?.let { goalInfoModel ->
                confirmationViewModel.goalInfoModel = goalInfoModel as GoalInfoModel
            } ?: kotlin.run {
                throw IllegalArgumentException("Must pass GoalInfoModel data")
            }
        }

        confirmationViewModel.addEditSuccessObservable.observe(viewLifecycleOwner, Observer {
            binding.root.findNavController().popBackStack(R.id.goalsAddStep1Fragment, true)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewData(confirmationViewModel.goalInfoModel)
    }

    private fun setUpViewData(goalInfoModel: GoalInfoModel) {
        binding.apply {
            if (goalInfoModel.hasCompleteDate) {
                imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_calendar)
                headerTextView.text = getString(R.string.GOALS_ADD_COMPLETE_DATE_CONFIRMATION_HEADER)
                subHeaderTextView.text = DisplayDateTimeUtils.getMediumFormatted(goalInfoModel.goalCompleteDate!!)
                descriptionTextView.text = String.format(
                        getString(R.string.GOALS_ADD_CONFIRMATION_DESCRIPTION_FORMAT),
                        goalInfoModel.goalName)
                val test = resources.getDimension(R.dimen.subHeaderTextSize)
                val pair = Pair(first = test, second = ResourcesCompat.getFont(context!!, R.font.font_medium)!!)
                subHeaderTextView.setTextSizeAndFont(pair)
            } else {
                imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_plant)
                headerTextView.text = getString(R.string.GOALS_ADD_FREQUENCY_AMOUNT_CONFIRMATION_HEADER)

                val amountWithCurrencySymbol = StringUtils.formatCurrencyStringWithFractionDigits(goalInfoModel.frequencyAmount.toString(), true)
                val amountPerRecurrenceFormat = String.format(getString(R.string.GOALS_RECURRENCE_FORMAT), amountWithCurrencySymbol, goalInfoModel.recurrenceType.toString().toLowerCase())
                val splitStringArray = amountPerRecurrenceFormat.split(".")
                if (splitStringArray.size == 2) {
                    subHeaderTextView.text = amountPerRecurrenceFormat.applyRelativeSizeToSubstring(.5f, splitStringArray[1])
                } else {
                    subHeaderTextView.text = amountPerRecurrenceFormat
                }
            }

            descriptionTextView.text = String.format(getString(R.string.GOALS_ADD_CONFIRMATION_DESCRIPTION_FORMAT), goalInfoModel.goalName.capitalize())
        }
    }
}