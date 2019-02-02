package com.engageft.feature.goals

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.CircularProgressBarWithTexts
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import utilGen1.StringUtils
import java.math.BigDecimal

class GoalDetailCompleteHeaderSection(private val context: Context,
                                      private val fundAmount: BigDecimal,
                                      private val listener: OnButtonClickListener)
    : StatelessSection((SectionParameters.builder().itemResourceId(R.layout.goal_detail_header_section)).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun onBindItemViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        (p0 as ViewHolder).apply {

            val showZeros = fundAmount.compareTo(BigDecimal.ZERO) != 0
            circularProgressBarWithTexts.setInnerTopText(StringUtils.formatCurrencyStringFractionDigitsReducedHeight(
                    amount = fundAmount.toFloat(),
                    fractionDigitsPercentHeight = .5f,
                    showZeroDigits = showZeros
            ))

            circularProgressBarWithTexts.setProgress(100)
            circularProgressBarWithTexts.showInnerBottomText(false)
            circularProgressBarWithTexts.setOuterBottomText(context.getString(R.string.GOALS_COMPLETE))
            circularProgressBarWithTexts.setOuterBottomTextColor(ContextCompat.getColor(context, R.color.structure6))

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