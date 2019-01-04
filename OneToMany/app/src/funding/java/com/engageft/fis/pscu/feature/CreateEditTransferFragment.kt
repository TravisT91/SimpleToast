package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.util.Log
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
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.BottomSheetListInputWithLabel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.engagekit.model.ScheduledLoad
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
    lateinit var binding: FragmentCreateEditTransferBinding

    private val unsavedChangesDialogListener = object : InformationDialogFragment.InformationDialogFragmentListener {
        override fun onDialogFragmentPositiveButtonClicked() {
            findNavController().navigateUp()
        }
        override fun onDialogFragmentNegativeButtonClicked() {
            // Do nothing.
        }
        override fun onDialogCancelled() {
            // Do nothing.
        }
    }

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return if (createEditTransferViewModel.hasUnsavedChanges()) {
                showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                true
            } else {
                false
            }
        }
    }

    override fun createViewModel(): BaseViewModel? {
        createEditTransferViewModel = ViewModelProviders.of(this).get(CreateEditTransferViewModel::class.java)
        return createEditTransferViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCreateEditTransferBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = createEditTransferViewModel
            palette = Palette

            val frequencyTypesList: ArrayList<CharSequence> = ArrayList(ScheduledLoadUtils.getFrequencyDisplayStringsForTransfer(context!!))
            frequencyBottomSheet.dialogOptions = frequencyTypesList
            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())
            date1BottomSheet.minimumDate = DateTime.now()
            date1BottomSheet.maximumDate = DateTime.now().plusMonths(2)
            date2BottomSheet.minimumDate = DateTime.now()
            date2BottomSheet.maximumDate = DateTime.now().plusMonths(2)
            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode
            accountToBottomSheet.isEnabled = false

            if (createEditTransferViewModel.frequency.get()!!.isNotEmpty()) {
                var index = 0
                for (type in frequencyTypesList) {
                    if (type == createEditTransferViewModel.frequency.get()) {
                        setFrequencySubviewsVisibility(index)
                        break
                    }
                    index++
                }
            }

            frequencyBottomSheet.setOnListSelectedListener(object: BottomSheetListInputWithLabel.OnListSelectedListener {
                override fun onItemSelectedIndex(index: Int) {
                    // this index is mapped to the frequencyTypesList
                    setFrequencySubviewsVisibility(index)
                }
            })

            accountFromBottomSheet.setOnListSelectedListener(object: BottomSheetListInputWithLabel.OnListSelectedListener {
                override fun onItemSelectedIndex(index: Int) {
                    createEditTransferViewModel.achAccountListObservable.value?.let { achAccountsList ->
                        if (achAccountsList.isNotEmpty()) {
                            if (index <= achAccountsList.size - 1) {
                                // set ach account id that's selected
                                createEditTransferViewModel.achAccountInfoId = achAccountsList[index].achAccountId
                                // pre-populate the To field
                                accountToBottomSheet.inputText = getString(R.string.programName)
                            } else {
                                InformationDialogFragment.newLotusInstance( title = getString(R.string.alert_error_title_generic),
                                        message ="ACH out is not supported. Please select a correct bank account.", buttonPositiveText = getString(R.string.dialog_information_ok_button),
                                        listener = object : InformationDialogFragment.InformationDialogFragmentListener{
                                            override fun onDialogFragmentNegativeButtonClicked() {}

                                            override fun onDialogFragmentPositiveButtonClicked() {
                                                accountFromBottomSheet.inputText = ""
                                            }

                                            override fun onDialogCancelled() {
                                                accountFromBottomSheet.inputText = ""
                                            }
                                        }).show(activity!!.supportFragmentManager, "wrongAccountDialog")
                            }
                        }
                    }
                }
            })

            nextButton.setOnClickListener {
                navigateToConfirmationScreen()
            }
        }

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        createEditTransferViewModel.apply {
            achAccountListObservable.observe(this@CreateEditTransferFragment, Observer {
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

            buttonStateObservable.observe(this@CreateEditTransferFragment, Observer {
                when (it) {
                   CreateEditTransferViewModel.ButtonState.SHOW -> binding.nextButton.visibility = View.VISIBLE
                   CreateEditTransferViewModel.ButtonState.HIDE -> binding.nextButton.visibility = View.GONE
                }
                activity?.invalidateOptionsMenu()
            })
        }

        return binding.root
    }

    // this index parameter is mapped to the frequencyTypesList
    private fun setFrequencySubviewsVisibility(index: Int) {
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

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_ach_bank_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.submit)
        menuItem.title = getString(R.string.ach_bank_transfer_create_next_button)
        menuItem.isVisible = createEditTransferViewModel.buttonStateObservable.value == CreateEditTransferViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.submit -> run {
                navigateToConfirmationScreen()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToConfirmationScreen() {
        val frequencyType = ScheduledLoadUtils.getFrequencyTypeStringForDisplayString(context!!, createEditTransferViewModel.frequency.get()!!)
        var scheduledDate: String? = null
        // set scheduledDate from week of day selected
        if (frequencyType == ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY) {
            val selectedDay = DisplayDateTimeUtils.getDayOfWeekNumber(createEditTransferViewModel.dayOfWeek.get()!!)
            val now = DateTime.now()
            val today: Int = DateTime.now().dayOfWeek + 1 // jodaTime is zero-based
            val nextRecurringDay = if (selectedDay > today) {
                selectedDay - today
            } else {
                DAYS_IN_A_WEEK - today + selectedDay
            }
            scheduledDate = now.plusDays(nextRecurringDay).toString()
        }
        binding.root.findNavController().navigate(R.id.action_createEditTransferFragment_to_createTransferConfirmationFragment,
                Bundle().apply {
                    putLong(ACH_ACCOUNT_ID, createEditTransferViewModel.achAccountInfoId)
                    putLong(CARD_ID, createEditTransferViewModel.cardId)
                    putString(TRANSFER_AMOUNT, createEditTransferViewModel.amount.get())
                    putString(TRANSFER_FREQUENCY, frequencyType)
                    putString(TRANSFER_DATE1, scheduledDate ?: createEditTransferViewModel.date1.get())
                    putString(TRANSFER_DATE2, createEditTransferViewModel.date2.get())
                })
    }

    companion object {
        const val CARD_ID = "CARD_ID"
        const val ACH_ACCOUNT_ID = "ACH_ACCOUNT_ID"
        const val TRANSFER_AMOUNT = "TRANSFER_AMOUNT"
        const val TRANSFER_FREQUENCY = "TRANSFER_FREQUENCY"
        const val TRANSFER_DATE1 = "TRANSFER_DATE1"
        const val TRANSFER_DATE2 = "TRANSFER_DATE2"

        const val DAYS_IN_A_WEEK = 7
    }
}