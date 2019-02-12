package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.feature.goals.utils.GoalConstants
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentGoalSingleTransferBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance

class GoalSingleTransferFragment: BaseEngagePageFragment() {

    private lateinit var viewModelTransfer: GoalSingleTransferViewModel
    private lateinit var binding: FragmentGoalSingleTransferBinding

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
            return if (viewModelTransfer.hasUnsavedChanges()) {
                fragmentDelegate.showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                true
            } else {
                false
            }
        }
    }

    override fun createViewModel(): BaseViewModel? {
        arguments!!.let {
            val goalId = it.getLong(GoalConstants.GOAL_ID_KEY, GoalConstants.GOAL_ID_DEFAULT)
            if (goalId == GoalConstants.GOAL_ID_DEFAULT) {
                throw IllegalArgumentException("Goal Id is not valid")
            } else {
                viewModelTransfer = ViewModelProviders.of(this, GoalSingleTransferViewModelFactory(goalId)).get(GoalSingleTransferViewModel::class.java)
            }
        }
        return viewModelTransfer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalSingleTransferBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelTransfer
            palette = Palette

            upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
            backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

            amountInputWithLabel.currencyCode = EngageAppConfig.currencyCode
            amountInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    viewModelTransfer.validateAmount(GoalSingleTransferViewModel.Event.ON_FOCUS_LOST)
                }
            })

            nextButton.setOnClickListener {
                navigateToConfirmation()
            }

            viewModelTransfer.apply {

                selectionOptionsListObservable.observe(viewLifecycleOwner, Observer { selectionList ->
                    if (selectionList.isNotEmpty()) {
                        val dialogOptions = ArrayList<CharSequence>()
                        for (accountSelectionOptions in selectionList) {
                            dialogOptions.add(accountSelectionOptions.accountNameAndBalance)
                        }
                        fromBottomSheet.dialogOptions = dialogOptions

                    }
                })

                amountValidationStateObservable.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        GoalSingleTransferViewModel.AmountErrorState.VALID -> {
                            amountInputWithLabel.setErrorTexts(null)
                        }
                        GoalSingleTransferViewModel.AmountErrorState.EMPTY -> {
                            amountInputWithLabel.setErrorTexts(null)
                        }
                        GoalSingleTransferViewModel.AmountErrorState.EXCEEDS -> {
                            amountInputWithLabel.setErrorTexts(listOf(getString(R.string.GOAL_SINGLE_TRANSFER_AMOUNT_ERROR_MESSAGE)))
                        }
                    }
                })

                nextButtonStateObservable.observe(viewLifecycleOwner, Observer {
                    if (it == GoalSingleTransferViewModel.ButtonState.SHOW) {
                        nextButton.visibility = View.VISIBLE
                    } else {
                        nextButton.visibility = View.GONE
                    }
                    activity!!.invalidateOptionsMenu()
                })
            }
        }

        return binding.root
    }

    private fun navigateToConfirmation() {
//        binding.root.findNavController().navigate(R.id.action_goalsAddStep2Fragment_to_goalsAddEditConfirmationFragment,
//                Bundle().apply {
//                    putParcelable(GoalConstants.GOAL_DATA_PARCELABLE_KEY, viewModelTransfer.getGoalData())
//                })

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_goal_next, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val menuItem = menu!!.findItem(R.id.menu_item_next)
        menuItem.isVisible = viewModelTransfer.nextButtonStateObservable.value == GoalSingleTransferViewModel.ButtonState.SHOW
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_item_next -> run {
                navigateToConfirmation()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}