package com.engageft.feature.goals

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.CircularProgressBarWithTexts
import com.engageft.apptoolbox.view.PillButton
import com.engageft.feature.goals.utils.getGoalInfoCompletionDateString
import com.engageft.feature.goals.utils.getPayPlanInfoContribution
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import utilGen1.StringUtils
import java.math.BigDecimal

class GoalDetailHeaderSection(private val context: Context,
                              private val goalDetailModel: GoalDetailViewModel.GoalDetailModel,
                              private val listener: OnButtonClickListener)
    : StatelessSection((SectionParameters.builder().itemResourceId(R.layout.goal_detail_header_section)).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun onBindItemViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        (p0 as ViewHolder).apply {

            val showZeros = goalDetailModel.goalInfo.fundAmount.compareTo(BigDecimal.ZERO) != 0
            circularProgressBarWithTexts.setInnerTopText(StringUtils.formatCurrencyStringFractionDigitsReducedHeight(
                    amount = goalDetailModel.goalInfo.fundAmount.toFloat(),
                    fractionDigitsPercentHeight = .5f,
                    showZeroDigits = showZeros
            ))

            if (goalDetailModel.goalInfo.isAchieved) {
                circularProgressBarWithTexts.setProgress(goalDetailModel.progress)
                circularProgressBarWithTexts.showInnerBottomText(false)
                circularProgressBarWithTexts.setOuterBottomText(context.getString(R.string.GOALS_COMPLETE))
                circularProgressBarWithTexts.setOuterBottomTextColor(ContextCompat.getColor(context, R.color.structure6))
                transferBalanceButton.visibility = View.VISIBLE
            } else {
                circularProgressBarWithTexts.setProgress(goalDetailModel.progress)

                val goalAmountFormatted = String.format(context.getString(R.string.GOAL_DETAIL_GOAL_AMOUNT_FORMAT),
                        StringUtils.formatCurrencyString(goalDetailModel.goalInfo.amount.toFloat(), true))
                circularProgressBarWithTexts.setInnerBottomText(goalAmountFormatted)

                val frequencyAmountAndCompleteDate = String.format(context.getString(R.string.GOAL_DETAIL_FREQUENCY_AMOUNT_COMPLETE_DATE_FORMAT),
                        goalDetailModel.goalInfo.payPlan.getPayPlanInfoContribution(context),
                        goalDetailModel.goalInfo.getGoalInfoCompletionDateString(context))
                circularProgressBarWithTexts.setOuterBottomText(frequencyAmountAndCompleteDate)

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

    private companion object {
        const val NOT_SET = -1
    }
}