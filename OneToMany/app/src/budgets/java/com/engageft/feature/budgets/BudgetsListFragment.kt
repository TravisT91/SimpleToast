package com.engageft.feature.budgets

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.feature.budgets.recyclerview.BudgetItem
import com.engageft.feature.budgets.recyclerview.BudgetsListAdapter
import com.engageft.fis.pscu.databinding.FragmentBudgetsListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import kotlinx.android.synthetic.main.fragment_budgets_list.*

/**
 * BudgetsListFragment
 * <p>
 * Fragment to display a list of budgets
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetsListFragment : BaseEngagePageFragment(), BudgetsListAdapter.BudgetListAdapterListener {
    private lateinit var viewModel: BudgetsListViewModel
    private lateinit var binding: FragmentBudgetsListBinding
    private var budgetsListAdapter: BudgetsListAdapter? = null

    // Without a slight delay, updating the recyclerview causes the close drawer animation to stutter.
    // The delay gives the drawer time to close before updateBudgetLists() is called on the main thread.
    // When I wrote this initially with the ViewModel doing all calculations/processing on a background thread
    // with injected strings, colors, etc. this delay was unnecessary. There was no stuttering.
    // The Pair consists of a BudgetItem for the "Total spent" category at top, and a list of BudgetItems for
    // all the individual category budgets.
    private val budgetsObserver = Observer<Pair<BudgetItem, List<BudgetItem>>> {
        Handler().postDelayed({ updateBudgetsList(totalBudgetItem = it.first, categoryBudgetItems = it.second) }, RECYCLERVIEW_UPDATE_DELAY_MS)
    }

    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(BudgetsListViewModel::class.java)
        return viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBudgetsListBinding.inflate(inflater,container,false)
        binding.apply {
            budgetsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                budgetsListAdapter = BudgetsListAdapter(context!!, this@BudgetsListFragment)
                adapter = budgetsListAdapter
            }

            palette = Palette
        }

        viewModel.budgetsObservable.observe(viewLifecycleOwner, budgetsObserver)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.refresh()
    }

    private fun updateBudgetsList(totalBudgetItem: BudgetItem, categoryBudgetItems: List<BudgetItem>) {
        budgetsListAdapter?.updateBudgetItems(totalBudgetItem, categoryBudgetItems)
    }

    companion object {
        private const val RECYCLERVIEW_UPDATE_DELAY_MS = 250L
    }

    // BudgetItemSection.BudgetItemSectionListener
    override fun onBudgetCategorySelected(categoryName: String) {
        Toast.makeText(context, "Selected category: $categoryName", Toast.LENGTH_SHORT).show()
    }
}