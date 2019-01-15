package com.engageft.fis.pscu.feature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.engageft.fis.pscu.R

/**
 * Created by joeyhutchins on 1/15/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SecondaryUserListRecyclerViewAdapter(private val selectionListener: SecondaryUserListSelectionListener)
    : RecyclerView.Adapter<SecondaryUserListRecyclerViewAdapter.SecondaryUserViewHolder>() {
    
    private companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_INACTIVE_USER = 1
        const val VIEW_TYPE_ADD_USER = 2
    }

    sealed class SecondaryUserListItem(val viewType: Int) {
        class ActiveSecondaryUserType(val name: CharSequence, val lastFour: CharSequence) : SecondaryUserListItem(VIEW_TYPE_USER)
        class InactiveSecondaryUserType(val name: CharSequence) : SecondaryUserListItem(VIEW_TYPE_INACTIVE_USER)
        class AddUserType : SecondaryUserListItem(VIEW_TYPE_ADD_USER)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecondaryUserListRecyclerViewAdapter.SecondaryUserViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_active_item, parent, false)
                SecondaryUserViewHolder.ActiveUserViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_INACTIVE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_inactive_item, parent, false)
                SecondaryUserViewHolder.InactiveUserViewHolder(viewHolderListener, view)
            }
            VIEW_TYPE_ADD_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.secondary_user_add_item, parent, false)
                SecondaryUserViewHolder.AddUserViewHolder(viewHolderListener, view)
            }
            else -> {
                throw IllegalStateException("Unknown view type")
            }
        }
    }

    override fun onBindViewHolder(holder: SecondaryUserListRecyclerViewAdapter.SecondaryUserViewHolder, position: Int) {
        holder.itemPosition = position
        when (holder) {
            is SecondaryUserViewHolder.ActiveUserViewHolder -> {
                val item = items[position] as SecondaryUserListItem.ActiveSecondaryUserType
                holder.userNameTextView.text = item.name
                holder.lastFourTextView.text = item.lastFour
            }
            is SecondaryUserViewHolder.InactiveUserViewHolder -> {
                val item = items[position] as SecondaryUserListItem.InactiveSecondaryUserType
                holder.userNameTextView.text = item.name
            }
            is SecondaryUserViewHolder.AddUserViewHolder -> {
                // Nothing to do here.
            }
        }
    }

    fun setSecondaryUserItems(items: List<SecondaryUserListItem>) {
        val oldList = items
        this.items = items
        DiffUtil.calculateDiff(CustomDiffUtil(oldList, items)).dispatchUpdatesTo(this)
    }

    sealed class SecondaryUserViewHolder(private val viewHolderListener: ViewHolderListener, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemPosition = -1

        init {
            itemView.setOnClickListener {
                viewHolderListener.onViewSelected(itemPosition)
            }
        }

        class ActiveUserViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView) {
            val userNameTextView: AppCompatTextView = itemView.findViewById(R.id.userNameText)
            val lastFourTextView: AppCompatTextView = itemView.findViewById(R.id.lastFourText)
        }
        class InactiveUserViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView) {
            val userNameTextView: AppCompatTextView = itemView.findViewById(R.id.userNameText)
        }
        class AddUserViewHolder(viewHolderListener: ViewHolderListener, itemView: View) : SecondaryUserViewHolder(viewHolderListener, itemView)
    }

    class CustomDiffUtil(private val oldList: List<SecondaryUserListItem>, private val newList: List<SecondaryUserListItem>) : DiffUtil.Callback() {
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
                        return oldItem.name == newItem.name && oldItem.lastFour == newItem.lastFour
                    }
                    is SecondaryUserListItem.InactiveSecondaryUserType -> {
                        newItem as SecondaryUserListItem.InactiveSecondaryUserType
                        return oldItem.name == newItem.name
                    }
                    is SecondaryUserListItem.AddUserType -> {
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
                        return oldItem.name == newItem.name && oldItem.lastFour == newItem.lastFour
                    }
                    is SecondaryUserListItem.InactiveSecondaryUserType -> {
                        newItem as SecondaryUserListItem.InactiveSecondaryUserType
                        return oldItem.name == newItem.name
                    }
                    is SecondaryUserListItem.AddUserType -> {
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