package com.engageft.fis.pscu.feature

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountNotificationsBinding
import com.engageft.fis.pscu.feature.branding.Palette
import kotlinx.android.synthetic.main.fragment_account_notifications.*
/**
 * AccountNotificationsFragment
 * </p>
 * Fragment for managing Push, SMS, and email notifications settings
 * </p>
 * Created by Atia Hashimi 12/07/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountNotificationsFragment: BaseEngageFullscreenFragment() {

    private lateinit var accountNotificationsViewModel: AccountNotificationsViewModel
    private lateinit var binding: FragmentAccountNotificationsBinding

    override fun createViewModel(): BaseViewModel? {
        accountNotificationsViewModel = ViewModelProviders.of(this).get(AccountNotificationsViewModel::class.java)
        return accountNotificationsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAccountNotificationsBinding.inflate(inflater, container, false)
        binding.apply {
            binding.viewModel = accountNotificationsViewModel
            binding.palette = Palette

            val description = getString(R.string.account_notifications_screen_description)
            binding.descriptionTextView.text = description.applyTypefaceToSubstring(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    getString(R.string.account_notifications_screen_description_subString))

            // set click listener on Disabled Push View which takes user to the system notification settings
            binding.pushAlertLayout.setOnClickListener {
                val intent = Intent()
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_uid", activity!!.applicationInfo.uid)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.putExtra("android.provider.extra.APP_PACKAGE", activity!!.packageName)
                } else {
                    intent.putExtra("app_package", activity!!.packageName)
                }

                startActivity(intent)
            }
        }

        accountNotificationsViewModel.apply {

            pushObservable.observe(this@AccountNotificationsFragment, Observer { isPushChecked ->
                binding.pushSwitch.isChecked = isPushChecked
                updatePushView(isPushChecked)
            })

            smsObservable.observe(this@AccountNotificationsFragment, Observer {
                binding.smsSwitch.isChecked = it
            })

            emailObservable.observe(this@AccountNotificationsFragment, Observer {
                binding.emailSwitch.isChecked = it
            })

            saveButtonStateObservable.observe(this@AccountNotificationsFragment, Observer {
                when (it) {
                    AccountNotificationsViewModel.SaveButtonState.SHOW -> {
                        binding.saveButton.visibility = View.VISIBLE
                    }
                    AccountNotificationsViewModel.SaveButtonState.HIDE -> {
                        binding.saveButton.visibility = View.GONE
                    }
                }
                activity?.invalidateOptionsMenu()
            })

            dialogInfoObservable.observe(this@AccountNotificationsFragment, Observer { dialogInfo ->
                if (dialogInfo.dialogType == DialogInfo.DialogType.GENERIC_SUCCESS) {
                    showGenericSuccessDialogMessageAndPopBackstack(binding.root)
                }
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)

        updatePushView(pushSwitch.isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.profile_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.save)
        saveMenuItem.isVisible = when (accountNotificationsViewModel.saveButtonStateObservable.value) {
            AccountNotificationsViewModel.SaveButtonState.SHOW -> true
            AccountNotificationsViewModel.SaveButtonState.HIDE -> false
            else -> false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.save -> run {
                accountNotificationsViewModel.onSaveClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updatePushView(isPushChecked: Boolean) {
        // if push notification is enabled and app notifications are Blocked show to the user that they won't receive push notifications
        if (isPushChecked && !NotificationManagerCompat.from(context!!).areNotificationsEnabled()) {
            binding.pushDisabledTextView.visibility = View.VISIBLE
            binding.pushAlertLayout.visibility = View.VISIBLE
            binding.pushRowLayout.setBackgroundColor(Palette.warningColor)
        } else {
            binding.pushDisabledTextView.visibility = View.GONE
            binding.pushAlertLayout.visibility = View.GONE
            binding.pushRowLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.cellBackground))
        }
    }
}