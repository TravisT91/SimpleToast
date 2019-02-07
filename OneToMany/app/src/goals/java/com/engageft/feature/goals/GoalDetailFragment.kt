package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.adapter.HorizontalRuleSection
import com.engageft.apptoolbox.adapter.HorizontalRuleSectionIndentStart
import com.engageft.apptoolbox.adapter.SelectableLabelsSection
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.feature.budgets.extension.isZero
import com.engageft.feature.goals.utils.GoalConstants.DELETE_LABEL_ID
import com.engageft.feature.goals.utils.GoalConstants.EDIT_LABEL_ID
import com.engageft.feature.goals.utils.GoalConstants.GOAL_FUND_AMOUNT_KEY
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID_DEFAULT
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID_KEY
import com.engageft.feature.goals.utils.GoalConstants.TRANSFER_LABEL_ID
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalDetailBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.adapter.ErrorStateSection
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogYesNoNewInstance
import com.engageft.fis.pscu.feature.recyclerview.toggleablelabel.ToggleableLabelSection
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class GoalDetailFragment: BaseEngagePageFragment() {

    private lateinit var sectionedAdapter: SectionedRecyclerViewAdapter
    private lateinit var viewModelGoalDetail: GoalDetailViewModel
    private lateinit var binding: FragmentGoalDetailBinding

    override fun createViewModel(): BaseViewModel? {
        arguments!!.let {
            val goalId = it.getLong(GOAL_ID_KEY, GOAL_ID_DEFAULT)
            if (goalId == GOAL_ID_DEFAULT) {
                throw IllegalArgumentException("Goal Id is not valid")
            } else {
                viewModelGoalDetail = ViewModelProviders.of(this, GoalDetailViewModelFactory(goalId)).get(GoalDetailViewModel::class.java)
            }
        }
        return viewModelGoalDetail
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalDetailBinding.inflate(inflater, container, false).apply {
            viewModel = viewModel
            palette = Palette

            viewModelGoalDetail.initGoalData(useCache = true)

            sectionedAdapter = SectionedRecyclerViewAdapter()
            recyclerView.adapter = sectionedAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)

            viewModelGoalDetail.apply {
                goalDetailStatesListObservable.observe(viewLifecycleOwner, Observer {
                    updateRecyclerView(it)
                })

                goalScreenTitleObservable.observe(viewLifecycleOwner, Observer {
                    toolbarController.setToolbarTitle(it.capitalize())
                })

                goalRecurringTransferObservable.observe(viewLifecycleOwner, Observer {
                    if (it == GoalDetailViewModel.RecurringTransferStatus.PAUSE_RESUME_FAILURE) {
                        // revert toggle switch state
                        updateRecyclerView(viewModelGoalDetail.goalDetailStatesListObservable.value!!)
                    }
                })

                deleteSuccessObservable.observe(viewLifecycleOwner, Observer {
                    binding.root.findNavController().popBackStack()
                })
            }
        }

        return binding.root
    }

    private fun updateRecyclerView(goalDetailStateList: List<GoalDetailState>) {
        sectionedAdapter.apply {
            removeAllSections()

            for (goalDetailState in goalDetailStateList) {
                when (goalDetailState) {
                    is GoalDetailState.ErrorItem -> {
                        addSection(ErrorStateSection(getString(R.string.GOAL_ERROR_TITLE), getString(R.string.GOAL_ERROR_DESCRIPTION), object : ErrorStateSection.OnErrorSectionInteractionListener {
                            override fun onErrorSectionClicked() {
                                // TODO: EDIT GOAL task FOTM-837
                                Toast.makeText(context!!, "On Error section clicked!", Toast.LENGTH_SHORT).show()
                            }
                        }))
                    }
                    is GoalDetailState.GoalIncompleteHeaderItem -> {
                        // header section
                        addSection(GoalDetailIncompleteHeaderSection(context!!, goalDetailState.goalIncompleteHeaderModel))

                        addSection(HorizontalRuleSection())
                    }
                    is GoalDetailState.GoalCompleteHeaderItem -> {
                        addSection(GoalDetailCompleteHeaderSection(context!!, goalDetailState.fundAmount, object:  GoalDetailCompleteHeaderSection.OnButtonClickListener {
                            override fun onTransferButtonClicked() {
                                // TODO(aHashimi): FOTM-575 single transfer
                                Toast.makeText(context!!, "onTransferButtonClicked!", Toast.LENGTH_SHORT).show()
                            }
                        }))

                        addSection(HorizontalRuleSection())
                    }
                    is GoalDetailState.SingleTransferItem -> {
                        addSection(SelectableLabelsSection(
                                context!!,
                                R.style.GoalDetailItemTextStyle,
                                object: SelectableLabelsSection.OnSelectableLabelInteractionListener {
                                    override fun onLabelClicked(labelId: Int) {
                                        // TODO(aHashimi): FOTM-575 single transfer
                                        Toast.makeText(context!!, "on transfer item clicked!", Toast.LENGTH_SHORT).show()
                                    }
                                }).addLabel(TRANSFER_LABEL_ID, getString(R.string.GOAL_DETAIL_TRANSFER)))

                        addSection(HorizontalRuleSectionIndentStart())
                    }
                    is GoalDetailState.GoalPauseItem -> {
                        val goalPaused = if (goalDetailState.errorState == GoalDetailState.ErrorState.ERROR) false else goalDetailState.isGoalPaused
                        addSection(ToggleableLabelSection(listOf(ToggleableLabelSection.LabelItem(
                                labelText = getString(R.string.GOAL_DETAIL_RECURRING_TRANSFER),
                                isChecked = !goalPaused,
                                disableSection = goalDetailState.errorState == GoalDetailState.ErrorState.ERROR)),
                                styleId = R.style.GoalDetailItemTextStyle,
                                listener = object : ToggleableLabelSection.OnToggleInteractionListener {
                                    override fun onChecked(labelId: Int, isChecked: Boolean) {
                                        promptPauseConfirmation(!isChecked)
                                    }
                                }))

                        addSection(HorizontalRuleSectionIndentStart())
                    }
                    is GoalDetailState.EditItem -> {
                        addSection(SelectableLabelsSection(
                                context!!,
                                R.style.GoalDetailItemTextStyle,
                                object : SelectableLabelsSection.OnSelectableLabelInteractionListener {
                                    override fun onLabelClicked(labelId: Int) {
                                        // TODO(aHashimi): FOTM-837
                                        Toast.makeText(context!!, "on edit item clicked!", Toast.LENGTH_SHORT).show()
                                    }

                                })
                                .addLabel(EDIT_LABEL_ID, getString(R.string.GOAL_DETAIL_EDIT)))
                        addSection(HorizontalRuleSectionIndentStart())
                    }
                    is GoalDetailState.DeleteItem -> {
                        addSection(SelectableLabelsSection(
                                context!!,
                                R.style.GoalDetailItemTextStyle,
                                object: SelectableLabelsSection.OnSelectableLabelInteractionListener {
                                    override fun onLabelClicked(labelId: Int) {
                                        onDeleteGoal()
                                    }
                                })
                                .addLabel(DELETE_LABEL_ID, getString(R.string.GOAL_DETAIL_DELETE)))
                        addSection(HorizontalRuleSection())
                    }
                }
            }

            notifyDataSetChanged()
        }
    }

    private fun promptPauseConfirmation(pauseGoal: Boolean) {
        val message = if (pauseGoal) {
            String.format(getString(R.string.GOAL_PAUSE_ALERT_MESSAGE_FORMAT), getString(R.string.GOAL_PAUSE))
        } else {
            String.format(getString(R.string.GOAL_PAUSE_ALERT_MESSAGE_FORMAT), getString(R.string.GOAL_RESUME))
        }
        fragmentDelegate.showDialog(infoDialogYesNoNewInstance(
                context = context!!,
                title = getString(R.string.GOAL_PAUSE_ALERT_TITLE),
                message = message,
                listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                    override fun onDialogFragmentNegativeButtonClicked() {
                        updateRecyclerView(viewModelGoalDetail.goalDetailStatesListObservable.value!!)
                    }

                    override fun onDialogFragmentPositiveButtonClicked() {
                        viewModelGoalDetail.onPauseResumeGoal()
                    }

                    override fun onDialogCancelled() {
                        updateRecyclerView(viewModelGoalDetail.goalDetailStatesListObservable.value!!)
                    }

                }))
    }

    private fun onDeleteGoal() {
        if (viewModelGoalDetail.fundAmount.isZero()) {
            val title = String.format(getString(R.string.GOAL_DELETE_ALERT_TITLE_FORMAT),
                    viewModelGoalDetail.goalName.capitalize())
            fragmentDelegate.showDialog(infoDialogYesNoNewInstance(
                    context = context!!,
                    title = title,
                    message = getString(R.string.GOAL_DELETE_ALERT_MESSAGE_FORMAT),
                    listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogFragmentNegativeButtonClicked() {}

                        override fun onDialogFragmentPositiveButtonClicked() {
                            viewModelGoalDetail.onDelete()
                        }

                        override fun onDialogCancelled() {}

                    }))
        } else {
            binding.root.findNavController().navigate(R.id.action_goalDetailScreenFragment_to_goalDeleteFragment,
                    Bundle().apply {
                        putLong(GOAL_ID_KEY, viewModelGoalDetail.goalId)
                        putSerializable(GOAL_FUND_AMOUNT_KEY, viewModelGoalDetail.fundAmount)
                    })
        }
    }
}