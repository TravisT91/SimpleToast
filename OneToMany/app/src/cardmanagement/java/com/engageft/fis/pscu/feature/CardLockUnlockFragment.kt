package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardLockUnlockBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyPaletteStyles
import com.engageft.fis.pscu.feature.utils.showAlertConfirmationDialog
import kotlinx.android.synthetic.main.fragment_card_lock_unlock.*

class CardLockUnlockFragment: BaseEngagePageFragment() {

    private lateinit var cardLockUnlockViewModel: CardLockUnlockViewModel

    override fun createViewModel(): BaseViewModel? {
        cardLockUnlockViewModel = ViewModelProviders.of(this).get(CardLockUnlockViewModel::class.java)
        return cardLockUnlockViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCardLockUnlockBinding.inflate(inflater, container, false)
        binding.apply {
            palette = Palette
            viewModel = cardLockUnlockViewModel

            headerTextView.text = getString(R.string.card_lock_unlock_header).applyTypefaceAndColorToSubString(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    Palette.primaryColor,
                    getString(R.string.card_lock_unlock_header_substring))

            lockUnlockButton.setOnClickListener {
                // alert: Are you sure you want to ---- your card?
                var title: String
                var message: String
                if (cardLockUnlockViewModel.isCardLocked()) {
                    title = getString(R.string.card_unlock_button_text)
                    message = String.format(getString(R.string.card_lock_unlock_alert_confirmation_format), getString(R.string.card_unlock_word).toLowerCase())
                } else {
                    title = getString(R.string.card_lock_button_text)
                    message = String.format(getString(R.string.card_lock_unlock_alert_confirmation_format), getString(R.string.card_lock_word).toLowerCase())
                }

                val dialogInfo = showAlertConfirmationDialog(context!!,
                        title = title,
                        message = message,
                        listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                            override fun onDialogFragmentNegativeButtonClicked() {
                            }

                            override fun onDialogFragmentPositiveButtonClicked() {
                                cardLockUnlockViewModel.onLockUnlock()
                            }

                            override fun onDialogCancelled() {
                            }

                        })

                dialogInfo.applyPaletteStyles(context!!)

                fragmentDelegate.showDialog(dialogInfo)
            }
        }

        cardLockUnlockViewModel.cardStatusObservable.observe(this, Observer {
            when (it) {
                CardLockUnlockViewModel.CardStatus.LOCKED -> {
                    toolbarController.setToolbarTitle(getString(R.string.card_unlock_button_text))
                    binding.lockUnlockButton.text = getString(R.string.card_unlock_button_text)

                    val statusWord = getString(R.string.CARD_STATUS_DISPLAY_LOCKED).toUpperCase()
                    val status = String.format(getString(R.string.card_lock_unlock_status_format), statusWord)
                    statusTextView.text = status.applyTypefaceToSubstring(ResourcesCompat.getFont(context!!, R.font.font_bold)!!, statusWord)
                }
                CardLockUnlockViewModel.CardStatus.UNLOCKED -> {
                    toolbarController.setToolbarTitle(getString(R.string.card_lock_button_text))
                    binding.lockUnlockButton.text = getString(R.string.card_lock_button_text)

                    val statusWord = getString(R.string.card_unlocked_word).toUpperCase()
                    val status = String.format(getString(R.string.card_lock_unlock_status_format), statusWord)
                    statusTextView.text = status.applyTypefaceToSubstring(ResourcesCompat.getFont(context!!, R.font.font_bold)!!, statusWord)
                }
                CardLockUnlockViewModel.CardStatus.CHANGED_SUCCESS -> {
                    // if status was locked, it's now unlocked successfully
                    val successStatus = if (cardLockUnlockViewModel.isCardLocked()) {
                        String.format(getString(R.string.card_unlocked_word))
                    } else {
                        getString(R.string.CARD_STATUS_DISPLAY_LOCKED).toLowerCase()
                    }
                    val successMessage = String.format(getString(R.string.card_lock_unlock_success_message_format), successStatus)
                    fragmentDelegate.showDialog(InformationDialogFragment.newLotusInstance(message = successMessage,
                            buttonPositiveText = getString(R.string.dialog_information_ok_button),
                            listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                                override fun onDialogFragmentNegativeButtonClicked() {
                                }

                                override fun onDialogFragmentPositiveButtonClicked() {
                                    binding.root.findNavController().popBackStack()
                                }

                                override fun onDialogCancelled() {
                                    binding.root.findNavController().popBackStack()
                                }
                            }))
                }
                CardLockUnlockViewModel.CardStatus.UNKNOWN -> {
                    throw IllegalStateException("Unknown CardStatus type.")
                }
            }
        })

        return binding.root
    }
}