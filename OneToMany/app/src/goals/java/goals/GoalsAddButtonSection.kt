package goals

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class GoalsAddButtonSection(private val listener: OnButtonSectionListener) : StatelessSection(SectionParameters.builder().itemResourceId(R.layout.goals_add_button_item).build()) {

    private var button: Button? = null
    private var enabled = true

    override fun getContentItemsTotal(): Int {
        return 1
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        this.button = (holder as ViewHolder).button
        this.button!!.isEnabled = enabled
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var button: PillButton = itemView.findViewById(R.id.addGoalButton)

        init {
            button.setOnClickListener { listener.onButtonClicked() }
        }
    }

    interface OnButtonSectionListener {
        fun onButtonClicked()
    }
}