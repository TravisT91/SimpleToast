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
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GoalsListFragment : BaseEngagePageFragment() {

    private lateinit var sectionedAdapter: SectionedRecyclerViewAdapter
    private lateinit var goalsListViewModel: GoalsListViewModel
    private lateinit var binding: FragmentGoalsListBinding

    override fun createViewModel(): BaseViewModel? {
        goalsListViewModel = ViewModelProviders.of(this).get(GoalsListViewModel::class.java)
        return goalsListViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalsListBinding.inflate(inflater, container, false).apply {
            viewModel = goalsListViewModel
            palette = Palette

            goalsListViewModel.initData(true)

            sectionedAdapter = SectionedRecyclerViewAdapter()
            recyclerView.adapter = sectionedAdapter
            recyclerView.layoutManager = LinearLayoutManager(context!!)

            goalsListViewModel.goalsListObservable.observe(viewLifecycleOwner, Observer<GoalsListViewModel.GoalModelItem> {
                updateRecyclerView(it)
            })

            swipeToRefreshLayout.setOnRefreshListener {
                swipeToRefreshLayout.isRefreshing = false
                goalsListViewModel.refreshData()
            }
        }

        return binding.root
    }

    private fun updateRecyclerView(goalItemModel: GoalsListViewModel.GoalModelItem) {
        sectionedAdapter.removeAllSections()

        if (goalItemModel.goalModelList.isNotEmpty()) {
            sectionedAdapter.addSection(GoalsListHeaderSection(goalItemModel.goalContributions))

            sectionedAdapter.addSection(GoalsListSection(context!!, goalItemModel.goalModelList, object : GoalsListSection.OnGoalListSectionListener {
                override fun onGoalClicked(goalId: Long) {

                    binding.root.findNavController().navigate(R.id.action_goalsListFragment_to_goalDetailScreenFragment,
                            Bundle().apply {
                                putLong(GOAL_ID_KEY, goalId)
                            })
                }
            }))
        } else {
            sectionedAdapter.addSection(GoalsEmptyListSection(context!!))
        }

        if (goalItemModel.canEditGoal) {
            // add button
            sectionedAdapter.addSection(GoalsAddButtonSection(object : GoalsAddButtonSection.OnButtonSectionListener {
                override fun onButtonClicked() {
                    binding.root.findNavController().navigate(R.id.action_goalsListFragment_to_goalsAddStep1Fragment)
                }
            }))
            activity?.invalidateOptionsMenu()
        }

        sectionedAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.general_options_menu_add_icon, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.add)
        var showMenuIcon = false
        goalsListViewModel.goalsListObservable.value?.let {
            showMenuIcon = it.canEditGoal
        }
        menuItem.isVisible = showMenuIcon
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.add -> run {
                binding.root.findNavController().navigate(R.id.action_goalsListFragment_to_goalsAddStep1Fragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val GOAL_ID_KEY = "GOAL_ID_KEY"
    }

}