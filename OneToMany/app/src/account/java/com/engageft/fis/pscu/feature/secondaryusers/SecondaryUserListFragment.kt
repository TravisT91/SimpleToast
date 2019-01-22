package com.engageft.fis.pscu.feature.secondaryusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentSecondaryUsersListBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * Created by joeyhutchins on 1/15/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SecondaryUserListFragment: BaseEngagePageFragment() {

    private lateinit var secondaryUserListViewModel: SecondaryUserListViewModel
    private lateinit var recyclerViewAdapter: SecondaryUserListRecyclerViewAdapter
    private lateinit var binding: FragmentSecondaryUsersListBinding

    private val selectionListener = object : SecondaryUserListRecyclerViewAdapter.SecondaryUserListSelectionListener {
        override fun onItemClicked(secondaryUserListItem: SecondaryUserListItem) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private val userListObserver = Observer<List<SecondaryUserListItem>> { list ->
        recyclerViewAdapter.setSecondaryUserItems(list)
    }

    private val splashObserver = Observer<Boolean> { showSplash ->
        showSplash?.let {
            if (it) {
                // TODO show splash
            } else {
                // TODO hide splash
            }
        } ?: kotlin.run {
            // TODO hide splash
        }
    }

    override fun createViewModel(): BaseViewModel? {
        secondaryUserListViewModel = ViewModelProviders.of(this).get(SecondaryUserListViewModel::class.java)
        return secondaryUserListViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSecondaryUsersListBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = secondaryUserListViewModel
            palette = Palette

            recyclerView.layoutManager = LinearLayoutManager(context!!)
            recyclerViewAdapter = SecondaryUserListRecyclerViewAdapter(selectionListener)
            recyclerView.adapter = recyclerViewAdapter
        }

        secondaryUserListViewModel.apply {
            secondaryUserListObservable.observe(this@SecondaryUserListFragment, userListObserver)
            showSecondarySplashObservable.observe(this@SecondaryUserListFragment, splashObserver)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        secondaryUserListViewModel.refreshViews()
    }
}