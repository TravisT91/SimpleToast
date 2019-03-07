package com.engageft.fis.pscu.feature.achtransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.DateInputWithLabel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardLoadAddEditCardBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance
import com.engageft.fis.pscu.feature.newInfoDialogInstance
import com.redmadrobot.inputmask.MaskedTextChangedListener

class CardLoadAddEditCardFragment: BaseEngagePageFragment() {

    private lateinit var viewModelAddEdit: CardLoadAddEditCardViewModel
    private lateinit var binding: FragmentCardLoadAddEditCardBinding

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
            if (viewModelAddEdit.hasUnsavedChanges()) {
                fragmentDelegate.showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                return true
            }
            return false
        }
    }

    override fun createViewModel(): BaseViewModel? {
        val ccAccountId = arguments!!.getLong(CardLoadConstants.CC_ACCOUNT_ID_KEY)
        viewModelAddEdit = ViewModelProviders.of(this, CardLoadAddEditCardViewModelFactory(ccAccountId))
                .get(CardLoadAddEditCardViewModel::class.java)
        return viewModelAddEdit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardLoadAddEditCardBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelAddEdit
            palette = Palette

            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            expirationDateInputWithLabel.dateFormat = DateInputWithLabel.DateFormat.MM_YY

            viewModelAddEdit.apply {

                eventTypeObservable.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        CardLoadAddEditCardViewModel.EventType.ADD -> {
                            setUpAddMode()
                        }
                        CardLoadAddEditCardViewModel.EventType.EDIT -> {
                            setUpEditMode()
                        }
                    }
                })

                buttonStateObservable.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        CardLoadAddEditCardViewModel.ButtonState.SHOW -> {
                            addButton.visibility = View.VISIBLE
                        }
                        CardLoadAddEditCardViewModel.ButtonState.HIDE -> {
                            addButton.visibility = View.GONE
                        }
                    }
                    activity?.invalidateOptionsMenu()
                })
            }
        }
        return binding.root
    }

    private fun setUpEditMode() {
        toolbarController.setToolbarTitle(getString(R.string.card_load_edit_card_screen_title))

        binding.apply {
            cardNumberInputWithLabel.inputMask = "**** **** **** [0000]"
            cardNumberInputWithLabel.setEnable(false)
            expirationDateInputWithLabel.setEnable(false)

            viewModelAddEdit.apply {
                deleteCardSuccessObservable.observe(viewLifecycleOwner, Observer {
                    root.findNavController().popBackStack()
                })

                cardExpirationDateObservable.observe(viewLifecycleOwner, Observer { expirationDate ->
                    expirationDate?.let { expirationDateInputWithLabel.inputText = it }
                })

                deleteButtonLayout.setOnClickListener {
                    val cardAndLastFour = String.format(getString(R.string.BANKACCOUNT_DESCRIPTION_FORMAT),
                            getString(R.string.card_load_card), cardNumber.get())
                    val deleteDialogInfo = newInfoDialogInstance(
                            context = context!!,
                            title = getString(R.string.ach_bank_account_delete_confirmation_title),
                            message = String.format(getString(R.string.ach_bank_account_delete_confirmation_message_format), cardAndLastFour),
                            buttonPositiveText = getString(R.string.ach_bank_account_delete),
                            buttonNegativeText = getString(R.string.dialog_information_cancel_button),
                            listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                                override fun onDialogFragmentNegativeButtonClicked() {}

                                override fun onDialogFragmentPositiveButtonClicked() {
                                    viewModelAddEdit.deleteCard()
                                }

                                override fun onDialogCancelled() {}

                            })

                    deleteDialogInfo.positiveButtonTextColor = Palette.errorColor
                    fragmentDelegate.showDialog(deleteDialogInfo)
                }
            }
        }
    }

    private fun setUpAddMode() {
        toolbarController.setToolbarTitle(getString(R.string.card_load_add_card_screen_title))

        binding.apply {

            viewModelAddEdit.apply {

                expirationDateInputWithLabel.addTextChangeListener(object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        expirationDate.set(binding.expirationDateInputWithLabel.getInputTextWithMask().toString())
                    }
                })

                cardNumberInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        validateCardNumber(CardLoadAddEditCardViewModel.ValidationType.ON_FOCUS_LOST)
                    }
                })

                cvvInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        validateCvv(CardLoadAddEditCardViewModel.ValidationType.ON_FOCUS_LOST)
                    }
                })

                expirationDateInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        validateExpirationDate(CardLoadAddEditCardViewModel.ValidationType.ON_FOCUS_LOST)
                    }
                })

                cardNumberValidationObservable.observe(viewLifecycleOwner, Observer { validation ->
                    when (validation) {
                        CardLoadAddEditCardViewModel.Validation.EMPTY -> {
                            cardNumberInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.VALID -> {
                            cardNumberInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.INVALID -> {
                            cardNumberInputWithLabel.setErrorTexts(listOf(getString(R.string.card_load_add_edit_card_number_validation_message)))
                        }
                    }
                })

                cvvValidationObservable.observe(viewLifecycleOwner, Observer { validation ->
                    when (validation) {
                        CardLoadAddEditCardViewModel.Validation.EMPTY -> {
                            cvvInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.VALID -> {
                            cvvInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.INVALID -> {
                            cvvInputWithLabel.setErrorTexts(listOf(getString(R.string.card_load_add_edit_card_cvv_validation_message)))
                        }
                    }
                })

                cardExpirationValidationObservable.observe(viewLifecycleOwner, Observer { validation ->
                    when (validation) {
                        CardLoadAddEditCardViewModel.Validation.EMPTY -> {
                            expirationDateInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.VALID -> {
                            expirationDateInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.INVALID -> {
                            expirationDateInputWithLabel.setErrorTexts(listOf(getString(R.string.card_load_add_edit_card_expiration_validation_message)))
                        }
                    }
                })

                addCardSuccessObservable.observe(viewLifecycleOwner, Observer {
                    root.findNavController().navigate(
                            R.id.action_cardLoadAddEditCardFragment_to_cardLoadSuccessFragment,
                            bundleOf(CardLoadConstants.SUCCESS_SCREEN_TYPE_KEY to SuccessType.ADD_CARD))
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_ach_bank_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val submitMenuItem = menu!!.findItem(R.id.submit)
        submitMenuItem.title = getString(R.string.ach_bank_account_button_add)
        submitMenuItem.isVisible = viewModelAddEdit.buttonStateObservable.value == CardLoadAddEditCardViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.submit -> {
                viewModelAddEdit.addCard()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}