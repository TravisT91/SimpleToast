package com.engageft.app.feature.goals.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.CircularTrackingPanel

import com.engageft.fis.pscu.R
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
        holder.goalNameTextView.text = "Goal Name!! $position"
        holder.itemView.setOnClickListener {
            listener.onGoalClicked(position.toLong())
        }
//        val goalInfo = goalInfoList!![position]
//        holder.goalInfo = goalInfo
//        holder.goalNameTextView.text = goalInfo.name
//
//        holder.goalDetailTextView.setText(StringUtils.getGoalInfoContributionString(context, goalInfo))
//
//        holder.goalFinishedDateTextView.setText(StringUtils.getGoalInfoCompletionDateString(context, goalInfo))
//
//        holder.goalProgressTextView.setText(StringUtils.getGoalInfoProgressString(context, goalInfo))
//
//        val progress = if (goalInfo.amount != null && goalInfo.amount.toFloat() != 0f && goalInfo.fundAmount != null)
//            goalInfo.fundAmount.toFloat() / goalInfo.amount.toFloat()
//        else
//            0f
//        holder.progressRingView.setProgress(progress)
//
//        holder.goalStatusImageView.visibility = View.INVISIBLE
//        if (goalInfo.payPlan != null && goalInfo.payPlan.isPaused) {
//            holder.goalStatusImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause))
//            holder.goalStatusImageView.visibility = View.VISIBLE
//            holder.goalFinishedDateTextView.visibility = View.GONE
//        } else if (GoalInfoUtils.isCompleted(goalInfo)) {
//            holder.goalStatusImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.checkbox_checked_30dp_gray))
//            holder.goalStatusImageView.visibility = View.VISIBLE
//        } else if (!GoalInfoUtils.isCompleted(goalInfo) && !TextUtils.isEmpty(goalInfo.estimatedCompleteDate)) {
//            val estimatedCompletionDate = BackendDateTimeUtils.getDateTimeForYMDString(goalInfo.estimatedCompleteDate)
//            if (estimatedCompletionDate != null && estimatedCompletionDate.isBeforeNow) {
//                holder.goalStatusImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alert_critical))
//                holder.goalStatusImageView.visibility = View.VISIBLE
//            }
//        }

//        holder.bottomHorizontalDividerView.visibility = if (position == contentItemsTotal - 1) View.INVISIBLE else View.VISIBLE
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

//        internal var goalInfo: GoalInfo? = null

        val circularTrackingPanel: CircularTrackingPanel = itemView.findViewById(R.id.circularTrackingPanel)
        val goalNameTextView: TextView = circularTrackingPanel.findViewById(R.id.tv_top_left)
//        private val goalStatusImageView: ImageView
//        private val goalNameTextView: TextView
//        private val goalProgressTextView: TextView
//        private val goalDetailTextView: TextView
//        private val goalFinishedDateTextView: TextView
//        private val bottomHorizontalDividerView: View

        init {

            //            this.goalStatusImageView = itemView.findViewById(R.id.iv_status_icon)
//            this.goalNameTextView = itemView.findViewById(R.id.tv_goal_name)
//            this.goalProgressTextView = itemView.findViewById(R.id.tv_goal_progress)
//            this.goalDetailTextView = itemView.findViewById(R.id.tv_goal_detail)
//            this.goalFinishedDateTextView = itemView.findViewById(R.id.tv_goal_finish_date)
//            this.bottomHorizontalDividerView = itemView.findViewById(R.id.horizontal_divider_bottom)

//            itemView.setOnClickListener { v -> listener?.onGoalClicked(goalInfo) }
        }
    }

    interface OnGoalListSectionListener {
        fun onGoalClicked(goalId: Long)
    }
}
