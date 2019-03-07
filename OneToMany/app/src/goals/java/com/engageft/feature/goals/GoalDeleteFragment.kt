package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.feature.goals.utils.GoalConstants.GOAL_FUND_AMOUNT_KEY
import com.engageft.feature.goals.utils.GoalConstants.GOAL_ID_KEY
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentGoalDeleteBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.setPalette
import utilGen1.StringUtils
import java.math.BigDecimal

class GoalDeleteFragment: BaseEngagePageFragment() {
    private lateinit var viewModelDelete: GoalDeleteViewModel

    override fun createViewModel(): BaseViewModel? {
        viewModelDelete = ViewModelProviders.of(this).get(GoalDeleteViewModel::class.java)
        return viewModelDelete
    }

    private lateinit var binding: FragmentGoalDeleteBinding
    private lateinit var goadFundAmount : BigDecimal

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalDeleteBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelDelete
            palette = Palette

            val goalId : Long
            arguments!!.apply {
                goalId = getLong(GOAL_ID_KEY)
                goadFundAmount = getSerializable(GOAL_FUND_AMOUNT_KEY) as BigDecimal
            }

            imageViewLayout.findViewById<AppCompatImageView>(R.id.imageViewIcon).setPalette(true)

            deleteButton.setOnClickListener {
                viewModelDelete.onTransferAndDelete(goalId)
            }

            cancelButton.setOnClickListener {
                binding.root.findNavController().popBackStack()
            }
        }

        viewModelDelete.deleteSuccessObservable.observe(viewLifecycleOwner, Observer {
            binding.root.findNavController().popBackStack(R.id.goalDetailFragment, true)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.ic_goal_delete)

        val amountWithCurrencySymbol = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(goadFundAmount.toString(), .5f, true)
        binding.subHeaderTextView.text = amountWithCurrencySymbol
    }
}