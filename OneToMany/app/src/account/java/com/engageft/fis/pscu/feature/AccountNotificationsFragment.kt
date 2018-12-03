package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentAccountNotificationsBinding

class AccountNotificationsFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountNotificationsViewModel: AccountNotificationsViewModel

    override fun createViewModel(): BaseViewModel? {
        accountNotificationsViewModel = ViewModelProviders.of(this).get(AccountNotificationsViewModel::class.java)
        return accountNotificationsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountNotificationsBinding.inflate(inflater, container, false)

        return binding.root
    }
}