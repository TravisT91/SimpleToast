package com.engageft.fis.pscu.feature.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.apptoolbox.view.ProductCardView
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionsAdapter
import com.google.android.material.tabs.TabLayout
import io.reactivex.disposables.CompositeDisposable

class DashboardTransactionsAdapter(context: Context,
                                   private val compositeDisposable: CompositeDisposable,
                                   private val listener: DashboardTransactionsAdapterListener,
                                   transactionsListener: OnTransactionsAdapterListener?)
    : TransactionsAdapter(context, transactionsListener) {

    // Because there's a single row at position 0 for dashboard data,
    // must provide a custom paging callback here that accounts for that row
    // and ensures that paging updates happen in the right position.
    override fun getListUpdateCallback(): ListUpdateCallback {
        return object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                adapterCallback.onInserted(position + 1, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                adapterCallback.onRemoved(position + 1, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                adapterCallback.onMoved(fromPosition + 1, toPosition + 1)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                adapterCallback.onChanged(position + 1, count, payload)
            }
        }
    }

    private var headerViewHolder: DashboardHeaderViewHolder? = null

    lateinit var productCardView: ProductCardView
    var productCardModel: ProductCardModel? = null
        set(value) {
            field = value

            value?.let {
                headerViewHolder?.productCardView?.updateWithProductCardModel(it)
            }
        }

    var spendingBalanceAmount: CharSequence? = null
        set(value) {
            field = value

            value?.let {
                headerViewHolder?.spendingBalanceAmountTextView?.text = it
            }
        }

    var savingsBalanceAmount: CharSequence? = null
        set(value) {
            field = value

            value?.let {
                headerViewHolder?.savingsBalanceAmountTextView?.text = it
            }
        }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1 // add 1 for the dashboard header view
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_DASHBOARD_HEADER
        } else {
            super.getItemViewType(position - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DASHBOARD_HEADER -> DashboardHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_dashboard_header, parent, false))
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DashboardHeaderViewHolder) {
            headerViewHolder = holder

            productCardModel?.let {
                holder.productCardView.updateWithProductCardModel(it)
            }
            spendingBalanceAmount?.let {
                holder.spendingBalanceAmountTextView.text = it
            }
            savingsBalanceAmount?.let {
                holder.savingsBalanceAmountTextView.text = it
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }
    }

    fun setSavingsBalanceVisibility(visible: Boolean) {
        headerViewHolder?.apply {
            if (visible) {
                savingsBalanceAmountTextView.visibility = View.VISIBLE
                savingsBalanceLabelTextView.visibility = View.VISIBLE
            } else {
                savingsBalanceAmountTextView.visibility = View.GONE
                savingsBalanceLabelTextView.visibility = View.GONE
            }
        }
    }

    private inner class DashboardHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productCardView: ProductCardView = itemView.findViewById(R.id.productCardView)
        val expandButton: AppCompatImageButton = itemView.findViewById(R.id.btn_disclose_hide_card_actions)
        val spendingBalanceAmountTextView: TextView = itemView.findViewById(R.id.spendingBalanceAmount)
        val spendingBalanceLabelTextView: TextView = itemView.findViewById(R.id.spendingBalanceLabel)
        val savingsBalanceAmountTextView: TextView = itemView.findViewById(R.id.savingsBalanceAmount)
        val savingsBalanceLabelTextView: TextView = itemView.findViewById(R.id.savingsBalanceLabel)

        val transactionsTabLayout: TabLayout = itemView.findViewById(R.id.transactionsTabLayout)

        init {
            BrandingInfoRepo.cards?.get(0)?.let {
                productCardView.applyBranding(it, compositeDisposable, null)
            }

            expandButton.setOnClickListener { listener.onExpandClicked() }

            spendingBalanceAmountTextView.setOnClickListener { listener.onSpendingBalanceClicked() }
            spendingBalanceLabelTextView.setOnClickListener { listener.onSpendingBalanceClicked() }

            savingsBalanceAmountTextView.setOnClickListener { listener.onSavingsBalanceClicked() }
            savingsBalanceLabelTextView.setOnClickListener { listener.onSavingsBalanceClicked() }

            transactionsTabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // intentionally left blank
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // intentionally left blank
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (transactionsTabLayout.selectedTabPosition) {
                        TRANSACTIONS_TAB_POSITION_ALL -> {
                            listener.onAllActivityClicked()
                        }
                        TRANSACTIONS_TAB_POSITION_DEPOSITS -> {
                            listener.onDepositsClicked()
                        }
                    }
                }
            })
        }
    }

    interface DashboardTransactionsAdapterListener {
        fun onExpandClicked()
        fun onSpendingBalanceClicked()
        fun onSavingsBalanceClicked()
        fun onAllActivityClicked()
        fun onDepositsClicked()
    }

    companion object {
        const val VIEW_TYPE_DASHBOARD_HEADER = 100 // large value so not to conflict with view types defined in TransactionsAdapter parent class

        const val TRANSACTIONS_TAB_POSITION_ALL = 0
        const val TRANSACTIONS_TAB_POSITION_DEPOSITS = 1
    }
}