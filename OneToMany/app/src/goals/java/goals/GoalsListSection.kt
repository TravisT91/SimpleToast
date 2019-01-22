package com.engageft.feature.goals

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.apptoolbox.view.CircularTrackingPanel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.utils.getGoalInfoCompletionDateString
import com.engageft.fis.pscu.feature.utils.getGoalInfoContributionString
import com.engageft.fis.pscu.feature.utils.getGoalInfoProgressString
import com.engageft.fis.pscu.feature.utils.isCompleted
import com.ob.ws.dom.utility.GoalInfo
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class GoalsListSection(private val context: Context,
                       private val goalInfoList: List<GoalInfo>,
                       private val listener: OnGoalListSectionListener) :
        StatelessSection((SectionParameters.builder().itemResourceId(R.layout.goals_list_item)).build()) {

    override fun getContentItemsTotal(): Int {
        return goalInfoList.size
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        holder.apply {

            itemView.setOnClickListener {
                listener.onGoalClicked(position.toLong())
            }

            val goalInfo = goalInfoList[position]

            val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null)
                goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
            else
                0f

            circularTrackingPanel.setProgress(progress)
            circularTrackingPanel.setTopAndCenterLeftText(goalInfo.name)
            circularTrackingPanel.setBottomLeftText(applyCustomTypefaceToSubStringProgressString(goalInfo))

            goalInfo.getGoalInfoContributionString(context)?.let {
                circularTrackingPanel.setTopRightText(it)
            }
            goalInfo.getGoalInfoCompletionDateString(context)?.let {
                circularTrackingPanel.setBottomRightText(it)
            }

            if (goalInfo.payPlan != null && goalInfo.payPlan.isPaused) {
                circularTrackingPanel.showDrawableWithinProgressBar(ContextCompat.getDrawable(context, R.drawable.ic_pause)!!)
            } else if (goalInfo.isCompleted()) {
                circularTrackingPanel.setSuccessMode()
                circularTrackingPanel.setTopRightStyle(R.style.progressBarSuccessTitleStyle)
                circularTrackingPanel.setPanelElevation(0f)
            }
            //TODO(aHashimi): Waiting for Design/Jess meeting about goals. The error state hasn't been determined if this even makes sense (after talking to Jess).
//            else if (!goalInfo.isCompleted() && goalInfo.estimatedCompleteDate.isNotBlank()) {
//                val estimatedCompletionDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate)
//                if (estimatedCompletionDate != null && estimatedCompletionDate.isBeforeNow) {
//                    circularTrackingPanel.showDrawableWithinProgressBar(ContextCompat.getDrawable(context, R.drawable.ic_information)!!)
//                }
//            }
        }
    }

    private fun applyCustomTypefaceToSubStringProgressString(goalInfo: GoalInfo): CharSequence {
        val progressString = goalInfo.getGoalInfoProgressString(context)
        val goalAmount = progressString.split(context.getString(R.string.GOALS_OF_WORD))
        if (goalAmount.size == 2) {
            return progressString.applyTypefaceToSubstring(
                    ResourcesCompat.getFont(context, R.font.font_medium)!!, goalAmount[1])
        }
        return progressString
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circularTrackingPanel: CircularTrackingPanel = itemView.findViewById(R.id.circularTrackingPanel)
    }

    interface OnGoalListSectionListener {
        fun onGoalClicked(goalId: Long)
    }
}
