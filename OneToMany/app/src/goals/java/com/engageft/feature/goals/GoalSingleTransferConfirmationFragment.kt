package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentGoalSingleTransferConfirmationBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment

class GoalSingleTransferConfirmationFragment: BaseEngagePageFragment() {
    private lateinit var viewModelConfirmation: GoalSingleTransferConfirmationViewModel

    override fun createViewModel(): BaseViewModel? {
        viewModelConfirmation = ViewModelProviders.of(this).get(GoalSingleTransferConfirmationViewModel::class.java)
        return viewModelConfirmation
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentGoalSingleTransferConfirmationBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }
}