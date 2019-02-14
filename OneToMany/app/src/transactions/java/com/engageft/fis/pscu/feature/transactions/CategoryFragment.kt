package com.engageft.fis.pscu.feature.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.BudgetCategory
import com.engageft.fis.pscu.databinding.CategoryFragmentBinding
import com.engageft.fis.pscu.feature.BaseEngageSubFragment
import com.engageft.fis.pscu.feature.transactions.adapter.CategoryAdapter

/**
 * CategoryFragment
 *
 * Created by Travis Tkachuk 1/24/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CategoryFragment : BaseEngageSubFragment() {

    var onCategorySelectedListener: ((String) -> Unit) = { _ -> }
    val TAG = this::class.java.simpleName

    lateinit var categoryViewModel: CategoryViewModel
    lateinit var binding: CategoryFragmentBinding

    private val categoryObserver = Observer<List<BudgetCategory>> {
        categories -> setCategoryListForRecyclerView(categories)
    }

    companion object {
        const val ARG_CURRENTLY_SELECTED_SUB_CATEGORY = "ARG_CURRENTLY_SELECTED_SUB_CATEGORY"
    }

    override fun createViewModel(): BaseViewModel? {
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        return categoryViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CategoryFragmentBinding.inflate(inflater, container, false)

        categoryViewModel.categories.observe(this@CategoryFragment, categoryObserver)

        return binding.root
    }

    private fun setCategoryListForRecyclerView(categories: List<BudgetCategory>){
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            val categoryAdapter = CategoryAdapter(
                    context!!,
                    categories,
                    onCategorySelectedListener,
                    arguments?.getString(ARG_CURRENTLY_SELECTED_SUB_CATEGORY))
            adapter = categoryAdapter
            scrollToPosition(categoryAdapter.getCurrentItemPosition())
        }
    }


}

