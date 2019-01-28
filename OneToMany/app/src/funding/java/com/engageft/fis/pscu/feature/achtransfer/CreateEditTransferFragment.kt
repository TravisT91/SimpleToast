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
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentCreateEditTransferBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.achtransfer.AccountsAndTransfersListFragment.Companion.SCHEDULED_LOAD_ID
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import org.joda.time.DateTime
import utilGen1.AchAccountInfoUtils
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils

class CreateEditTransferFragment: BaseEngagePageFragment() {

    private lateinit var createEditTransferViewModel: CreateEditTransferViewModel
    private lateinit var binding: FragmentCreateEditTransferBinding

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
                fragmentDelegate.showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
                        fragmentDelegate.showDialog(infoDialog)
                    }

                    // set fields to disabled in EDIT mode so user can't edit it but they can delete their recurring transfer
                    amountInputWithLabel.isEnabled = false
                    frequencyBottomSheet.isEnabled = false
                    daysOfWeekBottomSheet.isEnabled = false
                    date1BottomSheet.isEnabled = false
                    date2BottomSheet.isEnabled = false
                } else {
                    toolbarController.setToolbarTitle(getString(R.string.ach_bank_transfer_create_transfer))
                }
            }

            accountFromBottomSheet.isEnabled = false
            accountToBottomSheet.isEnabled = false

            frequencyBottomSheet.dialogOptions = ArrayList(ScheduledLoadUtils.getFrequencyDisplayStringsForTransfer(context!!))
            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())
            // don't allow today's date as a selection
            val minDate = DateTime.now().plusDays(1)
            val maxDate = DateTime.now().plusDays(60)
            date1BottomSheet.minimumDate = minDate
            date1BottomSheet.maximumDate = maxDate
            date2BottomSheet.minimumDate = minDate
            date2BottomSheet.maximumDate = maxDate
            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode

            nextButton.setOnClickListener {
                navigateToConfirmationScreen()
            }
        }

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        createEditTransferViewModel.apply {

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

            dialogInfoObservable.observe(this@CreateEditTransferFragment, Observer {
                if (it.dialogType == DialogInfo.DialogType.OTHER) {
                    promptAchIsNotAllowed()
                }
            })
        }

        return binding.root
    }

    private fun promptAchIsNotAllowed() {
        fragmentDelegate.showDialog(InformationDialogFragment.newLotusInstance(
                message = getString(R.string.ach_bank_transfer_create_ach_alert_message),
                buttonPositiveText = getString(R.string.dialog_information_ok_button),
                listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                    override fun onDialogFragmentNegativeButtonClicked() {}

                    override fun onDialogFragmentPositiveButtonClicked() {
                        binding.root.findNavController().popBackStack()
                    }

                    override fun onDialogCancelled() {}
                }))
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
                        achAccountId = createEditTransferViewModel.achAccountInfo!!.achAccountId,
                        cardId = createEditTransferViewModel.currentCard!!.debitCardId,
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