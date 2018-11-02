package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentAccountBinding

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

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountBinding.inflate(inflater, container, false)
        binding.themeUtils = Palette
        binding.apply {
            profile.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            password.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            passcode.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            touchId.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            touchIdSwitch.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            securityQuestions.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            idTheftProtection.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
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
                //TODO(ttkachuk) implement onCLick
            }
            disclosures.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
            logout.setOnClickListener {
                //TODO(ttkachuk) implement onCLick
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.TITLE_SETTINGS)
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