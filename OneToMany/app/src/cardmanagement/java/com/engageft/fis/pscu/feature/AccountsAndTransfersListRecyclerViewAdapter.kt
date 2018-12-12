package com.engageft.fis.pscu.feature

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.AchLoadInfo
import com.ob.ws.dom.utility.AchAccountInfo
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.fis.pscu.generated.callback.OnClickListener
import com.ob.domain.lookup.AchAccountStatus
import org.joda.time.DateTime
import utilGen1.AchAccountInfoUtils
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils

//class AccountsAndTransfersListRecyclerViewAdapter(val accountClickListener: () -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
class AccountsAndTransfersListRecyclerViewAdapter(
        private val context: Context,
        val achAccountClickListener: AchAccountInfoClickListener,
        val scheduledTransferClickListener: ScheduledLoadListClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // todo: create different viewHolders to readability
    // todo: data binding with recyclerView?

    private companion object {
        const val TYPE_ACCOUNT = 0
        const val TYPE_SCHEDULED_LOAD = 1
        const val TYPE_HISTORICAL_LOAD = 2
        const val TYPE_LABEL = 3
        const val TYPE_ACH_ACCOUNT_HEADER = 4
        const val TYPE_BANK_ACCOUNT_NONE = 5

        const val EMPTY_LIST_ACCOUNT_ID: Long = -1
    }

    private val mutableList = mutableListOf<Any>()

    private val achAccountInfoList = mutableListOf<AchAccountInfo>()
//    private val historicalAccountInfoList = mutableListOf<ScheduledLoad>()
//    private val scheduledLoadList = mutableListOf<ScheduledLoad>()
//    private val historicalLoadList = mutableListOf<AchLoadInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ACH_ACCOUNT_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ach_bank_account_header_item, parent, false)
                AchAccountHeaderViewHolder(view)
            }
            TYPE_ACCOUNT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ach_bank_account_list_item, parent, false)
                BankAccountsViewHolder(view)
            }
            TYPE_SCHEDULED_LOAD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ach_bank_transfer_load_item, parent, false)
                ScheduledTransferViewHolder(view)
            }
            TYPE_HISTORICAL_LOAD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ach_bank_transfer_load_item, parent, false)
                HistoricalTransferViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ach_bank_transfer_label_item, parent, false)
                LabelSectionViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AchAccountHeaderViewHolder -> {
                holder.apply {
                    val headerTextPair = mutableList[position] as HeaderTextPair
                    titleTextView.text = headerTextPair.headerText
                    subtitleTextView.text = headerTextPair.headerSubtext
                }
            }
            is BankAccountsViewHolder -> {
                holder.apply {
                    val achAccountInfo: AchAccountInfo = mutableList[position] as AchAccountInfo
                    // check if item is empty placeholder achAccountInfo
                    if (achAccountInfo.achAccountId == EMPTY_LIST_ACCOUNT_ID) {
                        bankStatusTextView.visibility = View.GONE
                        bankNameTextView.text = context.getString(R.string.ach_bank_transfer_add_bank)
                        bankNameTextView.setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                    } else {
                        bankNameTextView.text = AchAccountInfoUtils.accountDescriptionForDisplay(context, achAccountInfo)
                        bankStatusTextView.text = when (achAccountInfo.achAccountStatus) {
                            AchAccountStatus.VERIFIED -> context.getString(R.string.ach_bank_transfer_status_verified)
                            else -> context.getString(R.string.ach_bank_transfer_status_unverified)
                        }
                    }

                    holder.itemView.setOnClickListener {
                        achAccountClickListener.onAchAccountInfoClicked(achAccountInfo.achAccountId)
                    }
                }
            }
            is LabelSectionViewHolder -> {
                holder.labelTextView.text = mutableList[position] as String
            }
            is ScheduledTransferViewHolder -> {
                holder.apply {
                    val scheduledLoad = mutableList[position] as ScheduledLoad
                    val scheduledDate = DateTime(scheduledLoad.isoNextRunDate)
                    dayTextView.text = DisplayDateTimeUtils.getDayTwoDigits(scheduledDate)
                    monthTextView.text = DisplayDateTimeUtils.getMonthAbbr(scheduledDate)

                    transferTextView.text = context.getString(R.string.ach_bank_transfer_recurring_transfer)
                    transferSubTextView.text = ScheduledLoadUtils.getTransferDetailSimpleText(context, scheduledLoad)
                    amountTextView.text = formatAchIncomingBankTransferAmount(context, scheduledLoad.amountOrig)

                    holder.itemView.setOnClickListener {
                        scheduledTransferClickListener.onScheduledTransferClicked(scheduledLoad.scheduledLoadId)
                    }
                }
            }
            is HistoricalTransferViewHolder -> {
                holder.apply {
                    val achLoadInfo = mutableList[position] as AchLoadInfo

                    val loadDate = DateTime(achLoadInfo.isoLoadDate)
                    dayTextView.text = DisplayDateTimeUtils.getDayTwoDigits(loadDate)
                    monthTextView.text = DisplayDateTimeUtils.getMonthAbbr(loadDate)

                    transferTextView.text = context.getString(R.string.ach_bank_transfer)
                    transferSubTextView.text = String.format(context.getString(R.string.ach_bank_transfer_from_format), achLoadInfo.achLastDigits)
                    amountTextView.text = formatAchIncomingBankTransferAmount(context, achLoadInfo.amount)
                }
            }
        }
    }

    fun setAccountHeaderData(headerText: String, headerSubText: String) {
        val oldList = mutableList.toList()
        if (mutableList.size > 0) { // removes item if already in list
            val first = mutableList[0]
            if (first is HeaderTextPair) {
                mutableList.removeAt(0)
            }
        }
        val pair = HeaderTextPair(headerText, headerSubText)
        mutableList.add(0, pair)
//        notifyItemRangeChanged(0, mutableList.size)
        DiffUtil.calculateDiff(DiffUtilImplementation(oldList, mutableList))
                .dispatchUpdatesTo(this)
    }

    fun setAchAccountData(accountInfoList: List<AchAccountInfo>) {
        val oldList = mutableList.toList()
        var hasHeader = false
        if (mutableList.size > 0) { // checks if header exists
            val first = mutableList[0]
            if (first is HeaderTextPair) {
                hasHeader = true
            }
        }
        val beginPosition = if (hasHeader) 1 else 0
        if (mutableList.size > beginPosition) { // removes items if already in list
            mutableList.removeAll { it is AchAccountInfo }
        }

        // check if list is empty, which means an AchAccountInfo doesn't exist
        if (accountInfoList.isEmpty()) {
            val placeholder = AchAccountInfo()
            placeholder.achAccountId = EMPTY_LIST_ACCOUNT_ID
            mutableList.add(beginPosition, placeholder)
        } else {
            mutableList.addAll(beginPosition, accountInfoList)
        }
//        notifyItemRangeChanged(beginPosition, accountInfoList.size)
        DiffUtil.calculateDiff(DiffUtilImplementation(oldList, mutableList))
                .dispatchUpdatesTo(this)
    }

    fun setScheduledLoadData(header: String, scheduledLoadList: List<ScheduledLoad>){
        // removes pre-existing items
        removeExistingWithHeader<ScheduledLoad>()

        var insertPosition = mutableList.size
        if (scheduledLoadList.isNotEmpty()) {
            // adds new data. scheduledLoad is added after AchAccountInfo
            mutableList.forEachIndexed { index, any ->
                if (any is HeaderTextPair || any is AchAccountInfo) {
                    insertPosition = index + 1
                }
            }
            mutableList.add(insertPosition, header)
            mutableList.addAll(insertPosition + 1 , scheduledLoadList)
        }
        notifyItemRangeChanged(insertPosition, scheduledLoadList.size)
    }

    fun setHistoricalLoadData(header: String, historicalLoadList: List<AchLoadInfo>){
        // removes pre-existing items
        removeExistingWithHeader<AchLoadInfo>()

        // it's always added to the end of adapter list
        val insertPosition = mutableList.size
        mutableList.add(insertPosition, header)
        mutableList.addAll(insertPosition + 1 , historicalLoadList)
        notifyItemRangeChanged(insertPosition, historicalLoadList.size)
    }

    private inline fun <reified T> removeExistingWithHeader() {
        var existingAchLoadInfoBegin = -1
        kotlin.run search@{
            //label to exit loop early
            mutableList.forEachIndexed { index, any ->
                if (any is T) {
                    if (existingAchLoadInfoBegin == -1) {
                        existingAchLoadInfoBegin = index
                        return@search
                    }
                }
            }
        }
        mutableList.removeAll { it is T }
        if (existingAchLoadInfoBegin != -1) { // remove old header
            mutableList.removeAt(existingAchLoadInfoBegin - 1)
        }
    }

    override fun getItemCount(): Int {
        return mutableList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(mutableList[position]) {
            is HeaderTextPair -> TYPE_ACH_ACCOUNT_HEADER
            is AchAccountInfo -> TYPE_ACCOUNT
            is ScheduledLoad -> TYPE_SCHEDULED_LOAD
            is AchLoadInfo -> TYPE_HISTORICAL_LOAD
            else -> TYPE_LABEL
        }
    }

    private fun formatAchIncomingBankTransferAmount(context: Context, amount: String): String {
        // TODO(aHashimi): when ACH out is supported this string format needs to change as well
        return String.format(context.getString(R.string.ach_bank_transfer_amount_incoming_format), amount)
    }

    private class AchAccountHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val subtitleTextView: TextView = itemView.findViewById(R.id.subtitleTextView)
    }

    private class LabelSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
    }

    private class BankAccountsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bankNameTextView: TextView = itemView.findViewById(R.id.bankNameTextView)
        val bankStatusTextView: TextView = itemView.findViewById(R.id.bankStatusTextView)
    }

    private class ScheduledTransferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.dayDateTextView)
        val monthTextView: TextView = itemView.findViewById(R.id.monthDateTextView)
        val transferTextView: TextView = itemView.findViewById(R.id.transferTextView)
        val transferSubTextView: TextView = itemView.findViewById(R.id.transferSubTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    }

    private class HistoricalTransferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.dayDateTextView)
        val monthTextView: TextView = itemView.findViewById(R.id.monthDateTextView)
        val transferTextView: TextView = itemView.findViewById(R.id.transferTextView)
        val transferSubTextView: TextView = itemView.findViewById(R.id.transferSubTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    }

    private inner class HeaderTextPair(val headerText: String, val headerSubtext: String)

    class DiffUtilImplementation(val oldList: List<Any>, val newList: List<Any>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // id are the same
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            when(oldItem) {
                is AchAccountInfo -> {
                    return newItem is AchAccountInfo && newItem.accountId == oldItem.accountId
                }
                is String -> {
                    return oldItem == newItem
                }
                else -> {
                    return oldItem == newItem
                }
            }
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // if IDs are same have contents changed
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }

    interface ScheduledLoadListClickListener {
        //TODO(aHashimi): FOTM-113, should pass object's ID?
        fun onScheduledTransferClicked(scheduledLoadInfoId: Long)
    }

    interface AchAccountInfoClickListener {
        //TODO(aHashimi): FOTM-65, should pass object's ID?
        fun onAchAccountInfoClicked(achAccountInfoId: Long)
    }
}