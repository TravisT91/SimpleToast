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
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.BuildConfig
import com.engageft.fis.pscu.NotAuthenticatedActivity
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountBinding
import kotlinx.android.synthetic.main.fragment_account.*

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
                binding.root.findNavController().navigate(R.id.action_account_fragment_to_changeSecurityQuestionsFragment)
            }
            notifications.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            statements.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            secondaryAccount.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            copyright.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_account_fragment_to_copyrightFragment)
            }
            disclosures.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }

            softwareVersionTextView.text = String.format(getString(R.string.software_version_format), BuildConfig.VERSION_NAME)
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