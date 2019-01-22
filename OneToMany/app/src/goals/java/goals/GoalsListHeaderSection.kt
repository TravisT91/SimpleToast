package goals

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import utilGen1.StringUtils

class GoalsListHeaderSection(private val totalSaved: String)
    : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.goals_list_header_section).build()) {

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        holder.goalAmountSavedTextView.text = StringUtils.formatCurrencyStringFractionDigitsReducedHeight(totalSaved, 0.5f, true)
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var goalAmountSavedTextView: TextView = itemView.findViewById(R.id.tv_goal_saved_amount)
    }
}