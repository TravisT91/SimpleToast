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
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentChangeSecurityQuestionsBinding


/**
 * ChangeSecurityQuestionsFragment
 * <p>
 * Fragment for changing/setting a user's security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangeSecurityQuestionsFragment : BaseEngageFullscreenFragment() {
    private lateinit var changeSecurityQuestionsViewModel: ChangeSecurityQuestionsViewModel

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return if (changeSecurityQuestionsViewModel.hasUnsavedChanges()) {
                showDialog(infoDialogGenericUnsavedChangesNewInstance(context = activity!!, listener = unsavedChangesDialogListener))
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
                    showDialog(InformationDialogFragment.newLotusInstance(title = getString(R.string.SECURITY_QUESTIONS_SUCCESS_TITLE),
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
                    showDialog(InformationDialogFragment.newLotusInstance(title = getString(R.string.SECURITY_QUESTIONS_SUCCESS_TITLE),
                            message = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_CREATE),
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