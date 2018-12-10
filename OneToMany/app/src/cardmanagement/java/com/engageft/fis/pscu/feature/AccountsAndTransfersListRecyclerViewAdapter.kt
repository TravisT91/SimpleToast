package com.engageft.fis.pscu.feature

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.TextView
import com.engageft.fis.pscu.R
import com.ob.ws.dom.utility.AchLoadInfo
import com.ob.ws.dom.utility.AchAccountInfo
import com.engageft.engagekit.model.ScheduledLoad
import com.ob.domain.lookup.AchAccountStatus
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils

//class AccountsAndTransfersListRecyclerViewAdapter(val accountClickListener: () -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
class AccountsAndTransfersListRecyclerViewAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // todo: create different viewHolders to readability
    // todo: data binding with recyclerView?

    private companion object {
        const val TYPE_ACCOUNT = 0
        const val TYPE_SCHEDULED_LOAD = 1
        const val TYPE_HISTORICAL_LOAD = 2
        const val TYPE_LABEL = 3
        const val TYPE_ACH_ACCOUNT_HEADER = 4
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
                    bankNameTextView.text = achAccountInfo.bankName
                    bankStatusTextView.text = when (achAccountInfo.achAccountStatus) {
                        AchAccountStatus.VERIFIED -> "Verified"
                        AchAccountStatus.REMOVED -> "Removed"
                        else -> "Unverified"
                    }
                }
            }
            is LabelSectionViewHolder -> {
                val test: String = mutableList[position] as String
                holder.labelTextView.text = test
            }
            is ScheduledTransferViewHolder -> {
                holder.apply {
                    val scheduledLoad = mutableList[position] as ScheduledLoad
                    transferTextView.text = "Recurring Transfers"
                    val date1 = DateTime(scheduledLoad.scheduleDate).dayOfMonth
                    val date2 = DateTime(scheduledLoad.scheduleDate2).dayOfMonth
                    transferSubTextView.text = DisplayDateTimeUtils.getOrdinal(context, date1) + " and " + DisplayDateTimeUtils.getOrdinal(context, date2)+ " " + ScheduledLoad.getTypeStringFromType(scheduledLoad.type)
                }
            }
            is HistoricalTransferViewHolder -> {
                holder.apply {
                    val achLoadInfo = mutableList[position] as AchLoadInfo
                    transferTextView.text = "Bank Transfer"
                    transferSubTextView.text = "From account..." + achLoadInfo.achLastDigits
                }
            }
        }
    }

    fun setAccountHeaderData(headerText: String, headerSubText: String) {
        if (mutableList.size > 0) { // removes item if already in list
            val first = mutableList[0]
            if (first is HeaderTextPair) {
                mutableList.removeAt(0)
            }
        }
        val pair = HeaderTextPair(headerText, headerSubText)
        mutableList.add(0, pair)
        notifyItemRangeChanged(0, mutableList.size)
    }

    fun setAccountData(accountInfoList: List<AchAccountInfo>) {
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
        mutableList.addAll(beginPosition, accountInfoList)
        notifyItemRangeChanged(beginPosition, accountInfoList.size)

    }

    fun setScheduledLoadData(header: String, scheduledLoadList: List<ScheduledLoad>){
        // removes pre-existing items
        removeExistingWithHeader<ScheduledLoad>()

        // adds new data
        var insertPosition = 0
        mutableList.forEachIndexed { index, any ->
            if (any is HeaderTextPair || any is AchAccountInfo) {
                insertPosition = index + 1
            }
        }
        mutableList.add(insertPosition, header)
        mutableList.addAll(insertPosition + 1 , scheduledLoadList)
        notifyItemRangeChanged(insertPosition, scheduledLoadList.size)
    }

    fun setHistoricalLoadData(header: String, historicalLoadList: List<AchLoadInfo>){
        // TODO: remove existing items
        removeExistingWithHeader<AchLoadInfo>()
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
        // todo: count = addition of all lists
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
        val transferTextView: TextView = itemView.findViewById(R.id.transferTextView)
        val transferSubTextView: TextView = itemView.findViewById(R.id.transferSubTextView)
    }

    private class HistoricalTransferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transferTextView: TextView = itemView.findViewById(R.id.transferTextView)
        val transferSubTextView: TextView = itemView.findViewById(R.id.transferSubTextView)
    }

    fun List<AchAccountInfo>.shouldDisplayHeader(): Boolean =
            achAccountInfoList.isEmpty() || achAccountInfoList[0].achAccountStatus != AchAccountStatus.VERIFIED

    private class HeaderTextPair(val headerText: String, val headerSubtext: String)
}