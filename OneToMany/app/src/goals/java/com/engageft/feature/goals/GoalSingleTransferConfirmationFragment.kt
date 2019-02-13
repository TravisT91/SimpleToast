package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.feature.goals.utils.GoalConstants
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID_DEFAULT
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID_KEY
import com.engageft.feature.goals.utils.GoalConstants.TRANSFER_AMOUNT_KEY
import com.engageft.feature.goals.utils.GoalConstants.TRANSFER_FROM_KEY
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalSingleTransferConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import utilGen1.StringUtils
import java.math.BigDecimal

class GoalSingleTransferConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var viewModelConfirmation: GoalSingleTransferConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        arguments!!.let { bundle ->
            val goalId = bundle.getLong(GOAL_ID_KEY, GOAL_ID_DEFAULT)
            val transferType = bundle.getSerializable(TRANSFER_FROM_KEY) as GoalSingleTransferViewModel.TransferType
            val amount = bundle.getSerializable(TRANSFER_AMOUNT_KEY) as BigDecimal
            viewModelConfirmation = ViewModelProviders.of(this,
                    GoalSingleTransferConfirmationViewModelFactory(goalId, amount, transferType)).get(GoalSingleTransferConfirmationViewModel::class.java)

        }
        return viewModelConfirmation
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentGoalSingleTransferConfirmationBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelConfirmation
            palette = Palette

            imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_calendar)
            subHeaderTextView.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(viewModelConfirmation.amount.toFloat(), GoalConstants.FREQUENCY_SUBSTRING_RELATIVE_HEIGHT, true)

            if (viewModelConfirmation.transferFromType == GoalSingleTransferViewModel.TransferType.GOAL) {
                descriptionTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_GOAL_TEXT)
            } else {
                nameTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_BALANCE)
                descriptionTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_BALANCE_DESCRIPTION)
            }

            viewModelConfirmation.goalNameObservable.observe(viewLifecycleOwner, Observer { accountName ->
                accountName?.let {
                    nameTextView.text = String.format(
                            getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_GOAL_FORMAT),
                            it)
                }
            })
        }
        return binding.root
    }
}