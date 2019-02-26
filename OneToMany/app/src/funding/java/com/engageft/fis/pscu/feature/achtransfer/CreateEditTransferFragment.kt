package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentCreateEditTransferBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.TRANSFER_FUNDS_BUNDLE_KEY
import com.engageft.fis.pscu.feature.achtransfer.CardLoadConstants.SCHEDULED_LOAD_ID
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils
import utilGen1.StringUtils

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
        val scheduleLoadId = arguments!!.getLong(CardLoadConstants.SCHEDULED_LOAD_ID_KEY, SCHEDULED_LOAD_ID)
        createEditTransferViewModel = CreateEditTransferViewModelFactory(scheduleLoadId).create(CreateEditTransferViewModel::class.java)
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

            frequencyBottomSheet.dialogOptions = ArrayList(ScheduledLoadUtils.getFrequencyDisplayStringsForTransfer(context!!))
            daysOfWeekBottomSheet.dialogOptions = ArrayList(DisplayDateTimeUtils.daysOfWeekList())
            // don't allow today's date as a selection
            val minDate = DateTime.now().plusDays(1)
            val maxDate = DateTime.now().plusDays(60)
            date1BottomSheet.minimumDate = minDate
            date1BottomSheet.maximumDate = maxDate
            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode

            nextButton.setOnClickListener {
                navigateToConfirmationScreen()
            }

            createEditTransferViewModel.apply {

                formModeObservable.observe(viewLifecycleOwner, Observer { mode ->
                    when (mode) {
                        CreateEditTransferViewModel.FormMode.EDIT -> {
                            setEditMode()
                        }
                        CreateEditTransferViewModel.FormMode.CREATE -> {
                            toolbarController.setToolbarTitle(getString(R.string.ach_bank_transfer_create_transfer))
                        }
                    }
                })

                buttonStateObservable.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        CreateEditTransferViewModel.ButtonState.SHOW -> binding.nextButton.visibility = View.VISIBLE
                        CreateEditTransferViewModel.ButtonState.HIDE -> binding.nextButton.visibility = View.GONE
                    }
                    activity?.invalidateOptionsMenu()
                })

                fromAccountObservable.observe(viewLifecycleOwner, Observer { fundSourceList ->
                    val fromFundSourceMediator = HashMap<String, CreateEditTransferViewModel.FundSourceModel>()
                    // format ACH bank name with last 4 digits
                    val fromOptionsList = ArrayList<CharSequence>()
                    var formattedString: String
                    fundSourceList.forEach { source ->
                        formattedString = when (source.sourceType) {
                            FundSourceType.STANDARD_ACH -> {
                                String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT),
                                        source.name, source.lastFour)
                            }
                            FundSourceType.STANDARD_DEBIT -> {
                                String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT),
                                        getString(R.string.card_load_card), source.lastFour)
                            }
                        }
                        fromOptionsList.add(formattedString)
                        fromFundSourceMediator[formattedString] = source
                    }

                    if (fromOptionsList.isNotEmpty() && fromOptionsList.size == 1) {
                        accountFromBottomSheet.setEnable(false)
                        accountFromBottomSheet.inputText = fromOptionsList[0]
                    } else {
                        accountFromBottomSheet.dialogOptions = fromOptionsList
                    }

                    createEditTransferViewModel.setFundSourceListMediator(fromFundSourceMediator)
                })

                toAccountObservable.observe(viewLifecycleOwner, Observer { cardList ->
                    val toFundDestinationMediator = HashMap<String, CreateEditTransferViewModel.CardInfoModel>()

                    // format Card Info with last four
                    val toOptionsList = ArrayList<CharSequence>()
                    var element: String
                    cardList.forEach { cardInfoModel ->
                        element = String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_WITH_BALANCE_FORMAT),
                                cardInfoModel.name, cardInfoModel.lastFour,
                                StringUtils.formatCurrencyStringWithFractionDigits(cardInfoModel.balance.toString(), true))
                        toOptionsList.add(element)
                        toFundDestinationMediator[element] = cardInfoModel
                    }

                    setSelectedCard(toFundDestinationMediator)
                    if (toOptionsList.isNotEmpty() && toOptionsList.size == 1) {
                        accountToBottomSheet.setEnable(false)
                        accountToBottomSheet.inputText = toOptionsList[0]
                    } else {
                        accountToBottomSheet.dialogOptions = toOptionsList
                    }
                })

                deleteSuccessObservable.observe(viewLifecycleOwner, Observer {
                    binding.root.findNavController().popBackStack(R.id.accountsAndTransfersListFragment, false)
                })

                fundingStateObservable.observe(viewLifecycleOwner, Observer { state ->
                    when (state) {
                        CreateEditTransferViewModel.FundingState.ERROR -> {
                            headerTextView.visibility = View.GONE
                            subHeaderTextView.visibility = View.GONE

                            errorStateLayout.visibility = View.VISIBLE
                            val errorTitleTextView = errorStateLayout.findViewById<TextView>(R.id.titleTextView)
                            val messageTextView = errorStateLayout.findViewById<TextView>(R.id.descriptionTextView)
                            errorTitleTextView.text = getString(R.string.ach_bank_transfer_create_error_title)
                            messageTextView.text = getString(R.string.ach_bank_transfer_create_error_message)
                        }
                        CreateEditTransferViewModel.FundingState.OK -> {
                            errorStateLayout.visibility = View.GONE
                            headerTextView.visibility = View.VISIBLE
                            subHeaderTextView.visibility = View.VISIBLE
                        }
                    }
                })
            }
        }

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        return binding.root
    }

    private fun setEditMode() {
        binding.apply {
            toolbarController.setToolbarTitle(getString(R.string.ach_bank_transfer_edit_transfer_screen_title))

            deleteButtonLayout.visibility = View.VISIBLE
            headerTextView.visibility = View.GONE
            subHeaderTextView.visibility = View.GONE

            deleteButtonLayout.setOnClickListener {
                val infoDialog = InformationDialogFragment.newLotusInstance(title = getString(R.string.ach_bank_transfer_delete_transfer_title),
                        message = String.format(getString(R.string.ach_bank_transfer_delete_transfer_message_format),
                                createEditTransferViewModel.frequency.get(), createEditTransferViewModel.from.get()),
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
            accountFromBottomSheet.setEnable(false)
            accountToBottomSheet.setEnable(false)
            amountInputWithLabel.setEnable(false)
            frequencyBottomSheet.setEnable(false)
            daysOfWeekBottomSheet.setEnable(false)
            date1BottomSheet.setEnable(false)
        }
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
        binding.root.findNavController().navigate(R.id.action_createEditTransferFragment_to_createTransferConfirmationFragment,
                bundleOf(TRANSFER_FUNDS_BUNDLE_KEY to createEditTransferViewModel.transferFundsModel))
    }
}