package com.engageft.feature.goals

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils

class GoalDetailIncompleteHeaderSection(private val context: Context,
                                        private val goalIncompleteHeaderModel: GoalDetailState.GoalIncompleteHeaderItem.GoalIncompleteHeaderModel)
    : StatelessSection((SectionParameters.builder().itemResourceId(R.layout.goal_detail_header_section)).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun onBindItemViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        (p0 as ViewHolder).apply {

            goalIncompleteHeaderModel.apply {

                circularProgressBarWithTexts.setInnerTopText(StringUtils.formatCurrencyStringFractionDigitsReducedHeight(
                        amount = fundAmount.toFloat(),
                        fractionDigitsPercentHeight = .5f,
                        showZeroDigits = false
                ))

                circularProgressBarWithTexts.setProgress(progress)

                val goalAmountFormatted = String.format(context.getString(R.string.GOAL_DETAIL_GOAL_AMOUNT_FORMAT),
                        StringUtils.formatCurrencyString(goalAmount.toFloat(), true))
                circularProgressBarWithTexts.setInnerBottomText(goalAmountFormatted)

                if (isPaused) {
                    circularProgressBarWithTexts.setOuterBottomText(context.getString(R.string.GOALS_PAUSED))

                    if (errorState == GoalDetailState.ErrorState.ERROR) {
                        circularProgressBarWithTexts.setProgressColor(ContextCompat.getColor(context, R.color.goalErrorProgressColor))
                    }
                } else {
                    val frequencyAmountAndCompleteDate = String.format(context.getString(R.string.GOAL_DETAIL_FREQUENCY_AMOUNT_COMPLETE_DATE_FORMAT),
                            StringUtils.formatCurrencyStringWithFractionDigits(frequencyAmount.toFloat(), true),
                            payPlanType.toString().toLowerCase(),
                            DisplayDateTimeUtils.getMediumFormatted(goalCompleteDate))
                    circularProgressBarWithTexts.setOuterBottomText(frequencyAmountAndCompleteDate)

                }
            }
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var circularProgressBarWithTexts: CircularProgressBarWithTexts = itemView.findViewById(R.id.circularProgressBarWithTexts)

        init {
            val transferBalanceButton: PillButton = itemView.findViewById(R.id.transferBalanceButton)
            transferBalanceButton.visibility = View.GONE
        }
    }
}