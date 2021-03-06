package com.engageft.fis.pscu.feature.secondaryusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.fis.pscu.R
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
            // TODO(jhutchins): In the case of user clicked clicked: FOTM-694
            // In the case of header or footer clicked, do nothing.
            when (secondaryUserListItem) {
                is SecondaryUserListItem.AddUserType -> { findNavController().navigate(R.id.action_secondaryUserListFragment_to_addSecondaryUserFragment) }
                is SecondaryUserListItem.ActiveSecondaryUserType -> {
                    findNavController().navigate(R.id.action_secondaryUserListFragment_to_viewSecondaryUserFragment,
                            Bundle().apply {
                                putLong(ViewSecondaryUserFragment.KEY_SECONDARY_USER_ID, secondaryUserListItem.userId)
                                putLong(ViewSecondaryUserFragment.KEY_SECONDARY_CARD_ID, secondaryUserListItem.debitCardId)
                            })
                }
            }
        }
    }

    private val userListObserver = Observer<List<SecondaryUserListItem>> { list ->
        recyclerViewAdapter.setSecondaryUserItems(list)
    }

    private val splashObserver = Observer<Boolean> { showSplash ->
        showSplash?.let {
            if (it) {
                binding.secondarySplash.visibility = View.VISIBLE
            } else {
                binding.secondarySplash.visibility = View.GONE
            }
        } ?: kotlin.run {
            binding.secondarySplash.visibility = View.GONE
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

            titleTextView.text = getString(R.string.secondary_users_splash_title_format).applyTypefaceAndColorToSubString(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    Palette.primaryColor,
                    getString(R.string.secondary_users_splash_title_substring))
            buttonAddAccount.setOnClickListener {
                findNavController().navigate(R.id.action_secondaryUserListFragment_to_addSecondaryUserFragment)
            }
        }

        secondaryUserListViewModel.apply {
            secondaryUserListObservable.observe(this@SecondaryUserListFragment, userListObserver)
            showSecondarySplashObservable.observe(this@SecondaryUserListFragment, splashObserver)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Refresh this view when we navigate forward or backward to/from this screen.
        secondaryUserListViewModel.refreshViews()
    }
}