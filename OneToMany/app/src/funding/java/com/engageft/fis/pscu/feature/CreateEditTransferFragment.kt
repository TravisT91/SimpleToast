package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.BottomSheetListInputWithLabel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentCreateEditTransferBinding
import com.engageft.fis.pscu.feature.branding.Palette
import org.joda.time.DateTime
import utilGen1.AchAccountInfoUtils
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils

class CreateEditTransferFragment: BaseEngageFullscreenFragment() {

    lateinit var createEditTransferViewModel: CreateEditTransferViewModel

    override fun createViewModel(): BaseViewModel? {
        createEditTransferViewModel = ViewModelProviders.of(this).get(CreateEditTransferViewModel::class.java)
        return createEditTransferViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCreateEditTransferBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = createEditTransferViewModel
            palette = Palette

            val daysOfWeekList: ArrayList<CharSequence> = ArrayList(ScheduledLoadUtils.getFrequencyDisplayStringsForTransfer(context!!))
            frequencyBottomSheet.dialogOptions = daysOfWeekList
            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())
            date1BottomSheet.minimumDate = DateTime.now()
            date1BottomSheet.maximumDate = DateTime.now().plusMonths(2)
            date2BottomSheet.minimumDate = DateTime.now()
            date2BottomSheet.maximumDate = DateTime.now().plusMonths(2)
            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode
            accountToBottomSheet.isEnabled = false

            frequencyBottomSheet.setOnListSelectedListener(object: BottomSheetListInputWithLabel.OnListSelectedListener {
                override fun onItemSelectedIndex(index: Int) {
                    when (index) {
                        0 -> {
                            binding.date1BottomSheet.visibility = View.GONE
                            binding.date2BottomSheet.visibility = View.GONE
                            binding.daysOfWeekBottomSheet.visibility = View.GONE
                        }
                        1 -> {
                            binding.date1BottomSheet.visibility = View.VISIBLE
                            binding.date2BottomSheet.visibility = View.GONE
                            binding.daysOfWeekBottomSheet.visibility = View.GONE
                        }
                        2 -> {
                            binding.date1BottomSheet.visibility = View.VISIBLE
                            binding.date2BottomSheet.visibility = View.VISIBLE
                            binding.daysOfWeekBottomSheet.visibility = View.GONE
                        }
                        3 -> {
                            binding.date1BottomSheet.visibility = View.GONE
                            binding.date2BottomSheet.visibility = View.GONE
                            binding.daysOfWeekBottomSheet.visibility = View.VISIBLE
                        }
                    }
                }
            })

            accountFromBottomSheet.setOnListSelectedListener(object: BottomSheetListInputWithLabel.OnListSelectedListener {
                override fun onItemSelectedIndex(index: Int) {
                    createEditTransferViewModel.achAccountList.value?.let {
                        if (it.isNotEmpty()) {
                            if (index <= it.size - 1) {
                                // pre-populate the To field
                                accountToBottomSheet.inputText = getString(R.string.programName)
                            } else {
                                InformationDialogFragment.newInstance( title = getString(R.string.alert_error_title_generic),
                                        message ="ACH out is not supported. Please select a correct bank account.", buttonPositiveText = getString(R.string.dialog_information_ok_button),
                                        listener = object : InformationDialogFragment.InformationDialogFragmentListener{
                                            override fun onDialogFragmentNegativeButtonClicked() {}

                                            override fun onDialogFragmentPositiveButtonClicked() {
                                                accountFromBottomSheet.inputText = ""
                                            }

                                            override fun onDialogCancelled() {
                                                accountFromBottomSheet.inputText = ""
                                            }
                                        })
                            }
                        }
                    }
                }
            })
        }

        createEditTransferViewModel.apply {
            achAccountList.observe(this@CreateEditTransferFragment, Observer {
                val achAccountList = mutableListOf<String>()
                for (achAccountInfo in it) {
                    achAccountList.add(AchAccountInfoUtils.accountDescriptionForDisplay(context!!, achAccountInfo))
                }
                // add current account/program name
                achAccountList.add(getString(R.string.programName))
                if (achAccountList.isNotEmpty()) {
                    binding.accountFromBottomSheet.dialogOptions = ArrayList(achAccountList)
                    binding.accountToBottomSheet.dialogOptions = ArrayList(achAccountList)
                }
            })
        }

        return binding.root
    }
}