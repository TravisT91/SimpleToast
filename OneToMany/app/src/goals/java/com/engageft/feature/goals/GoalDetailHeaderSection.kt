package com.engageft.feature.goals

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.CircularProgressBarWithTexts
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import utilGen1.StringUtils

class GoalDetailHeaderSection(private val context: Context,
                              private val goalDetailModel: GoalDetailScreenViewModel.GoalDetailModel,
                              private val listener: OnButtonClickListener)
    : StatelessSection((SectionParameters.builder().itemResourceId(R.layout.goal_detail_header_section)).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun onBindItemViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        (p0 as ViewHolder).apply {

            circularProgressBarWithTexts.setInnerTopText(StringUtils.formatCurrencyStringFractionDigitsReducedHeight(
                    amount = goalDetailModel.goalInfo.amount.toFloat(),
                    fractionDigitsPercentHeight = .5f,
                    showZeroDigits = true
            ))

            if (goalDetailModel.goalInfo.isAchieved) {
                circularProgressBarWithTexts.setProgress(goalDetailModel.progress)
                circularProgressBarWithTexts.showInnerBottomText(false)
                circularProgressBarWithTexts.setInnerBottomTextColor(ContextCompat.getColor(context, R.color.structure6))
                transferBalanceButton.visibility = View.VISIBLE
            } else {
                circularProgressBarWithTexts.setProgress(goalDetailModel.progress)
                circularProgressBarWithTexts.setInnerBottomText(goalDetailModel.goalInfo.fundAmount.toString())
                transferBalanceButton.visibility = View.GONE
            }

            transferBalanceButton.setOnClickListener {
                listener.onTransferButtonClicked()
            }
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var circularProgressBarWithTexts: CircularProgressBarWithTexts = itemView.findViewById(R.id.circularProgressBarWithTexts)
        var transferBalanceButton: PillButton = itemView.findViewById(R.id.transferBalanceButton)
    }

    interface OnButtonClickListener {
        fun onTransferButtonClicked()
    }
}