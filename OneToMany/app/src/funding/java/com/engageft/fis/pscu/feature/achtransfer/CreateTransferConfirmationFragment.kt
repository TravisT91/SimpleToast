package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCreateTransferConfirmBinding
import com.engageft.fis.pscu.feature.BaseEngageFullscreenFragment
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericErrorTitleMessageConditionalNewInstance
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils

class CreateTransferConfirmationFragment: BaseEngageFullscreenFragment() {
    private lateinit var createTransferViewModel: CreateTransferConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        createTransferViewModel = ViewModelProviders.of(this).get(CreateTransferConfirmationViewModel::class.java)
        return createTransferViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCreateTransferConfirmBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = createTransferViewModel
            palette = Palette

            arguments?.let { bundle ->
                createTransferViewModel.achAccountInfoId = bundle.getLong(ACH_ACCOUNT_ID, -1L)
                createTransferViewModel.cardId = bundle.getLong(CARD_ID, -1L)
                createTransferViewModel.amount = bundle.getString(TRANSFER_AMOUNT, "")
                createTransferViewModel.scheduledDate1 = bundle.getSerializable(TRANSFER_DATE1) as? DateTime
                createTransferViewModel.scheduledDate2 = bundle.getSerializable(TRANSFER_DATE2) as? DateTime
                createTransferViewModel.frequencyType = bundle.getString(TRANSFER_FREQUENCY, "")
            } ?: throw IllegalStateException("must pass data")

            amountTextView.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(createTransferViewModel.amount, 0.5f, true)

            when(createTransferViewModel.frequencyType) {
                ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY -> {
                    frequencyTextView.text = String.format(getString(R.string.ach_bank_transfer_create_confirmation_frequency_format),
                            getString(R.string.TRANSFER_TWICE_MONTHLY_TEXT), DisplayDateTimeUtils.getMediumFormatted(DateTime(createTransferViewModel.scheduledDate1)))
                }
                ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> {
                    frequencyTextView.text = String.format(getString(R.string.ach_bank_transfer_create_confirmation_frequency_format),
                            getString(R.string.TRANSFER_MONTHLY_TEXT), DisplayDateTimeUtils.getMediumFormatted(DateTime(createTransferViewModel.scheduledDate1)))
                }
                ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> {
                    frequencyTextView.text = String.format(getString(R.string.TRANSFER_WEEKLY_SIMPLE_LOAD_DESCRIPTION),
                            DateTime(createTransferViewModel.scheduledDate1).dayOfWeek().asText)
                }
                ScheduledLoad.SCHED_LOAD_TYPE_ONCE -> {
                    titleTextView.text = getString(R.string.ach_bank_transfer_create_confirmation_transfer)
                    frequencyTextView.visibility = View.GONE
                }
            }
        }

        createTransferViewModel.navigationEventObservable.observe(this, Observer {
            if (it == CreateTransferConfirmationViewModel.NavigationEvent.TRANSFER_SUCCESS) {
                binding.root.findNavController().popBackStack(R.id.accountsAndTransfersListFragment, false)
            }
        })

        createTransferViewModel.dialogInfoObservable.observe(this, Observer {
            if (it.dialogType == DialogInfo.DialogType.SERVER_ERROR) {
                infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, it)
            }
        })

        return binding.root
    }

    companion object {
        const val CARD_ID = "CARD_ID"
        const val ACH_ACCOUNT_ID = "ACH_ACCOUNT_ID"
        const val TRANSFER_AMOUNT = "TRANSFER_AMOUNT"
        const val TRANSFER_FREQUENCY = "TRANSFER_FREQUENCY"
        const val TRANSFER_DATE1 = "TRANSFER_DATE1"
        const val TRANSFER_DATE2 = "TRANSFER_DATE2"

        private const val DAYS_IN_A_WEEK = 7

        fun createBundle(achAccountId: Long, cardId: Long, frequency: String, amount: String, scheduledDate1: DateTime?,
                      scheduledDate2: DateTime?, dayOfWeek: String): Bundle {

            return Bundle().apply {
                putLong(ACH_ACCOUNT_ID, achAccountId)
                putLong(CARD_ID, cardId)
                putString(TRANSFER_AMOUNT, amount)
                putString(TRANSFER_FREQUENCY, frequency)

                // convert day of week to DateTime()
                when (frequency) {
                    ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> {
                        val selectedDay = DisplayDateTimeUtils.getDayOfWeekNumber(dayOfWeek)
                        val now = DateTime.now()
                        val today: Int = DateTime.now().dayOfWeek + 1 // jodaTime is zero-based
                        val nextRecurringDay = if (selectedDay > today) {
                            selectedDay - today
                        } else {
                            DAYS_IN_A_WEEK - today + selectedDay
                        }
                        putSerializable(TRANSFER_DATE1, now.plusDays(nextRecurringDay))
                    }
                    ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> {
                        putSerializable(TRANSFER_DATE1, scheduledDate1)
                    }
                    ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY -> {
                        putSerializable(TRANSFER_DATE1, scheduledDate1)
                        putSerializable(TRANSFER_DATE2, scheduledDate2)
                    }
                }
            }
        }
    }
}