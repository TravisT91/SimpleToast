package com.engageft.fis.pscu.feature

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
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentChangeSecurityQuestionsBinding
import com.engageft.fis.pscu.feature.branding.BrandingManager
import com.engageft.fis.pscu.feature.branding.Palette


/**
 * ChangeSecurityQuestionsFragment
 * <p>
 * Fragment for changing/setting a user's security questions.
 *
 * TODO(jhutchins): This fragment needs to be refactored with the understanding that it could be created from
 * settings or from login in two different contexts with different navigations graphs.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangeSecurityQuestionsFragment : BaseEngagePageFragment() {
    private lateinit var changeSecurityQuestionsViewModel: ChangeSecurityQuestionsViewModel

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return if (changeSecurityQuestionsViewModel.hasUnsavedChanges()) {
                fragmentDelegate.showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
                true
            } else if (changeSecurityQuestionsViewModel.modeObservable.value == ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsMode.CREATE) {
                fragmentDelegate.showDialog(InformationDialogFragment.newLotusInstance(
                        title = getString(R.string.SECURITY_QUESTIONS_CREATE_BACK_TITLE),
                        message = getString(R.string.SECURITY_QUESTIONS_CREATE_BACK_MESSAGE),
                        buttonPositiveText = getString(R.string.SECURITY_QUESTIONS_CREATE_BACK_POSITIVE),
                        buttonNegativeText = getString(R.string.SECURITY_QUESTIONS_CREATE_BACK_NEGATIVE),
                        listener = mustCreateDialogListener))
                true
            } else {
                false
            }
        }
    }

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

    private val mustCreateDialogListener = object : InformationDialogFragment.InformationDialogFragmentListener {
        override fun onDialogFragmentPositiveButtonClicked() {
            // We are in the SecurityQuestionsActivity if this is the case.
            EngageService.getInstance().authManager.logout()
            BrandingManager.clearBranding()
            activity!!.finish()
            findNavController().navigate(R.id.action_changeSecurityQuestionsFragment2_to_notAuthenticatedActivity2)
        }
        override fun onDialogFragmentNegativeButtonClicked() {
            // Do nothing.
        }
        override fun onDialogCancelled() {
            // Do nothing.
        }
    }

    override fun createViewModel(): BaseViewModel? {
        changeSecurityQuestionsViewModel = ViewModelProviders.of(this).get(ChangeSecurityQuestionsViewModel::class.java)
        return changeSecurityQuestionsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChangeSecurityQuestionsBinding.inflate(inflater, container, false)
        binding.viewModel = changeSecurityQuestionsViewModel
        binding.palette = Palette

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        changeSecurityQuestionsViewModel.modeObservable.observe(this, Observer {mode ->
            when (mode) {
                ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsMode.FETCHING -> {
                    binding.header.text = getString(R.string.SECURITY_QUESTIONS_HEADER_FETCHING)
                }
                ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsMode.CHANGE -> {
                    binding.header.text = getString(R.string.SECURITY_QUESTIONS_HEADER_CHANGE)
                }
                ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsMode.CREATE -> {
                    binding.header.text = getString(R.string.SECURITY_QUESTIONS_HEADER_CREATE)
                }
            }
        })
        changeSecurityQuestionsViewModel.navigationObservable.observe(this, Observer { navEvent ->
            when (navEvent) {
                ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsNavigation.NONE -> {}
                ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsNavigation.CHANGE_SUCCESSFUL -> {
                    fragmentDelegate.showDialog(InformationDialogFragment.newLotusInstance(title = getString(R.string.SECURITY_QUESTIONS_SUCCESS_TITLE),
                            message = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_CHANGE),
                            buttonPositiveText = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_OK), listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogCancelled() {
                            binding.root.findNavController().navigateUp()
                        }
                        override fun onDialogFragmentNegativeButtonClicked() {
                            binding.root.findNavController().navigateUp()
                        }
                        override fun onDialogFragmentPositiveButtonClicked() {
                            binding.root.findNavController().navigateUp()
                        }
                    }))
                }
                ChangeSecurityQuestionsViewModel.ChangeSecurityQuestionsNavigation.CREATE_SUCCESSFUL -> {
                    fragmentDelegate.showDialog(InformationDialogFragment.newLotusInstance(title = getString(R.string.SECURITY_QUESTIONS_SUCCESS_TITLE),
                            message = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_CREATE),
                            buttonPositiveText = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_OK), listener = object : InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogCancelled() {
                            binding.root.findNavController().navigate(R.id.action_changeSecurityQuestionsFragment2_to_authenticatedActivity2)
                        }
                        override fun onDialogFragmentNegativeButtonClicked() {
                            binding.root.findNavController().navigate(R.id.action_changeSecurityQuestionsFragment2_to_authenticatedActivity2)
                        }
                        override fun onDialogFragmentPositiveButtonClicked() {
                            binding.root.findNavController().navigate(R.id.action_changeSecurityQuestionsFragment2_to_authenticatedActivity2)
                        }
                    }))
                }
            }
        })
        changeSecurityQuestionsViewModel.questions1List.observe(this, Observer { questions ->
            questions?.let {
                val options = ArrayList<CharSequence>()
                for (question : String in questions) {
                    options.add(question)
                }
                binding.questionsList1.dialogOptions = options
            } ?: kotlin.run {
                binding.questionsList1.dialogOptions = ArrayList<CharSequence>()
            }
        })
        changeSecurityQuestionsViewModel.questions2List.observe(this, Observer { questions ->
            questions?.let {
                val options = ArrayList<CharSequence>()
                for (question : String in questions) {
                    options.add(question)
                }
                binding.questionsList2.dialogOptions = options
            } ?: kotlin.run {
                binding.questionsList1.dialogOptions = ArrayList<CharSequence>()
            }
        })
        changeSecurityQuestionsViewModel.saveButtonStateObservable.observe(this, Observer { buttonState ->
            when(buttonState) {
                ChangeSecurityQuestionsViewModel.SaveButtonState.VISIBLE_ENABLED -> {
                    binding.saveButton.visibility = View.VISIBLE
                    activity?.invalidateOptionsMenu()
                }
                ChangeSecurityQuestionsViewModel.SaveButtonState.GONE -> {
                    binding.saveButton.visibility = View.GONE
                    activity?.invalidateOptionsMenu()
                }
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.change_seq_questions_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val saveMenuItem = menu!!.findItem(R.id.save)
        saveMenuItem.isVisible = changeSecurityQuestionsViewModel.saveButtonStateObservable.value == ChangeSecurityQuestionsViewModel.SaveButtonState.VISIBLE_ENABLED
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.save -> run {
                changeSecurityQuestionsViewModel.onSaveClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}