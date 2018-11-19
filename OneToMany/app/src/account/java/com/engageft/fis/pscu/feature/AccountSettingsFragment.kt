package com.engageft.fis.pscu.feature

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.NotAuthenticatedActivity
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountBinding

/**
 * AccountSettingsFragment
 * <p>
 * This fragment presents the user with settings options which will navigate them to the appropriate
 * fragment to adjust that setting.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class AccountSettingsFragment : LotusFullScreenFragment() {
    private lateinit var accountSettingsViewModel: AccountSettingsViewModel

    override fun createViewModel(): BaseViewModel? {
        accountSettingsViewModel = ViewModelProviders.of(this).get(AccountSettingsViewModel::class.java)
        return accountSettingsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountBinding.inflate(inflater, container, false)
        binding.viewModel = accountSettingsViewModel
        binding.palette = Palette
        binding.apply {
            profile.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            password.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }

            securityQuestions.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            notifications.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            statements.setOnClickListener {
                findNavController().navigate(R.id.action_account_fragment_to_statementsFragment)
            }
            secondaryAccount.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            copyright.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            disclosures.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
        }
        accountSettingsViewModel.navigationObservable.observe(this, Observer { navigationEvent ->
            when (navigationEvent) {
                AccountSettingsViewModel.AccountSettingsNavigation.NONE -> {}
                AccountSettingsViewModel.AccountSettingsNavigation.LOGOUT -> {
                    activity!!.finish()
                    startActivity(Intent(context, NotAuthenticatedActivity::class.java))
                }
            }
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.help -> run {
                //TODO(ttkachuk) Implement help menu item
            }
        }
        return super.onOptionsItemSelected(item)
    }
}