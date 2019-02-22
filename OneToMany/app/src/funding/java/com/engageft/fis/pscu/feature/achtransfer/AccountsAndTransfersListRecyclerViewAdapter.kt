package com.engageft.fis.pscu.feature.achtransfer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.secondaryusers.SecondaryUserListItem
import com.ob.ws.dom.utility.AchAccountInfo
import com.ob.ws.dom.utility.AchLoadInfo
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import java.util.Locale

/**
 * Created by Atia Hashimi 12/14/18
 * Refactored by Joey Hutchins 2/21/19
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountsAndTransfersListRecyclerViewAdapter(
        private val selectionListener: AccountsAndTransfersSelectionListener): RecyclerView.Adapter<AccountsAndTransfersListRecyclerViewAdapter.AccountsAndTransfersViewHolder>() {
    companion object {
        const val VIEW_TYPE_CARD_LOAD_HEADER = 0
        const val VIEW_TYPE_HEADER = 1
        const val VIEW_TYPE_BANK_ACCOUNT = 2
        const val VIEW_TYPE_ADD_ITEM = 3
        const val VIEW_TYPE_BANK_ACCOUNT_FOOTER = 4
        const val VIEW_TYPE_CREDIT_DEBIT = 5
        const val VIEW_TYPE_TRANSFER = 6
        const val VIEW_TYPE_CREATE_TRANSFER = 7
    }

    private var items = listOf<SecondaryUserListItem>()
    private val viewHolderListener = object : ViewHolderListener {
        override fun onViewSelected(position: Int) {
            selectionListener.onItemClicked(items[position])
        }
    }

    private val mutableList = mutableListOf<Any>()
    private var buttonText: String = ""
    private var shouldShowButton: Boolean = false

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountsAndTransfersViewHolder {
        return when (viewType) {
            VIEW_TYPE_CARD_LOAD_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_transfer_header_item, parent, false)
                AccountsAndTransfersViewHolder.CardLoadHeaderViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_header_item, parent, false)
                AccountsAndTransfersViewHolder.HeaderViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_BANK_ACCOUNT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_bank_item, parent, false)
                AccountsAndTransfersViewHolder.BankAccountViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_ADD_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_add_item, parent, false)
                AccountsAndTransfersViewHolder.AddItemViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_BANK_ACCOUNT_FOOTER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_verify_footer_item, parent, false)
                AccountsAndTransfersViewHolder.VerifyBankAccountFooterViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_CREDIT_DEBIT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_credit_debit_item, parent, false)
                AccountsAndTransfersViewHolder.HeaderViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_TRANSFER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_transfer_item, parent, false)
                AccountsAndTransfersViewHolder.ScheduledLoadViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_CREATE_TRANSFER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_create_transfer_item, parent, false)
                AccountsAndTransfersViewHolder.CreateTransferViewHolder(viewHolderListener, view)
            }
            else -> {
                throw IllegalStateException("Unknown view type")
            }
        }
    }

    override fun onBindViewHolder(holder: AccountsAndTransfersViewHolder, position: Int) {
        holder.itemPosition = position
        when (holder) {
            is AccountsAndTransfersViewHolder.CardLoadHeaderViewHolder -> {
                // Nothing to do.
            }
            is AccountsAndTransfersViewHolder.HeaderViewHolder -> {
                val item = items[position] as AccountsAndTransferListItem.HeaderItem
                when (item) {
                    is AccountsAndTransferListItem.HeaderItem.BankAccountHeaderItem -> {
                        holder.headerText.text = holder.itemView.context.getString(R.string.card_load_bank_account_header)
                    }
                    is AccountsAndTransferListItem.HeaderItem.CreditDebitHeaderItem -> {
                        holder.headerText.text = holder.itemView.context.getString(R.string.card_load_credit_debit_header)
                    }
                    is AccountsAndTransferListItem.HeaderItem.ScheduledLoadHeader -> {
                        holder.headerText.text = holder.itemView.context.getString(R.string.card_load_transfer_scheduled_header)
                    }
                    is AccountsAndTransferListItem.HeaderItem.RecentActivityHeaderItem -> {
                        holder.headerText.text = holder.itemView.context.getString(R.string.card_load_transfer_recent_activity_header)
                    }
                }
            }
            is AccountsAndTransfersViewHolder.BankAccountViewHolder -> {
                val item = items[position] as AccountsAndTransferListItem.BankAccountItem
                holder.apply {
                    bankNameText.text = item.bankName
                    if (item.verified) {
                        bankStatusText.text = holder.itemView.context.getString(R.string.ach_bank_transfer_status_verified)
                        bankStatusText.setTextColor(ContextCompat.getColor(holder.itemView.context!!, R.color.structure5))
                    } else {
                        bankStatusText.text = holder.itemView.context.getString(R.string.ach_bank_transfer_status_unverified)
                        bankStatusText.setTextColor(Palette.errorColor)
                    }
                }
                //                holder.apply {
//                    val achAccountInfo: AchAccountInfo = mutableList[position] as AchAccountInfo
//                    // check if item is empty placeholder achAccountInfo
//                    if (achAccountInfo.achAccountId == EMPTY_LIST_ACCOUNT_ID) {
//                        bankStatusTextView.visibility = View.GONE
//                        bankNameTextView.text = context.getString(R.string.ach_bank_transfer_add_bank)
//                        bankNameTextView.setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
//                    } else {
//                        bankNameTextView.text = AchAccountInfoUtils.accountDescriptionForDisplay(context, achAccountInfo)
//                        bankStatusTextView.text = when (achAccountInfo.achAccountStatus) {
//                            AchAccountStatus.VERIFIED -> context.getString(R.string.ach_bank_transfer_status_verified)
//                            else -> context.getString(R.string.ach_bank_transfer_status_unverified)
//                        }
//                    }
//
//                    holder.itemView.setOnClickListener {
//                        if (achAccountInfo.achAccountId == EMPTY_LIST_ACCOUNT_ID) {
//                            achAccountClickListener.onAddBankAccountClicked()
//                        } else {
//                            achAccountClickListener.onAchAccountDetailClicked(achAccountInfo.achAccountId)
//                        }
//                    }
//                }
            }
            is AccountsAndTransfersViewHolder.AddItemViewHolder -> {
                val item = items[position] as AccountsAndTransferListItem.AddItem
                when (item) {
                    is AccountsAndTransferListItem.AddItem.AddBankAccountItem -> {
                        holder.buttonText.text = holder.itemView.context.getString(R.string.card_load_button_add_bank)
                    }
                    is AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem -> {
                        holder.buttonText.text = holder.itemView.context.getString(R.string.card_load_button_add_credit_debit)
                    }
                }
            }
            is AccountsAndTransfersViewHolder.VerifyBankAccountFooterViewHolder -> {
                // Nothing to do.
            }
            is AccountsAndTransfersViewHolder.CreditDebitCardViewHolder -> {
                val item = items[position] as AccountsAndTransferListItem.CreditDebitCardItem
                // TODO(jhutchins):
            }
            is AccountsAndTransfersViewHolder.ScheduledLoadViewHolder -> {
                val item = items[position] as AccountsAndTransferListItem.TransferItem
                when (item) {
                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem -> {
                        val scheduledLoadItem = item as AccountsAndTransferListItem.TransferItem.ScheduledLoadItem
                        holder.apply {
                            dayTextView.text = DisplayDateTimeUtils.getDayTwoDigits(scheduledLoadItem.nextRunDate)
                            monthTextView.text = DisplayDateTimeUtils.getMonthAbbr(scheduledLoadItem.nextRunDate)
                            val transferType = if (scheduledLoadItem.isAccount) itemView.context.getString(R.string.card_load_transfer_from_account) else itemView.context.getString(R.string.card_load_transfer_from_card)
                            transferTextView.text = String.format(itemView.context.getString(R.string.card_load_transfer_scheduled_title_format), transferType, scheduledLoadItem.lastFour)
                            transferSubTextView.text = when (scheduledLoadItem) {
                                is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem -> {
                                    String.format(itemView.context.getString(R.string.TRANSFER_MONTHLY_SIMPLE_LOAD_DESCRIPTION),
                                            DisplayDateTimeUtils.getDayOrdinal(itemView.context, scheduledLoadItem.nextRunDate))
                                }
                                is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.TwiceMonthlyItem -> {
                                    String.format(itemView.context.getString(R.string.TRANSFER_TWICE_MONTHLY_SIMPLE_LOAD_DESCRIPTION),
                                            DisplayDateTimeUtils.getDayOrdinal(itemView.context, scheduledLoadItem.scheduleDate1),
                                            DisplayDateTimeUtils.getDayOrdinal(itemView.context, scheduledLoadItem.scheduleDate2))
                                }
                                is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.WeeklyItem -> {
                                    String.format(itemView.context.getString(R.string.TRANSFER_WEEKLY_SIMPLE_LOAD_DESCRIPTION),
                                            scheduledLoadItem.nextRunDate.dayOfWeek().getAsText(Locale.getDefault()))
                                }
                                is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.AltWeeklyItem -> {
                                    String.format(itemView.context.getString(R.string.TRANSFER_ALT_WEEKLY_SIMPLE_LOAD_DESCRIPTION),
                                            scheduledLoadItem.nextRunDate.dayOfWeek().getAsText(Locale.getDefault()))
                                }
                            }
                            amountTextView.text = formatAchIncomingBankTransferAmount(itemView.context, scheduledLoadItem.amount)
                        }
                    }
                    is AccountsAndTransferListItem.TransferItem.RecentActivityItem -> {
                        holder.apply {
                            dayTextView.text = DisplayDateTimeUtils.getDayTwoDigits(item.nextRunDate)
                            monthTextView.text = DisplayDateTimeUtils.getMonthAbbr(item.nextRunDate)
                            transferTextView.text = if (item.isAccount) itemView.context.getString(R.string.card_load_transfer_recent_bank) else itemView.context.getString(R.string.card_load_transfer_recent_card)

                            val transferType = if (item.isAccount) itemView.context.getString(R.string.card_load_transfer_from_account) else itemView.context.getString(R.string.card_load_transfer_from_card)
                            transferSubTextView.text = String.format(itemView.context.getString(R.string.card_load_transfer_scheduled_title_format), transferType, item.lastFour)
                        }
                    }
                }
            }
            is AccountsAndTransfersViewHolder.CreateTransferViewHolder -> {
                // Nothing to do.
            }
        }
    }

    fun setAccountsAndTransfersItems(items: List<AccountsAndTransferListItem>) {
//        val oldList = this.items
//        this.items = items
//        DiffUtil.calculateDiff(SecondaryUserListRecyclerViewAdapter.SecondaryUserDiffUtil(oldList, items)).dispatchUpdatesTo(this)
    }

//    fun setCreateTransferButtonState(text: String, showButton: Boolean) {
//        buttonText = text
//        shouldShowButton = showButton
//        notifyItemRangeChanged(mutableList.size, 1)
//    }
//
//    fun setAccountHeaderData(headerText: String, headerSubText: String) {
//        val oldList = mutableList.toList()
//        removeHeader()
//        val pair = HeaderTextPair(headerText, headerSubText)
//        mutableList.add(0, pair)
//        DiffUtil.calculateDiff(CustomDiffUtil(oldList, mutableList))
//                .dispatchUpdatesTo(this)
//    }
//
//    fun removeHeaderAndNotifyAdapter() {
//        val oldList = mutableList.toList()
//        removeHeader()
//        DiffUtil.calculateDiff(CustomDiffUtil(oldList, mutableList))
//                .dispatchUpdatesTo(this)
//    }
//
//    fun removeAchAccountSection() {
//        val oldList = mutableList.toList()
//        if (mutableList.size > 0) { // removes item if already in list
//            val first = mutableList[0]
//            if (first is AchAccountInfo) {
//                mutableList.removeAt(0)
//            }
//        }
//        DiffUtil.calculateDiff(CustomDiffUtil(oldList, mutableList))
//                .dispatchUpdatesTo(this)
//    }
//
//    fun setAchAccountData(accountInfoList: List<AchAccountInfo>) {
//        val oldList = mutableList.toList()
//        var hasHeader = false
//        if (mutableList.size > 0) { // checks if header exists
//            val first = mutableList[0]
//            if (first is HeaderTextPair) {
//                hasHeader = true
//            }
//        }
//        val beginPosition = if (hasHeader) 1 else 0
//        if (mutableList.size > beginPosition) { // removes items if already in list
//            mutableList.removeAll { it is AchAccountInfo }
//        }
//
//        // check if list is empty, which means an AchAccountInfo doesn't exist
//        if (accountInfoList.isEmpty()) {
//            val placeholder = AchAccountInfo()
//            placeholder.achAccountId = EMPTY_LIST_ACCOUNT_ID
//            mutableList.add(beginPosition, placeholder)
//        } else {
//            mutableList.addAll(beginPosition, accountInfoList)
//        }
//        DiffUtil.calculateDiff(CustomDiffUtil(oldList, mutableList))
//                .dispatchUpdatesTo(this)
//    }
//
//    fun setScheduledLoadData(header: String, scheduledLoadList: List<ScheduledLoad>) {
//        val oldList = mutableList.toList()
//        // removes pre-existing items
//        removeExistingWithLabelItem<ScheduledLoad>()
//
//        var insertPosition = mutableList.size
//        if (scheduledLoadList.isNotEmpty()) {
//            // adds new data. scheduledLoad is added after AchAccountInfo
//            mutableList.forEachIndexed { index, any ->
//                if (any is HeaderTextPair || any is AchAccountInfo) {
//                    insertPosition = index + 1
//                }
//            }
//            mutableList.add(insertPosition, header)
//            mutableList.addAll(insertPosition + 1 , scheduledLoadList)
//        }
//        DiffUtil.calculateDiff(CustomDiffUtil(oldList, mutableList))
//                .dispatchUpdatesTo(this)
//    }
//
//    fun setHistoricalLoadData(header: String, historicalLoadList: List<AchLoadInfo>) {
//        val oldList = mutableList.toList()
//        // removes pre-existing items
//        removeExistingWithLabelItem<AchLoadInfo>()
//
//        val insertPosition = mutableList.size
//
//        if (historicalLoadList.isNotEmpty()) {
//            mutableList.add(insertPosition, header)
//            // it's always added to the end of adapter list
//            mutableList.addAll(insertPosition + 1 , historicalLoadList)
//        }
//
//        DiffUtil.calculateDiff(CustomDiffUtil(oldList, mutableList))
//                .dispatchUpdatesTo(this)
//    }
//
//    private inline fun <reified T> removeExistingWithLabelItem() {
//        var existingTransferLoadBegin = -1
//        kotlin.run search@{ //label to exit loop early
//            mutableList.forEachIndexed { index, any ->
//                if (any is T) {
//                    if (existingTransferLoadBegin == -1) {
//                        existingTransferLoadBegin = index
//                        return@search
//                    }
//                }
//            }
//        }
//        mutableList.removeAll { it is T }
//
//        // remove old label section
//        kotlin.run search@{
//            if (existingTransferLoadBegin != -1) {
//                mutableList.forEachIndexed { index, any ->
//                    // label found is the above the list items being removed
//                    if (any is String && index < existingTransferLoadBegin) {
//                        mutableList.removeAt(existingTransferLoadBegin - 1)
//                        return@search
//                    }
//                }
//            }
//        }
//    }
//
//    private fun removeHeader() {
//        if (mutableList.size > 0) { // removes item if already in list
//            val first = mutableList[0]
//            if (first is HeaderTextPair) {
//                mutableList.removeAt(0)
//            }
//        }
//    }

    private fun formatAchIncomingBankTransferAmount(context: Context, amount: String): String {
        // TODO(aHashimi): when ACH out is supported this string format needs to change as well
        return String.format(context.getString(R.string.ach_bank_transfer_amount_incoming_format), amount)
    }

    sealed class AccountsAndTransfersViewHolder(private val viewHolderListener: ViewHolderListener, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemPosition = -1

        init {
            itemView.setOnClickListener {
                viewHolderListener.onViewSelected(itemPosition)
            }
        }

        class CardLoadHeaderViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView)
        class HeaderViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            val headerText: AppCompatTextView = itemView.findViewById(R.id.labelTextView)
        }
        class BankAccountViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            val bankNameText: AppCompatTextView = itemView.findViewById(R.id.bankNameTextView)
            val bankStatusText: AppCompatTextView = itemView.findViewById(R.id.bankStatusTextView)
        }
        class AddItemViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            val buttonText: AppCompatTextView = itemView.findViewById(R.id.labelText)
        }
        class VerifyBankAccountFooterViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView)
        class CreditDebitCardViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            val labelText: AppCompatTextView = itemView.findViewById(R.id.cardLabel)
        }
        class ScheduledLoadViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            val dayTextView: TextView = itemView.findViewById(R.id.dayDateTextView)
            val monthTextView: TextView = itemView.findViewById(R.id.monthDateTextView)
            val transferTextView: TextView = itemView.findViewById(R.id.transferTextView)
            val transferSubTextView: TextView = itemView.findViewById(R.id.transferSubTextView)
            val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        }
        class CreateTransferViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView)
    }

    private inner class HeaderTextPair(val headerText: String, val headerSubtext: String)

    class CustomDiffUtil(private val oldList: List<Any>, private val newList: List<Any>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // id are the same
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            when(oldItem) {
                is AchAccountInfo -> {
                    return newItem is AchAccountInfo && newItem.achAccountId == oldItem.achAccountId
                }
                is ScheduledLoad -> {
                    return newItem is ScheduledLoad && newItem.scheduledLoadId == oldItem.scheduledLoadId
                }
                is AchLoadInfo -> {
                    return newItem is AchLoadInfo && newItem.loadId == oldItem.loadId
                }
                is HeaderTextPair -> {
                    return newItem is HeaderTextPair && newItem.headerText == oldItem.headerText &&
                            newItem.headerSubtext == oldItem.headerSubtext
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
        fun onAchAccountDetailClicked(achAccountInfoId: Long)
        fun onAddBankAccountClicked()
    }

    interface CreateTransferButtonClickListener {
        //TODO(aHashimi): FOTM-113
        //TODO(aHashimi): supporting multiple ach accounts: which one's being verfied? https://engageft.atlassian.net/browse/FOTM-588
        fun onCreateTransferClicked()
    }

    interface AccountsAndTransfersSelectionListener {
        fun onItemClicked(secondaryUserListItem: SecondaryUserListItem)
    }

    interface ViewHolderListener {
        fun onViewSelected(position: Int)
    }
}

sealed class AccountsAndTransferListItem(val viewType: Int) {
    object CardLoadHeaderItem: AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_CARD_LOAD_HEADER)
    sealed class HeaderItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_HEADER) {
        object BankAccountHeaderItem: HeaderItem()
        object CreditDebitHeaderItem : HeaderItem()
        object ScheduledLoadHeader : HeaderItem()
        object RecentActivityHeaderItem : HeaderItem()
    }
    class BankAccountItem(val bankName: String, val verified: Boolean) : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_BANK_ACCOUNT)
    sealed class AddItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_ADD_ITEM) {
        object AddBankAccountItem : AddItem()
        object AddCreditDebitCardItem : AddItem()
    }
    sealed class TransferItem() : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_TRANSFER) {
        sealed class ScheduledLoadItem(val nextRunDate: DateTime, val lastFour: String, val isAccount: Boolean, val amount: String) : TransferItem() {
            class MonthlyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
            class TwiceMonthlyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String, val scheduleDate1: DateTime, val scheduleDate2: DateTime): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
            class WeeklyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
            class AltWeeklyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
        }
        class RecentActivityItem(val nextRunDate: DateTime, val lastFour: String, val isAccount: Boolean, val amount: String) : TransferItem()
    }
    object VerifyBankAccountFooterItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_BANK_ACCOUNT_FOOTER)
    class CreditDebitCardItem(val lastFour: String) : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_CREDIT_DEBIT)
    object CreateTransferItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_CREATE_TRANSFER)
}