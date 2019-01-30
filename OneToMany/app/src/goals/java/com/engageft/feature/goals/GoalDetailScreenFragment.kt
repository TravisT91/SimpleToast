package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.adapter.SelectableLabelsSection
import com.engageft.feature.goals.GoalsListFragment.Companion.GOAL_ID_KEY
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.ToggleableLabelSection
import com.engageft.fis.pscu.feature.branding.Palette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import java.lang.IllegalArgumentException

class GoalDetailScreenFragment: BaseEngagePageFragment(), SelectableLabelsSection.OnSelectableLabelInteractionListener {

    private lateinit var sectionedAdapter: SectionedRecyclerViewAdapter
    private lateinit var viewModelGoalDetail: GoalDetailScreenViewModel
    private lateinit var binding: FragmentGoalsListBinding

    override fun createViewModel(): BaseViewModel? {
        viewModelGoalDetail = ViewModelProviders.of(this).get(GoalDetailScreenViewModel::class.java)
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

            viewModelGoalDetail.goalDetailModelObservable.observe(viewLifecycleOwner, Observer<GoalDetailScreenViewModel.GoalDetailModel> {
                updateRecyclerView(it)
            })
        }

        return binding.root
    }

    private fun updateRecyclerView(goalDetailModel: GoalDetailScreenViewModel.GoalDetailModel) {
        sectionedAdapter.removeAllSections()

        sectionedAdapter.addSection(SelectableLabelsSection(context!!).addLabel(TRANSFER_LABEL_ID, getString(R.string.GOAL_DETAIL_TRANSFER)))

        sectionedAdapter.addSection(ToggleableLabelSection(context!!, listOf(ToggleableLabelSection.LabelItem(
                labelText = getString(R.string.GOAL_DETAIL_RECURRING_TRANSFER),
                isChecked = !goalDetailModel.goalInfo.payPlan.isPaused)),
                object : ToggleableLabelSection.OnToggleInteractionListener {
                    override fun onChecked(labelId: Int, isChecked: Boolean) {
                        // TODO(aHashimi): FOTM-573
                    }
                }))

        sectionedAdapter.addSection(SelectableLabelsSection(context!!).addLabel(EDIT_LABEL_ID, getString(R.string.GOAL_DETAIL_EDIT)))
        sectionedAdapter.addSection(SelectableLabelsSection(context!!).addLabel(DELETE_LABEL_ID, getString(R.string.GOAL_DETAIL_DELETE)))

        sectionedAdapter.notifyDataSetChanged()
    }

    override fun onLabelClicked(labelId: Int) {
        when (labelId) {
            TRANSFER_LABEL_ID -> {
                // TODO(aHashimi): FOTM-575 single transfer
            }
            EDIT_LABEL_ID -> {
               // TODO(aHashimi): FOTM-837
            }
            DELETE_LABEL_ID -> {
                // TODO(aHashimi): FOTM-573
            }
        }
    }

    companion object {
        const val TRANSFER_LABEL_ID = 0
        const val EDIT_LABEL_ID = 1
        const val DELETE_LABEL_ID = 3

        const val GOAL_ID_DEFAULT = -1L
    }
}