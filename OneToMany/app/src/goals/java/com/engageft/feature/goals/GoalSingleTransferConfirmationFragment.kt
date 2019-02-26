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
import com.engageft.apptoolbox.ViewUtils.newLotusButtonsStackedInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
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

            imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_transfer)
            subHeaderTextView.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(viewModelConfirmation.transferAmount.toFloat(), GoalConstants.FREQUENCY_SUBSTRING_RELATIVE_HEIGHT, true)

            viewModelConfirmation.apply {

                goalInfoObservable.observe(viewLifecycleOwner, Observer { goalInfo ->
                    when (transferFromType) {
                        GoalSingleTransferViewModel.TransferType.SPENDING_BALANCE -> {
                            nameTextView.text = String.format(getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_GOAL_FORMAT), goalInfo.name)
                            descriptionTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_GOAL_TEXT)
                        }
                        GoalSingleTransferViewModel.TransferType.GOAL -> {
                            nameTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_BALANCE)
                            if (goalInfo.isAchieved) {
                                descriptionTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_GOAL_TEXT)
                            } else {
                                descriptionTextView.text = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_TOWARD_BALANCE_DESCRIPTION)
                            }
                        }
                    }
                })

                deleteSuccessObservable.observe(viewLifecycleOwner, Observer {
                    root.findNavController().popBackStack(R.id.goalDetailFragment, true)
                })

                transferSuccessObservable.observe(viewLifecycleOwner, Observer {
                    root.findNavController().popBackStack(R.id.goalDetailFragment, true)
                })

                saveButton.setOnClickListener {
                    if (shouldPromptToDeleteGoal()) {
                        showShouldDeleteGoalConfirmation()
                    } else {
                        transfer()
                    }
                }
            }
        }

        return binding.root
    }

    private fun showShouldDeleteGoalConfirmation() {
        viewModelConfirmation.goalInfoObservable.value?.apply {
            fragmentDelegate.showDialog(InformationDialogFragment.newLotusButtonsStackedInstance(
                    title = String.format(getString(R.string.GOAL_DELETE_ALERT_TITLE_FORMAT), name),
                    message = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_GOAL_DELETE_SUBTITLE),
                    buttonPositiveText = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_GOAL_DELETE_YES),
                    buttonNegativeText = getString(R.string.GOAL_SINGLE_TRANSFER_CONFIRMATION_GOAL_DELETE_NO),
                    listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogFragmentNegativeButtonClicked() {
                            viewModelConfirmation.transfer()
                        }

                        override fun onDialogFragmentPositiveButtonClicked() {
                            viewModelConfirmation.onTransferAndDelete()
                        }

                        override fun onDialogCancelled() {}

                    }
            ))
        }
    }
}