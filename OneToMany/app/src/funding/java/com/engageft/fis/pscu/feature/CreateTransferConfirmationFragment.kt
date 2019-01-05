package com.engageft.fis.pscu.feature

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
import com.engageft.fis.pscu.feature.CreateEditTransferFragment.Companion.ACH_ACCOUNT_ID
import com.engageft.fis.pscu.feature.CreateEditTransferFragment.Companion.CARD_ID
import com.engageft.fis.pscu.feature.CreateEditTransferFragment.Companion.TRANSFER_AMOUNT
import com.engageft.fis.pscu.feature.CreateEditTransferFragment.Companion.TRANSFER_DATE1
import com.engageft.fis.pscu.feature.CreateEditTransferFragment.Companion.TRANSFER_DATE2
import com.engageft.fis.pscu.feature.CreateEditTransferFragment.Companion.TRANSFER_FREQUENCY
import com.engageft.fis.pscu.feature.branding.Palette
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils

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
                createTransferViewModel.scheduledDate1 = bundle.getString(TRANSFER_DATE1, "")
                createTransferViewModel.scheduledDate2 = bundle.getString(TRANSFER_DATE2, "")
                createTransferViewModel.frequencyType = bundle.getString(TRANSFER_FREQUENCY, "")
            }

            amountTextView.text = createTransferViewModel.amount

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

        return binding.root
    }
}