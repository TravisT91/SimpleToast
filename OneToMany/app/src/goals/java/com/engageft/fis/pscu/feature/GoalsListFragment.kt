package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalsListBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.ob.ws.dom.utility.GoalInfo
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
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

            sectionedAdapter = SectionedRecyclerViewAdapter()
            recyclerView.adapter = sectionedAdapter
            recyclerView.layoutManager = LinearLayoutManager(context!!)

            goalsListViewModel.apply {

                goalsListObservable.observe(viewLifecycleOwner, Observer {
                    updateRecyclerView(it)
                })
            }

            swipeToRefreshLayout.setOnRefreshListener {
                swipeToRefreshLayout.isRefreshing = false
                goalsListViewModel.refreshViews(false)
            }
        }

        return binding.root
    }

    private fun GoalsListViewModel.updateRecyclerView(goalsList: List<GoalInfo>) {
        sectionedAdapter.removeAllSections()

        if (goalsList.isNotEmpty()) {
            sectionedAdapter.addSection(GoalsListHeaderSection(context!!, goalsListViewModel.goalsContributed))

            sectionedAdapter.addSection(GoalsListSection(context!!, goalsList, object : GoalsListSection.OnGoalListSectionListener {
                override fun onGoalClicked(goalId: Long) {
                    Toast.makeText(context, "$goalId is clicked!", Toast.LENGTH_SHORT).show()
                }
            }))
        } else {
            sectionedAdapter.addSection(GoalsEmptyListSection(context!!))
        }

        if (canEditGoal) {
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

    override fun onResume() {
        super.onResume()
        goalsListViewModel.refreshViews(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_create_transfer, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.createTransfer)
        menuItem.isVisible = goalsListViewModel.canEditGoal
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.createTransfer -> run {
                binding.root.findNavController().navigate(R.id.action_goalsListFragment_to_goalsAddStep1Fragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}