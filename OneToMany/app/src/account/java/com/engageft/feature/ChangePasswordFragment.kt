package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment: LotusFullScreenFragment() {

    private lateinit var changePasswordViewModel: ChangePasswordViewModel

    override fun createViewModel(): BaseViewModel? {
        changePasswordViewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
        return changePasswordViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        binding.viewModel = changePasswordViewModel

        binding.updatePasswordButton.setOnClickListener {
            changePasswordViewModel.updatePassword()
        }

        changePasswordViewModel.dialogInfoObservable.observe(this, Observer { dialogInfo ->
            when (dialogInfo.dialogType) {
                DialogInfo.DialogType.GENERIC_SUCCESS -> {
                    val listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                        override fun onDialogFragmentNegativeButtonClicked() {
                        }

                        override fun onDialogFragmentPositiveButtonClicked() {
                            binding.root.findNavController().popBackStack()
                        }

                        override fun onDialogCancelled() {
                            binding.root.findNavController().popBackStack()
                        }
                    }

                    showDialog(infoDialogGenericSuccessTitleMessageNewInstance(context!!, listener = listener))

                }
                DialogInfo.DialogType.GENERIC_ERROR -> {
                    showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                }
                DialogInfo.DialogType.SERVER_ERROR -> {
                    showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                }
                else -> {}
            }
        })

        return binding.root
    }
}