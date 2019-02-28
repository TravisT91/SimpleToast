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
            }
        }
        return binding.root
    }

    private fun setUpEditMode() {
        toolbarController.setToolbarTitle(getString(R.string.card_load_edit_card_screen_title))

        viewModelAddEdit.deleteCardSuccessObservable.observe(viewLifecycleOwner, Observer {
            binding.root.findNavController().popBackStack()
        })
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

                numberInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
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

                cardNumberValidationObservable.observe(viewLifecycleOwner, Observer { validation ->
                    when (validation) {
                        CardLoadAddEditCardViewModel.Validation.EMPTY -> {
                            numberInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.VALID -> {
                            numberInputWithLabel.setErrorTexts(null)
                        }
                        CardLoadAddEditCardViewModel.Validation.INVALID -> {
                            numberInputWithLabel.setErrorTexts(listOf(getString(R.string.card_load_add_edit_card_number_validation_message)))
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
                            R.id.action_cardLoadAddEditCardFragment_to_achBankAccountAddVerifySuccessFragment,
                            bundleOf(CardLoadConstants.SUCCESS_SCREEN_TYPE_KEY to CardLoadConstants.ADD_ACH_BANK_SUCCESS_TYPE))
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