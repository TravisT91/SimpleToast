package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentChangeSecurityQuestionsBinding

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangeSecurityQuestionsFragment : LotusFullScreenFragment() {
    private lateinit var changeSecurityQuestionsViewModel: ChangeSecurityQuestionsViewModel
    override fun createViewModel(): BaseViewModel? {
        changeSecurityQuestionsViewModel = ViewModelProviders.of(this).get(ChangeSecurityQuestionsViewModel::class.java)
        return changeSecurityQuestionsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChangeSecurityQuestionsBinding.inflate(inflater, container, false)
        binding.viewModel = changeSecurityQuestionsViewModel
        binding.palette = Palette

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
                            positiveButton = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_OK), listener = object : InformationDialogFragment.InformationDialogFragmentListener {
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
                            positiveButton = getString(R.string.SECURITY_QUESTIONS_SUCCESS_MESSAGE_OK), listener = object : InformationDialogFragment.InformationDialogFragmentListener {
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

        return binding.root
    }
}