package com.engageft.fis.pscu.feature.secondaryusers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.utils.CardStatusUtils

/**
 * Created by joeyhutchins on 1/15/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SecondaryUserListRecyclerViewAdapter(private val selectionListener: SecondaryUserListSelectionListener)
    : RecyclerView.Adapter<SecondaryUserListRecyclerViewAdapter.SecondaryUserViewHolder>() {
    
    companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_ADD_USER = 1
        const val VIEW_TYPE_CARD_HEADER = 2
        const val VIEW_TYPE_CARD_FOOTER = 3
    }

    private var items = listOf<SecondaryUserListItem>()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecondaryUserViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_user_item, parent, false)
                SecondaryUserViewHolder.ActiveUserViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_ADD_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_add_item, parent, false)
                SecondaryUserViewHolder.AddUserViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_CARD_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_card_header_item, parent, false)
                SecondaryUserViewHolder.CardHeaderViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_CARD_FOOTER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_card_footer_item, parent, false)
                SecondaryUserViewHolder.CardFooterViewHolder(viewHolderListener, view)
            }
            else -> {
                throw IllegalStateException("Unknown view type")
            }
        }
    }

    override fun onBindViewHolder(holder: SecondaryUserViewHolder, position: Int) {
        when (holder) {
            is SecondaryUserViewHolder.ActiveUserViewHolder -> {
                val item = items[position] as SecondaryUserListItem.ActiveSecondaryUserType
                holder.userNameTextView.text = item.name
                holder.statusTextView.text = CardStatusUtils.cardStatusStringForProductCardModelCardStatus(holder.itemView.context, item.cardStatus)
                if (item.cardStatus != ProductCardModelCardStatus.CARD_STATUS_ACTIVE) {
                    holder.statusTextView.setTextColor(Palette.errorColor)
                } else {
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context!!, R.color.structure5))
                }
            }
            is SecondaryUserViewHolder.AddUserViewHolder -> {
                // Nothing to do here.
            }
            is SecondaryUserViewHolder.CardHeaderViewHolder -> {
                val item = items[position] as SecondaryUserListItem.CardHeaderType
                holder.cardTitleText.text = item.cardDisplayName
            }
            is SecondaryUserViewHolder.CardFooterViewHolder -> {
                val item = items[position] as SecondaryUserListItem.CardFooterType
                val pluralString = if (item.cardUserLimit == 0) {
                    holder.itemView.context.getString(R.string.secondary_users_user_limit_footer_zero)
                } else {
                    holder.itemView.context.resources.getQuantityString(R.plurals.secondary_users_user_limit_footer_plural, item.cardUserLimit, item.cardUserLimit)
                }
                holder.userLimitText.text = String.format(holder.itemView.context.getString(R.string.secondary_users_user_limit_footer_format), pluralString)
            }
        }
    }

    fun setSecondaryUserItems(items: List<SecondaryUserListItem>) {
        val oldList = this.items
        this.items = items
        DiffUtil.calculateDiff(SecondaryUserDiffUtil(oldList, items)).dispatchUpdatesTo(this)
    }

    sealed class SecondaryUserViewHolder(private val viewHolderListener: ViewHolderListener, itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                viewHolderListener.onViewSelected(adapterPosition)
            }
        }

        class ActiveUserViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView) {
            val userNameTextView: AppCompatTextView = itemView.findViewById(R.id.userNameText)
            val statusTextView: AppCompatTextView = itemView.findViewById(R.id.statusText)
        }
        class AddUserViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView)
        class CardHeaderViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView) {
            val cardTitleText: AppCompatTextView = itemView.findViewById(R.id.cardTitleText)
        }
        class CardFooterViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView) {
            val userLimitText: AppCompatTextView = itemView.findViewById(R.id.userLimitText)
        }
    }

    private class SecondaryUserDiffUtil(private val oldList: List<SecondaryUserListItem>, private val newList: List<SecondaryUserListItem>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem.viewType != newItem.viewType) {
                return false
            } else {
                // if their viewTypes are the same, we can safely assume their child classes are the same.
                // We can cast:
                when (oldItem) {
                    is SecondaryUserListItem.ActiveSecondaryUserType -> {
                        newItem as SecondaryUserListItem.ActiveSecondaryUserType
                        return oldItem.name == newItem.name && oldItem.cardStatus == newItem.cardStatus
                    }
                    is SecondaryUserListItem.AddUserType -> {
                        return true
                    }
                    is SecondaryUserListItem.CardHeaderType -> {
                        return true
                    }
                    is SecondaryUserListItem.CardFooterType -> {
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
                when (oldItem) {
                    is SecondaryUserListItem.ActiveSecondaryUserType -> {
                        newItem as SecondaryUserListItem.ActiveSecondaryUserType
                        return oldItem.name == newItem.name && oldItem.cardStatus == newItem.cardStatus
                    }
                    is SecondaryUserListItem.AddUserType -> {
                        return true
                    }
                    is SecondaryUserListItem.CardHeaderType -> {
                        return true
                    }
                    is SecondaryUserListItem.CardFooterType -> {
                        return true
                    }
                    else -> {
                        throw IllegalStateException("Invalid list type")
                    }
                }
            }
        }

    }

    interface SecondaryUserListSelectionListener {
        fun onItemClicked(secondaryUserListItem: SecondaryUserListItem)
    }

    interface ViewHolderListener {
        fun onViewSelected(position: Int)
    }
}