package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyRelativeSizeToSubstring
import com.engageft.engagekit.utils.PayPlanInfoUtils
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalEditConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
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
        }

        confirmationViewModel.successStateObservable.observe(viewLifecycleOwner, Observer {
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
            val imageLayout = confirmationLayout.findViewById<View>(R.id.imageViewLayout)
            imageLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_plant)

            confirmationLayout.findViewById<TextView>(R.id.headerTextView).text = getString(R.string.GOAL_EDIT_CONFIRMATION_HEADER)
            val subHeaderTextView = confirmationLayout.findViewById<TextView>(R.id.subHeaderTextView)

            when (goalInfoModel.recurrenceType) {
                PayPlanInfoUtils.PAY_PLAN_WEEK -> {

                }
                PayPlanInfoUtils.PAY_PLAN_MONTH -> {

                }
            }

            val amountWithCurrencySymbol = StringUtils.formatCurrencyStringWithFractionDigits(goalInfoModel.frequencyAmount.toString(), true)
            val amountPerRecurrenceFormat = String.format(getString(R.string.GOALS_RECURRENCE_FORMAT), amountWithCurrencySymbol, goalInfoModel.recurrenceType.toLowerCase())
            val splitStringArray = amountPerRecurrenceFormat.split(".")
            if (splitStringArray.size == 2) {
                subHeaderTextView.text = amountPerRecurrenceFormat.applyRelativeSizeToSubstring(.5f, splitStringArray[1])
            } else {
                subHeaderTextView.text = amountPerRecurrenceFormat
            }

            confirmationLayout.findViewById<TextView>(R.id.descriptionTextView).text = String.format(getString(R.string.GOAL_EDIT_CONFIRMATION_COMPLETE_DATE_EFFECT_DESCRIPTION), goalInfoModel.goalName.capitalize())
        }
    }
}