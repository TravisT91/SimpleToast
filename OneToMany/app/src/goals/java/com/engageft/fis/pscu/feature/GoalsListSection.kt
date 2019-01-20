package com.engageft.fis.pscu.feature

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.CircularTrackingPanel
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.fis.pscu.R

import com.engageft.fis.pscu.feature.branding.Palette
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
            circularTrackingPanel.setTopAndCenterLeftText(goalInfo.name)

            goalInfo.getGoalInfoCompletionDateString(context)?.let {
                circularTrackingPanel.setBottomRightText(it)
            }

            circularTrackingPanel.setBottomLeftText(goalInfo.getGoalInfoProgressString(context))

            val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null)
                goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
            else
                0f
            circularTrackingPanel.setProgress(progress)

            goalInfo.getGoalInfoContributionString(context)?.let {
                circularTrackingPanel.setTopRightText(it)
            }

            if (goalInfo.payPlan != null && goalInfo.payPlan.isPaused) {
                circularTrackingPanel.showDrawableWithinProgressBar(ContextCompat.getDrawable(context, R.drawable.ic_pause)!!)
            } else if (goalInfo.isCompleted()) {
                circularTrackingPanel.setSuccessDrawable(R.drawable.ic_success_check_mark)
                circularTrackingPanel.setBackgroundSuccessColor(Palette.successColor)
            } else if (!goalInfo.isCompleted() && goalInfo.estimatedCompleteDate.isNotBlank()) {
                val estimatedCompletionDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate)
                if (estimatedCompletionDate != null && estimatedCompletionDate.isBeforeNow) {
                    //TODO: design?
//                    goalStatusImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alert_critical))
//                    goalStatusImageView.visibility = View.VISIBLE
                }
            }
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circularTrackingPanel: CircularTrackingPanel = itemView.findViewById(R.id.circularTrackingPanel)
    }

    interface OnGoalListSectionListener {
        fun onGoalClicked(goalId: Long)
    }
}
