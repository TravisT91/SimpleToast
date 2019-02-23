package com.engageft.fis.pscu.feature.achtransfer

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
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

    private var items = listOf<AccountsAndTransferListItem>()

    private val viewHolderListener = object : ViewHolderListener {
        override fun onViewSelected(position: Int) {
            selectionListener.onItemClicked(items[position])
        }
    }

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
                AccountsAndTransfersViewHolder.CreditDebitCardViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_TRANSFER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_load_transfer_item, parent, false)
                AccountsAndTransfersViewHolder.CardLoadViewHolder(viewHolderListener, view)
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
                    bankNameText.text = String.format(itemView.context.getString(R.string.card_load_bank_account_title_format), item.bankName, item.lastFour)
                    if (item.verified) {
                        bankStatusText.text = holder.itemView.context.getString(R.string.ach_bank_transfer_status_verified)
                        bankStatusText.setTextColor(ContextCompat.getColor(holder.itemView.context!!, R.color.structure5))
                    } else {
                        bankStatusText.text = holder.itemView.context.getString(R.string.ach_bank_transfer_status_unverified)
                        bankStatusText.setTextColor(Palette.errorColor)
                    }
                }
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
                holder.apply {
                    labelText.text = String.format(itemView.context.getString(R.string.card_load_credit_debit_title_format), item.lastFour)
                }
            }
            is AccountsAndTransfersViewHolder.CardLoadViewHolder -> {
                val item = items[position] as AccountsAndTransferListItem.TransferItem
                when (item) {
                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem -> {
                        val scheduledLoadItem = item as AccountsAndTransferListItem.TransferItem.ScheduledLoadItem
                        holder.apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                itemView.foreground = ContextCompat.getDrawable(itemView.context, R.drawable.selectable_item_background_structure2)
                            }
                            dayTextView.text = DisplayDateTimeUtils.getDayTwoDigits(scheduledLoadItem.nextRunDate)
                            monthTextView.text = DisplayDateTimeUtils.getMonthAbbr(scheduledLoadItem.nextRunDate)
                            val transferType = if (scheduledLoadItem.isAccount) itemView.context.getString(R.string.card_load_transfer_from_account) else itemView.context.getString(R.string.card_load_transfer_from_card)
                            transferTextView.text = String.format(itemView.context.getString(R.string.card_load_transfer_scheduled_title_format), transferType, scheduledLoadItem.lastFour)
                            transferSubTextView.text = when (scheduledLoadItem) {
                                is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem -> {
                                    String.format(itemView.context.getString(R.string.TRANSFER_MONTHLY_SIMPLE_LOAD_DESCRIPTION),
                                            DisplayDateTimeUtils.getDayOrdinal(itemView.context, scheduledLoadItem.nextRunDate))
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                itemView.foreground = null
                            }
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
        val oldList = this.items
        this.items = items
        DiffUtil.calculateDiff(AccountsAndTransfersDiffUtil(oldList, items)).dispatchUpdatesTo(this)
    }

    private fun formatAchIncomingBankTransferAmount(context: Context, amount: String): String {
        // TODO(aHashimi): when ACH out is supported this string format needs to change as well
        return String.format(context.getString(R.string.ach_bank_transfer_amount_incoming_format), amount)
    }

    sealed class AccountsAndTransfersViewHolder(private val viewHolderListener: ViewHolderListener, itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                viewHolderListener.onViewSelected(adapterPosition)
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
        class CardLoadViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            val dayTextView: TextView = itemView.findViewById(R.id.dayDateTextView)
            val monthTextView: TextView = itemView.findViewById(R.id.monthDateTextView)
            val transferTextView: TextView = itemView.findViewById(R.id.transferTextView)
            val transferSubTextView: TextView = itemView.findViewById(R.id.transferSubTextView)
            val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        }
        class CreateTransferViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : AccountsAndTransfersViewHolder(viewHolderListener, itemView) {
            init {
                itemView.setOnClickListener(null)
                itemView.findViewById<PillButton>(R.id.createTransferButton).setOnClickListener {
                    viewHolderListener.onViewSelected(adapterPosition)
                }
            }
        }
    }

    private class AccountsAndTransfersDiffUtil(private val oldList: List<AccountsAndTransferListItem>, private val newList: List<AccountsAndTransferListItem>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem.viewType != newItem.viewType) {
                return false
            } else {
                // if their viewTypes are the same, we can safely assume their child classes are the same.
                // We can cast:
                return when (oldItem) {
                    is AccountsAndTransferListItem.CardLoadHeaderItem -> {
                        return true
                    }
                    is AccountsAndTransferListItem.HeaderItem -> {
                        return when (oldItem) {
                            is AccountsAndTransferListItem.HeaderItem.BankAccountHeaderItem -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.BankAccountHeaderItem
                            }
                            is AccountsAndTransferListItem.HeaderItem.CreditDebitHeaderItem -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.CreditDebitHeaderItem
                            }
                            is AccountsAndTransferListItem.HeaderItem.ScheduledLoadHeader -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.ScheduledLoadHeader
                            }
                            is AccountsAndTransferListItem.HeaderItem.RecentActivityHeaderItem -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.RecentActivityHeaderItem
                            }
                        }
                    }
                    is AccountsAndTransferListItem.BankAccountItem -> {
                        newItem as AccountsAndTransferListItem.BankAccountItem
                        return oldItem.bankName == newItem.bankName && oldItem.lastFour == newItem.lastFour && oldItem.verified == newItem.verified
                    }
                    is AccountsAndTransferListItem.AddItem -> {
                        return when (oldItem) {
                            is AccountsAndTransferListItem.AddItem.AddBankAccountItem -> {
                                newItem is AccountsAndTransferListItem.AddItem.AddBankAccountItem
                            }
                            is AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem -> {
                                newItem is AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem
                            }
                        }
                    }
                    is AccountsAndTransferListItem.TransferItem -> {
                        return when (oldItem) {
                            is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem -> {
                                return when (oldItem) {
                                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem -> {
                                        if (newItem is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem) {
                                            oldItem.lastFour == newItem.lastFour && oldItem.nextRunDate == newItem.nextRunDate &&
                                                    oldItem.amount == newItem.amount && oldItem.isAccount == newItem.isAccount
                                        } else {
                                            false
                                        }
                                    }
                                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.WeeklyItem -> {
                                        if (newItem is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.WeeklyItem) {
                                            oldItem.lastFour == newItem.lastFour && oldItem.nextRunDate == newItem.nextRunDate &&
                                                    oldItem.amount == newItem.amount && oldItem.isAccount == newItem.isAccount
                                        } else {
                                            false
                                        }
                                    }
                                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.AltWeeklyItem -> {
                                        if (newItem is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.AltWeeklyItem) {
                                            oldItem.lastFour == newItem.lastFour && oldItem.nextRunDate == newItem.nextRunDate &&
                                                    oldItem.amount == newItem.amount && oldItem.isAccount == newItem.isAccount
                                        } else {
                                            false
                                        }
                                    }
                                }
                            }
                            is AccountsAndTransferListItem.TransferItem.RecentActivityItem -> {
                                if (newItem is AccountsAndTransferListItem.TransferItem.RecentActivityItem) {
                                    oldItem.nextRunDate == newItem.nextRunDate && oldItem.lastFour == newItem.lastFour &&
                                            oldItem.isAccount == newItem.isAccount && oldItem.amount == newItem.amount
                                } else {
                                    false
                                }
                            }
                        }
                    }
                    is AccountsAndTransferListItem.VerifyBankAccountFooterItem -> {
                        return true
                    }
                    is AccountsAndTransferListItem.CreditDebitCardItem -> {
                        newItem as AccountsAndTransferListItem.CreditDebitCardItem
                        return oldItem.lastFour == newItem.lastFour
                    }
                    is AccountsAndTransferListItem.CreateTransferItem -> {
                        return true
                    }
                    else -> {
                        throw IllegalStateException("Invalid list type")
                    }
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
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem.viewType != newItem.viewType) {
                return false
            } else {
                // if their viewTypes are the same, we can safely assume their child classes are the same.
                // We can cast:
                return when (oldItem) {
                    is AccountsAndTransferListItem.CardLoadHeaderItem -> {
                        return true
                    }
                    is AccountsAndTransferListItem.HeaderItem -> {
                        return when (oldItem) {
                            is AccountsAndTransferListItem.HeaderItem.BankAccountHeaderItem -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.BankAccountHeaderItem
                            }
                            is AccountsAndTransferListItem.HeaderItem.CreditDebitHeaderItem -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.CreditDebitHeaderItem
                            }
                            is AccountsAndTransferListItem.HeaderItem.ScheduledLoadHeader -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.ScheduledLoadHeader
                            }
                            is AccountsAndTransferListItem.HeaderItem.RecentActivityHeaderItem -> {
                                newItem is AccountsAndTransferListItem.HeaderItem.RecentActivityHeaderItem
                            }
                        }
                    }
                    is AccountsAndTransferListItem.BankAccountItem -> {
                        newItem as AccountsAndTransferListItem.BankAccountItem
                        return oldItem.bankName == newItem.bankName && oldItem.lastFour == newItem.lastFour && oldItem.verified == newItem.verified
                    }
                    is AccountsAndTransferListItem.AddItem -> {
                        return when (oldItem) {
                            is AccountsAndTransferListItem.AddItem.AddBankAccountItem -> {
                                newItem is AccountsAndTransferListItem.AddItem.AddBankAccountItem
                            }
                            is AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem -> {
                                newItem is AccountsAndTransferListItem.AddItem.AddCreditDebitCardItem
                            }
                        }
                    }
                    is AccountsAndTransferListItem.TransferItem -> {
                        return when (oldItem) {
                            is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem -> {
                                return when (oldItem) {
                                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem -> {
                                        if (newItem is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.MonthlyItem) {
                                            oldItem.lastFour == newItem.lastFour && oldItem.nextRunDate == newItem.nextRunDate &&
                                                    oldItem.amount == newItem.amount && oldItem.isAccount == newItem.isAccount
                                        } else {
                                            false
                                        }
                                    }
                                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.WeeklyItem -> {
                                        if (newItem is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.WeeklyItem) {
                                            oldItem.lastFour == newItem.lastFour && oldItem.nextRunDate == newItem.nextRunDate &&
                                                    oldItem.amount == newItem.amount && oldItem.isAccount == newItem.isAccount
                                        } else {
                                            false
                                        }
                                    }
                                    is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.AltWeeklyItem -> {
                                        if (newItem is AccountsAndTransferListItem.TransferItem.ScheduledLoadItem.AltWeeklyItem) {
                                            oldItem.lastFour == newItem.lastFour && oldItem.nextRunDate == newItem.nextRunDate &&
                                                    oldItem.amount == newItem.amount && oldItem.isAccount == newItem.isAccount
                                        } else {
                                            false
                                        }
                                    }
                                }
                            }
                            is AccountsAndTransferListItem.TransferItem.RecentActivityItem -> {
                                if (newItem is AccountsAndTransferListItem.TransferItem.RecentActivityItem) {
                                    oldItem.nextRunDate == newItem.nextRunDate && oldItem.lastFour == newItem.lastFour &&
                                            oldItem.isAccount == newItem.isAccount && oldItem.amount == newItem.amount
                                } else {
                                    false
                                }
                            }
                        }
                    }
                    is AccountsAndTransferListItem.VerifyBankAccountFooterItem -> {
                        return true
                    }
                    is AccountsAndTransferListItem.CreditDebitCardItem -> {
                        newItem as AccountsAndTransferListItem.CreditDebitCardItem
                        return oldItem.lastFour == newItem.lastFour
                    }
                    is AccountsAndTransferListItem.CreateTransferItem -> {
                        return true
                    }
                    else -> {
                        throw IllegalStateException("Invalid list type")
                    }
                }
            }
        }

    }

    interface AccountsAndTransfersSelectionListener {
        fun onItemClicked(secondaryUserListItem: AccountsAndTransferListItem)
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
    class BankAccountItem(val bankName: String, val lastFour: String, val verified: Boolean, val achAccountId: Long) : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_BANK_ACCOUNT)
    sealed class AddItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_ADD_ITEM) {
        object AddBankAccountItem : AddItem()
        object AddCreditDebitCardItem : AddItem()
    }
    sealed class TransferItem() : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_TRANSFER) {
        sealed class ScheduledLoadItem(val nextRunDate: DateTime, val lastFour: String, val isAccount: Boolean, val amount: String) : TransferItem() {
            class MonthlyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
            class WeeklyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
            class AltWeeklyItem(nextRunDate: DateTime, lastFour: String, isAccount: Boolean, amount: String): ScheduledLoadItem(nextRunDate, lastFour, isAccount, amount)
        }
        class RecentActivityItem(val nextRunDate: DateTime, val lastFour: String, val isAccount: Boolean, val amount: String) : TransferItem()
    }
    object VerifyBankAccountFooterItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_BANK_ACCOUNT_FOOTER)
    class CreditDebitCardItem(val lastFour: String) : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_CREDIT_DEBIT)
    object CreateTransferItem : AccountsAndTransferListItem(AccountsAndTransfersListRecyclerViewAdapter.VIEW_TYPE_CREATE_TRANSFER)
}