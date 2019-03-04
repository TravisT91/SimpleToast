package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCreateTransferConfirmBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericErrorTitleMessageConditionalNewInstance
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils

class CardLoadTransferConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var createTransferViewModel: CardLoadTransferConfirmationViewModel

    private lateinit var cardLoadTransfer: CardLoadTransfer

    override fun createViewModel(): BaseViewModel? {
        cardLoadTransfer = arguments!!.getParcelable(CardLoadConstants.TRANSFER_FUNDS_BUNDLE_KEY) as CardLoadTransfer
        createTransferViewModel = CreateTransferConfirmationViewModelFactory(cardLoadTransfer).create(CardLoadTransferConfirmationViewModel::class.java)
        return createTransferViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCreateTransferConfirmBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = createTransferViewModel
            palette = Palette

            imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_transfer)

            amountTextView.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(cardLoadTransfer.amount.toString(), 0.5f, true)
            val account = if (cardLoadTransfer.fundSourceType == FundSourceType.STANDARD_DEBIT) {
                String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT),
                        getString(R.string.card_load_card), cardLoadTransfer.fromLastFour)
            } else {
                // capitalize account name since it's a proper noun
                String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT), cardLoadTransfer.fromAccountName?.capitalize(), cardLoadTransfer.fromLastFour)
            }
            var recurrenceType = ""
            when(cardLoadTransfer.frequency) {
                ScheduleLoadFrequencyType.MONTHLY -> {
                    cardLoadTransfer.scheduleDate?.let { date ->
                        recurrenceType = String.format(getString(R.string.card_load_transfer_confirmation_frequency_format),
                                DisplayDateTimeUtils.getMediumFormatted(DateTime(date)))
                    }
                }
                ScheduleLoadFrequencyType.EVERY_OTHER_WEEK -> {
                    cardLoadTransfer.scheduleDate?.let { date ->
                        recurrenceType = String.format(getString(R.string.TRANSFER_ALT_WEEKLY_SIMPLE_LOAD_DESCRIPTION),
                                DateTime(date).dayOfWeek().asText)
                    }
                }
                ScheduleLoadFrequencyType.WEEKLY -> {
                    cardLoadTransfer.scheduleDate?.let { date ->
                        recurrenceType = String.format(getString(R.string.TRANSFER_WEEKLY_SIMPLE_LOAD_DESCRIPTION),
                                DateTime(date).dayOfWeek().asText)
                    }
                }
                ScheduleLoadFrequencyType.ONCE -> {
                    transferMessageTextView.visibility = View.GONE
                }
            }
            frequencyTextView.text = String.format(getString(R.string.card_load_transfer_confirmation_from_format), recurrenceType, account)
        }

        createTransferViewModel.createTransferSuccessObservable.observe(viewLifecycleOwner, Observer {
            binding.root.findNavController().popBackStack(R.id.accountsAndTransfersListFragment, false)
        })

        createTransferViewModel.dialogInfoObservable.observe(this, Observer {
            if (it.dialogType == DialogInfo.DialogType.SERVER_ERROR) {
                infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, it)
            }
        })

        return binding.root
    }
}