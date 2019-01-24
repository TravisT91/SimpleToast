package com.engageft.feature.goals

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.util.applyTypefaceToSubstring
import com.engageft.apptoolbox.view.CircularTrackingPanel
import com.engageft.fis.pscu.R
import com.engageft.feature.goals.utils.getGoalInfoCompletionDateString
import com.engageft.feature.goals.utils.getGoalInfoContributionString
import com.engageft.feature.goals.utils.getGoalInfoProgressString
import com.engageft.fis.pscu.feature.branding.Palette
import com.ob.ws.dom.utility.GoalInfo
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class GoalsListSection(private val context: Context,
                       private val goalInfoList: List<GoalsListViewModel.GoalModel>,
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

            val goalModel = goalInfoList[position]

            circularTrackingPanel.setProgress(goalModel.progress)
            circularTrackingPanel.setTopAndCenterLeftText(goalModel.goalInfo.name)
            circularTrackingPanel.setBottomLeftText(applyCustomTypefaceToSubStringProgressString(goalModel.goalInfo))
            circularTrackingPanel.setTopRightText(goalModel.goalInfo.getGoalInfoContributionString(context))
            circularTrackingPanel.setBottomRightText(goalModel.goalInfo.getGoalInfoCompletionDateString(context))

            if (goalModel.goalInfo.payPlan != null && goalModel.goalInfo.payPlan.isPaused) {
                circularTrackingPanel.showDrawableWithinProgressBar(ContextCompat.getDrawable(context, R.drawable.ic_pause)!!)
            } else if (goalModel.goalInfo.isAchieved) {
                circularTrackingPanel.apply {
                    showDrawableWithinProgressBar(ContextCompat.getDrawable(context, R.drawable.ic_success_check_mark)!!)
                    setProgress(100)
                    showProgressBar(true)
                    setProgressColor(ContextCompat.getColor(context, R.color.white))
                    setTopAndCenteredLeftStyle(R.style.progressBarSuccessTitleStyle)
                    setBottomRightTextStyle(R.style.progressBarSuccessBottomRightStyle)
                    setTopRightStyle(R.style.progressBarSuccessTitleStyle)
                    setCardAndBackgroundColor(Palette.successColor)
                    setPanelElevation(0f)
                    showCenteredTitleAndRemoveLeftTextViews()
                }
            }
            //TODO(aHashimi): Waiting for Design about this state. https://engageft.atlassian.net/browse/FOTM-817
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
