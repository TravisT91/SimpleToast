package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
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
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.apptoolbox.view.BottomSheetListInputWithLabel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentCreateEditTransferBinding
import com.engageft.fis.pscu.feature.BaseEngageFullscreenFragment
import com.engageft.fis.pscu.feature.achtransfer.AccountsAndTransfersListFragment.Companion.SCHEDULED_LOAD_ID
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import com.ob.ws.dom.utility.AchAccountInfo
import org.joda.time.DateTime
import utilGen1.AchAccountInfoUtils
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils

class CreateEditTransferFragment: BaseEngageFullscreenFragment() {

    private lateinit var createEditTransferViewModel: CreateEditTransferViewModel
    private lateinit var binding: FragmentCreateEditTransferBinding
    private var achAccountsIndexMap = HashMap<Int, AchAccountInfo>()
    private var cardsInfoIndexMap = HashMap<Int, CreateEditTransferViewModel.CardInfo>()
    private val accountsToDisplay = mutableListOf<String>()

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

            arguments?.let {
                val scheduledLoadId = it.getLong(SCHEDULED_LOAD_ID, SCHEDULED_LOAD_ID_DEFAULT)

                //it's in EDIT MODE
                if (scheduledLoadId != SCHEDULED_LOAD_ID_DEFAULT) {

                    toolbarController.setToolbarTitle(getString(R.string.ach_bank_transfer_edit_transfer_screen_title))

                    // don't populate the old data if user has edited the fields and navigates back to this fragment from confirmation
                    createEditTransferViewModel.initScheduledLoads(scheduledLoadId)
                    deleteButtonLayout.visibility = View.VISIBLE
                    titleTextView.visibility = View.GONE
                    subTitleTextView.visibility = View.GONE

                    deleteButtonLayout.setOnClickListener {
                        val infoDialog = InformationDialogFragment.newLotusInstance(title = getString(R.string.ach_bank_transfer_delete_transfer_title),
                                message = String.format(getString(R.string.ach_bank_transfer_delete_transfer_message_format),
                                        createEditTransferViewModel.frequency.get(), createEditTransferViewModel.fromAccount.get()),
                                buttonPositiveText = getString(R.string.dialog_information_yes_button),
                                buttonNegativeText = getString(R.string.dialog_information_no_button),
                                listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                                    override fun onDialogFragmentNegativeButtonClicked() {
                                    }

                                    override fun onDialogFragmentPositiveButtonClicked() {
                                        createEditTransferViewModel.onDeleteScheduledLoad()
                                    }

                                    override fun onDialogCancelled() {
                                    }

                                })
                        infoDialog.positiveButtonTextColor = Palette.errorColor
                        showDialog(infoDialog)
                    }

                    // set fields to disabled in EDIT mode so user can't edit it but they can delete their recurring transfer
                    accountFromBottomSheet.isEnabled = false
                    accountToBottomSheet.isEnabled = false
                    amountInputWithLabel.isEnabled = false
                    frequencyBottomSheet.isEnabled = false
                    daysOfWeekBottomSheet.isEnabled = false
                    date1BottomSheet.isEnabled = false
                    date2BottomSheet.isEnabled = false
                } else {
                    toolbarController.setToolbarTitle(getString(R.string.ach_bank_transfer_create_transfer))
                }
            }

            frequencyBottomSheet.dialogOptions = ArrayList(ScheduledLoadUtils.getFrequencyDisplayStringsForTransfer(context!!))
            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())
            date1BottomSheet.minimumDate = DateTime.now()
            date1BottomSheet.maximumDate = DateTime.now().plusMonths(2)
            date2BottomSheet.minimumDate = DateTime.now()
            date2BottomSheet.maximumDate = DateTime.now().plusMonths(2)
            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode

            accountFromBottomSheet.setOnListSelectedListener(object: BottomSheetListInputWithLabel.OnListSelectedListener {
                override fun onItemSelectedIndex(index: Int) {
                    val achAccountInfo = achAccountsIndexMap[index]

                    achAccountInfo?.let {
                        createEditTransferViewModel.achAccountId = achAccountInfo.achAccountId

                        // populate the To field to the first item of card Info
                        for (entry in cardsInfoIndexMap) {
                            accountToBottomSheet.inputText = String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT), entry.value.name, entry.value.lastFour)
                            createEditTransferViewModel.cardId = entry.value.cardId

                            break
                        }
                    } ?: kotlin.run {
                        //TODO(aHashimi): ACH Out is not support, show dialog to the user when they select their card
                        promptUnsupportedAccount(getString(R.string.ach_bank_transfer_create_ach_out_message))
                        // reset field
                        accountFromBottomSheet.inputText = ""
                    }
                }
            })

            accountToBottomSheet.setOnListSelectedListener(object: BottomSheetListInputWithLabel.OnListSelectedListener {
                override fun onItemSelectedIndex(index: Int) {
                    val cardInfo = cardsInfoIndexMap[index]

                    cardInfo?.let {

                        // check if user is allowed to transfer money to their account
                        if (createEditTransferViewModel.isAchFundingAllowed) {
                            createEditTransferViewModel.cardId = it.cardId

                            //populate the From field
                            for (entry in achAccountsIndexMap) {
                                accountFromBottomSheet.inputText = AchAccountInfoUtils.accountDescriptionForDisplay(context!!, entry.value)
                                break
                            }
                        } else {
                            promptUnsupportedAccount(getString(R.string.ach_bank_transfer_create_ach_in_message))
                            accountToBottomSheet.inputText = ""
                        }
                    } ?: kotlin.run {
                        //TODO(aHashimi): ACH Out is not support, show dialog to the user when they select their card
                        promptUnsupportedAccount(getString(R.string.ach_bank_transfer_create_ach_out_message))
                        accountToBottomSheet.inputText = ""
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

            cardsInfoAndAchAccountsListsObservable.observe(this@CreateEditTransferFragment, Observer {
                val cardsInfoList = it.first
                val achAccountsList = it.second

                var index = 0

                //format bank name with last four digits
                for (achAccountInfo in achAccountsList) {
                    // add bank name to the list for bottomSheet list
                    accountsToDisplay.add(AchAccountInfoUtils.accountDescriptionForDisplay(context!!, achAccountInfo))
                    // mapping index to object when user selects account from accountsToDisplay
                    achAccountsIndexMap[index] = achAccountInfo
                    index++
                }

                for (cardNameAndLastFour in cardsInfoList) {
                    // add cardInfo to the list for bottomSheet list
                    accountsToDisplay.add(String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT), cardNameAndLastFour.name, cardNameAndLastFour.lastFour))
                    // mapping index to object when user selects account from accountsToDisplay
                    cardsInfoIndexMap[index] = cardNameAndLastFour
                    index++
                }

                binding.accountFromBottomSheet.dialogOptions = ArrayList(accountsToDisplay)
                binding.accountToBottomSheet.dialogOptions = ArrayList(accountsToDisplay)
            })

            buttonStateObservable.observe(this@CreateEditTransferFragment, Observer {
                when (it) {
                   CreateEditTransferViewModel.ButtonState.SHOW -> binding.nextButton.visibility = View.VISIBLE
                   CreateEditTransferViewModel.ButtonState.HIDE -> binding.nextButton.visibility = View.GONE
                }
                activity?.invalidateOptionsMenu()
            })

            fromAccountObservable.observe(this@CreateEditTransferFragment, Observer {
                // format ACH bank name with last 4 digits
                fromAccount.set(AchAccountInfoUtils.accountDescriptionForDisplay(context!!, it))
            })

            toAccountObservable.observe(this@CreateEditTransferFragment, Observer {
                // format Card Info with last four
                toAccount.set(String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT), it.name, it.lastFour))
            })

            navigationEventObservable.observe(this@CreateEditTransferFragment, Observer {
                binding.root.findNavController().popBackStack(R.id.accountsAndTransfersListFragment, false)
            })
        }

        return binding.root
    }

    private fun promptUnsupportedAccount(message: String) {
        InformationDialogFragment.newLotusInstance(title = getString(R.string.ach_bank_transfer_create_ach_out_title),
                message = message,
                buttonPositiveText = getString(R.string.dialog_information_ok_button),
                listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                    override fun onDialogFragmentNegativeButtonClicked() {}

                    override fun onDialogFragmentPositiveButtonClicked() {}

                    override fun onDialogCancelled() {}
                }).show(activity!!.supportFragmentManager, "wrongAccountDialog")
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

        val date1 = if (createEditTransferViewModel.date1.get()!!.isNotEmpty()) {
            DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(createEditTransferViewModel.date1.get())
        } else {
            null
        }

        val date2 = if (createEditTransferViewModel.date2.get()!!.isNotEmpty()) {
            DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(createEditTransferViewModel.date2.get())
        } else {
            null
        }

        binding.root.findNavController().navigate(R.id.action_createEditTransferFragment_to_createTransferConfirmationFragment,
                CreateTransferConfirmationFragment.createBundle(
                        achAccountId = createEditTransferViewModel.achAccountId,
                        cardId = createEditTransferViewModel.cardId,
                        frequency = frequencyType,
                        amount = CurrencyUtils.getNonFormattedDecimalAmountString(currencyCode = EngageAppConfig.currencyCode, stringWithCurrencySymbol = createEditTransferViewModel.amount.get()!!),
                        scheduledDate1 = date1,
                        scheduledDate2 = date2,
                        dayOfWeek = createEditTransferViewModel.dayOfWeek.get()!!))
    }

    companion object {
        private const val SCHEDULED_LOAD_ID_DEFAULT = -1L
    }
}