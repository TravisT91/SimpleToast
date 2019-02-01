package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.adapter.HorizontalRuleSection
import com.engageft.apptoolbox.adapter.HorizontalRuleSectionIndentStart
import com.engageft.apptoolbox.adapter.SelectableLabelsSection
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.ToggleableLabelSection
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogYesNoNewInstance
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import java.lang.IllegalArgumentException
import java.math.BigDecimal

class GoalDetailFragment: BaseEngagePageFragment() {

    private lateinit var sectionedAdapter: SectionedRecyclerViewAdapter
    private lateinit var viewModelGoalDetail: GoalDetailViewModel
    private lateinit var binding: FragmentGoalsListBinding

    override fun createViewModel(): BaseViewModel? {
        viewModelGoalDetail = ViewModelProviders.of(this).get(GoalDetailViewModel::class.java)
        return viewModelGoalDetail
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalsListBinding.inflate(inflater, container, false).apply {
            viewModel = viewModel
            palette = Palette

            arguments!!.let {
                val goalId = it.getLong(GOAL_ID_KEY, GOAL_ID_DEFAULT)
                if (goalId == GOAL_ID_DEFAULT) {
                    throw IllegalArgumentException("Goal Id is not valid")
                } else {
                    viewModelGoalDetail.initGoalDetail(goalId)
                }
            }

            sectionedAdapter = SectionedRecyclerViewAdapter()
            recyclerView.adapter = sectionedAdapter
            recyclerView.layoutManager = LinearLayoutManager(context!!)

            viewModelGoalDetail.goalDetailModelObservable.observe(viewLifecycleOwner, Observer<GoalDetailViewModel.GoalDetailModel> {
                updateRecyclerView(it)
            })

            viewModelGoalDetail.deleteStatusObservable.observe(viewLifecycleOwner, Observer {
                binding.root.findNavController().popBackStack()
            })
        }

        return binding.root
    }

    private fun updateRecyclerView(goalDetailModel: GoalDetailViewModel.GoalDetailModel) {
        sectionedAdapter.removeAllSections()

        toolbarController.setToolbarTitle(goalDetailModel.goalInfo.name.capitalize())

        sectionedAdapter.addSection(GoalDetailHeaderSection(context!!, goalDetailModel, object: GoalDetailHeaderSection.OnButtonClickListener {
            override fun onTransferButtonClicked() {
                // TODO(aHashimi): FOTM-575 single transfer
            }
        }))

        sectionedAdapter.addSection(HorizontalRuleSection())

        sectionedAdapter.addSection(SelectableLabelsSection(
                context!!,
                R.style.GoalDetailItemTextStyle,
                object: SelectableLabelsSection.OnSelectableLabelInteractionListener {
                    override fun onLabelClicked(labelId: Int) {
                        // TODO(aHashimi): FOTM-575 single transfer
                    }
                }).addLabel(TRANSFER_LABEL_ID, getString(R.string.GOAL_DETAIL_TRANSFER)))

        sectionedAdapter.addSection(HorizontalRuleSectionIndentStart())

        if (!goalDetailModel.goalInfo.isAchieved) {
            sectionedAdapter.addSection(ToggleableLabelSection(context!!, listOf(ToggleableLabelSection.LabelItem(
                    labelText = getString(R.string.GOAL_DETAIL_RECURRING_TRANSFER),
                    isChecked = goalDetailModel.goalInfo.payPlan.isPaused)),
                    styleId = R.style.GoalDetailItemTextStyle,
                    listener = object : ToggleableLabelSection.OnToggleInteractionListener {
                        override fun onChecked(labelId: Int, isChecked: Boolean) {
                            // TODO(aHashimi): FOTM-573
                        }
                    }))

            sectionedAdapter.addSection(HorizontalRuleSectionIndentStart())

            sectionedAdapter.addSection(SelectableLabelsSection(
                    context!!,
                    R.style.GoalDetailItemTextStyle,
                    object : SelectableLabelsSection.OnSelectableLabelInteractionListener {
                        override fun onLabelClicked(labelId: Int) {
                            // TODO(aHashimi): FOTM-837
                        }

                    })
                    .addLabel(EDIT_LABEL_ID, getString(R.string.GOAL_DETAIL_EDIT)))
            sectionedAdapter.addSection(HorizontalRuleSectionIndentStart())
        }

        sectionedAdapter.addSection(SelectableLabelsSection(
                context!!,
                R.style.GoalDetailItemTextStyle,
                object: SelectableLabelsSection.OnSelectableLabelInteractionListener {
                    override fun onLabelClicked(labelId: Int) {
                        onDeleteGoal()
                    }
                })
                .addLabel(DELETE_LABEL_ID, getString(R.string.GOAL_DETAIL_DELETE)))
        sectionedAdapter.addSection(HorizontalRuleSection())

        sectionedAdapter.notifyDataSetChanged()
    }

    private fun onDeleteGoal() {
        if (viewModelGoalDetail.goalDetailModelObservable.value!!.goalInfo.fundAmount.compareTo(BigDecimal.ZERO) == 0) {
            val title = String.format(getString(R.string.GOAL_DELETE_ALERT_TITLE_FORMAT),
                    viewModelGoalDetail.goalDetailModelObservable.value!!.goalInfo.name.capitalize())
            fragmentDelegate.showDialog(infoDialogYesNoNewInstance(
                    context = context!!,
                    title = title,
                    message = getString(R.string.GOAL_DELETE_ALERT_MESSAGE_FORMAT),
                    listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogFragmentNegativeButtonClicked() {

                        }

                        override fun onDialogFragmentPositiveButtonClicked() {
                            viewModelGoalDetail.onDelete()
                        }

                        override fun onDialogCancelled() {
                        }

                    }))
        } else {
            binding.root.findNavController().navigate(R.id.action_goalDetailScreenFragment_to_goalDeleteFragment,
                    Bundle().apply {
                        putLong(GOAL_ID_KEY, viewModelGoalDetail.goalDetailModelObservable.value!!.goalInfo.goalId)
                        putSerializable(GOAL_FUND_AMOUNT_KEY, viewModelGoalDetail.goalDetailModelObservable.value!!.goalInfo.fundAmount)
                    })
        }
    }

    companion object {
        const val TRANSFER_LABEL_ID = 0
        const val EDIT_LABEL_ID = 1
        const val DELETE_LABEL_ID = 3

        const val GOAL_ID_DEFAULT = -1L

        const val GOAL_ID_KEY = "GOAL_ID_KEY"
        const val GOAL_FUND_AMOUNT_KEY = "GOAL_FUND_AMOUNT_KEY"
    }
}