package com.engageft.fis.pscu.feature

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentAccountNotificationsBinding
import com.engageft.fis.pscu.feature.palettebindings.setSwitchTintList
import com.engageft.fis.pscu.feature.palettebindings.setThemeFilled

class AccountNotificationsFragment: BaseEngageFullscreenFragment() {
    private lateinit var accountNotificationsViewModel: AccountNotificationsViewModel

    override fun createViewModel(): BaseViewModel? {
        accountNotificationsViewModel = ViewModelProviders.of(this).get(AccountNotificationsViewModel::class.java)
        return accountNotificationsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountNotificationsBinding.inflate(inflater, container, false)
        binding.apply {
            binding.viewModel = accountNotificationsViewModel
            binding.saveButton.setThemeFilled(true)
            val description = getString(R.string.account_notifications_screen_description)
            binding.descriptionTextView.text = description.applyTypefaceToSubstring(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    getString(R.string.account_notifications_screen_description_subString))
            binding.pushSwitch.setSwitchTintList(Palette.primaryColor, lighter(Palette.primaryColor, .8f),
                    ContextCompat.getColor(context!!, R.color.structure2), ContextCompat.getColor(context!!, android.R.color.darker_gray))

            binding.smsSwitch.setSwitchTintList(Palette.primaryColor, lighter(Palette.primaryColor, .8f),
                    ContextCompat.getColor(context!!, R.color.structure2), ContextCompat.getColor(context!!, android.R.color.darker_gray))

            binding.emailSwitch.setSwitchTintList(Palette.primaryColor, lighter(Palette.primaryColor, .8f),
                    ContextCompat.getColor(context!!, R.color.structure2), ContextCompat.getColor(context!!, android.R.color.darker_gray))

            // set click listener on Disabled push textView
        }

        accountNotificationsViewModel.apply {

            pushObservable.observe(this@AccountNotificationsFragment, Observer {
                binding.pushSwitch.isChecked = it
                // if push notification is enabled and app notifications are NOT enabled
                if (it && !NotificationManagerCompat.from(context!!).areNotificationsEnabled()) {
                    binding.pushDisabledTextView.visibility = View.VISIBLE
                } else {
                    binding.pushDisabledTextView.visibility = View.GONE
                }
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
                        activity?.invalidateOptionsMenu()
                    }
                    AccountNotificationsViewModel.SaveButtonState.HIDE -> {
                        binding.saveButton.visibility = View.GONE
                        activity?.invalidateOptionsMenu()
                    }
                }
            })

            dialogInfoObservable.observe(this@AccountNotificationsFragment, Observer { dialogInfo ->
                when (dialogInfo.dialogType) {
                    DialogInfo.DialogType.GENERIC_SUCCESS -> {
                        showGenericSuccessDialogMessageAndPopBackstack(binding.root)
                    }
                    else -> {}
                }
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.profile_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.save)
        when (accountNotificationsViewModel.saveButtonStateObservable.value) {
            AccountNotificationsViewModel.SaveButtonState.SHOW -> saveMenuItem.isVisible = true
            AccountNotificationsViewModel.SaveButtonState.HIDE -> saveMenuItem.isVisible = false
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

    /**
     * Lightens a color by a given factor.
     * https://stackoverflow.com/questions/4928772/using-color-and-color-darker-in-android
     *
     * @param color
     * The color to lighten
     * @param factor
     * The factor to lighten the color. 0 will make the color unchanged. 1 will make the
     * color white.
     * @return lighter version of the specified color.
     *
     */
    private fun lighter(color: Int, factor: Float): Int {
        val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
        val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
        val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
        return Color.argb(Color.alpha(color), red, green, blue)
    }
}