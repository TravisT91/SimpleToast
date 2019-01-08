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
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.BuildConfig
import com.engageft.fis.pscu.NotAuthenticatedActivity
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * AccountSettingsFragment
 * <p>
 * This baseFragmentIm presents the user with settings options which will navigate them to the appropriate
 * baseFragmentIm to adjust that setting.
 * </p>
 * Created by joeyhutchins on 8/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class AccountSettingsFragment : BaseEngagePageFragment() {
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
                binding.root.findNavController().navigate(R.id.action_account_fragment_to_profileFragment)
            }
            password.setOnClickListener {
                findNavController().navigate(R.id.action_account_fragment_to_changePasswordFragment)
            }
            securityQuestions.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_account_fragment_to_changeSecurityQuestionsFragment)
            }
            notifications.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_account_fragment_to_accountNotificationsFragment)
            }
            statements.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_account_fragment_to_statementsFragment)
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
        inflater?.inflate(R.menu.settings_action_menu, menu)
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