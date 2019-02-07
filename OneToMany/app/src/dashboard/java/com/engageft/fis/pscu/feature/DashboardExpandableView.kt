package com.engageft.fis.pscu.feature

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.engageft.apptoolbox.BaseActivity
import com.engageft.apptoolbox.view.ListBottomSheetDialogFragment
import com.engageft.apptoolbox.view.ProductCardView
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.R.style.DashboardMoreOptionsBottomSheetDialogFragmentStyle


/**
 *  DashboardExpandableView
 *  </p>
 *  Manages Dashboard card view and action items list.
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class DashboardExpandableView : ConstraintLayout {

    private lateinit var cardAndActionsView: ViewGroup
    lateinit var cardView: ProductCardView
        private set
    private lateinit var actionsView: View
    private lateinit var expandCollapseButton: AppCompatImageButton
    private lateinit var transparentBarBelowCardView: View
    private lateinit var shadowAboveTransparentBar: View

    var showingActions = false
        private set

    private var cardAndActionsViewCollapsedHeight = 0
    private var cardAndActionsViewExpandedHeight = 0
    private var cardViewTopMarginCollapsed = 0
    private var cardViewTopMarginExpanded = 0
    var animationDurationMs = 0L
        private set
    private var cardViewAnimationDelayMs = 0L
    private var buttonRotationAnimationDurationMs = 0L

    var listener: DashboardExpandableViewListener? = null

    constructor(context: Context) : super(context) {
        initializeViews()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initializeViews()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        initializeViews()
    }

    companion object {
        const val MORE_CARD_OPTIONS_DIALOG_TAG = "MORE_CARD_OPTIONS_DIALOG_TAG"
    }

    // Must save expanded/collapsed state so that it is shown correctly after returning to Dashboard from
    // a fragment like card lock/unlock that is displayed while Dashboard is expanded. View is recreated
    // in this case, and will always be collapsed unless state is saved and restored.
    // See https://medium.com/@kirillsuslov/how-to-save-android-view-state-in-kotlin-9dbe96074d49
    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState)

        ss.isVisible = this.visibility == View.VISIBLE
        ss.showingActions = this.showingActions

        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            if (state.isVisible) {
                visibility = View.VISIBLE
            } else {
                visibility = View.INVISIBLE
            }
            this.showingActions = state.showingActions
            if (this.showingActions) {
                showActionsImmediate()
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : View.BaseSavedState {
        var isVisible: Boolean = false
        var showingActions: Boolean = false

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel) : super(source) {
            isVisible = source.readByte().toInt() != 0
            showingActions = source.readByte().toInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte((if (isVisible) 1 else 0).toByte())
            out.writeByte((if (showingActions) 1 else 0).toByte())
        }

        companion object {

            //required field that makes Parcelables from a Parcel
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private fun initializeViews() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_dashboard_expandable, this)

        cardAndActionsView = findViewById(R.id.cl_card_view_and_actions)
        cardView = findViewById(R.id.cv_dashboard)
        actionsView = findViewById(R.id.layout_card_actions)
        expandCollapseButton = findViewById(R.id.btn_disclose_hide_card_actions)
        transparentBarBelowCardView = findViewById(R.id.view_bar_under_button_bottom_half)
        shadowAboveTransparentBar = findViewById(R.id.view_shadow_under_button_top_half)

        expandCollapseButton.setOnClickListener {
            showActions(!showingActions)
        }

        cardAndActionsViewCollapsedHeight = context.resources.getDimensionPixelSize(R.dimen.dashboard_card_and_actions_height_collapsed)
        cardAndActionsViewExpandedHeight = context.resources.getDimensionPixelSize(R.dimen.dashboard_card_and_actions_height_expanded)
        cardViewTopMarginCollapsed = context.resources.getDimensionPixelSize(R.dimen.dashboard_card_view_top_margin_collapsed)
        cardViewTopMarginExpanded = context.resources.getDimensionPixelSize(R.dimen.dashboard_card_view_top_margin_expanded)

        animationDurationMs = resources.getInteger(R.integer.dashboard_disclose_hide_duration_ms).toLong()
        cardViewAnimationDelayMs = animationDurationMs * 7 / 10
        buttonRotationAnimationDurationMs = resources.getInteger(R.integer.dashboard_button_rotation_duration_ms).toLong()
    }

    private fun showMoreOptionsBottomNav(items: List<ExpandableViewListItem>) {
        val stringOptions = ArrayList<String>()
        for (item in items) {
            stringOptions.add(getLabelForExpandableItem(item))
        }
        val dialog = ListBottomSheetDialogFragment.newInstance(
                object : ListBottomSheetDialogFragment.ListBottomSheetDialogListener{
                    override fun onOptionSelected(index: Int, optionText: CharSequence) {
                        val clickListener = getClickListenerForExpandableItem(items[index])
                        clickListener.onClick(this@DashboardExpandableView)
                    }
                    override fun onDialogCancelled() {
                        // Do nothing.
                    }
                }, context.getString(R.string.OVERVIEW_MORE_CARD_OPTIONS), stringOptions, null, DashboardMoreOptionsBottomSheetDialogFragmentStyle)
        dialog.show((context as BaseActivity).supportFragmentManager, MORE_CARD_OPTIONS_DIALOG_TAG)
    }

    // use when restoring state of view to show it expanded immediately
    fun showActionsImmediate() {
        showingActions = true
        // card and actions view height
        val layoutParams = cardAndActionsView.layoutParams
        layoutParams.height = cardAndActionsViewExpandedHeight
        cardAndActionsView.layoutParams = layoutParams

        // card view position
        val cardViewLayoutParams = cardView.layoutParams as ConstraintLayout.LayoutParams
        cardViewLayoutParams.topMargin = cardViewTopMarginExpanded
        cardView.layoutParams = cardViewLayoutParams

        // card actions alpha
        actionsView.alpha = 1.0F

        // rotation of expand/collapse button
        expandCollapseButton.rotation = 180F

        // size of expand/collapse button
        expandCollapseButton.scaleX = 1.33F
        expandCollapseButton.scaleY = 1.33F

        // alpha of shadow above bottom edge of view
        shadowAboveTransparentBar.alpha = 1.0F

        listener?.onExpandImmediate()
        cardView.setCondensed(false, false, animationDurationMs, cardViewAnimationDelayMs)
    }

    fun showActions(show: Boolean) {
        // TODO(kurt): still seems possible to quickly tap the button twice, leaving it in a strange state with the disclosure caret twisted. Why?
        expandCollapseButton.isEnabled = false

        // setup vals for animations

        // animate expanding/collapsing card and actions view
        val currentHeight: Int
        val newHeight: Int

        // animate position of card view - move up as view expands
        val cardViewTopMarginStart: Int
        val cardViewTopMarginEnd: Int

        // animate alpha card actions rows - fade in as view expands
        val cardActionsAlphaStart: Float
        val cardActionsAlphaEnd: Float

        // animate rotation of expand/collapse button
        val buttonRotationByAmount: Float

        // animate expand/collapse button grow/shrink - grow as view expands
        val buttonScale: Float

        // animate alpha of shadow above bottom edge of view - fade out as view expands
        val shadowAlphaStart: Float
        val shadowAlphaEnd: Float

        if (show) {
            // card and actions view height
            currentHeight = cardAndActionsViewCollapsedHeight
            newHeight = cardAndActionsViewExpandedHeight

            // card view position
            cardViewTopMarginStart = cardViewTopMarginCollapsed
            cardViewTopMarginEnd = cardViewTopMarginExpanded

            // card actions alpha
            cardActionsAlphaStart = 0F
            cardActionsAlphaEnd = 1F

            // expand/collapse button rotation
            buttonRotationByAmount = 180F

            // expand/collapse button grow/shrink
            buttonScale = 1.33F

            // shadow above bottom edge alpha
            shadowAlphaStart = 1F
            shadowAlphaEnd = 0F
        } else {
            // card and actions view height
            currentHeight = cardAndActionsViewExpandedHeight
            newHeight = cardAndActionsViewCollapsedHeight

            // card view position
            cardViewTopMarginStart = cardViewTopMarginExpanded
            cardViewTopMarginEnd = cardViewTopMarginCollapsed

            // card actions alpha
            cardActionsAlphaStart = 1F
            cardActionsAlphaEnd = 0F

            // expand/collapse button rotation
            buttonRotationByAmount = -180F

            // expand/collapse button grow/shrink
            buttonScale = 1F

            // shadow above bottom edge alpha
            shadowAlphaStart = 0F
            shadowAlphaEnd = 1F
        }

        // card and actions view height
        val cardAndActionsViewHeightAnimator = ValueAnimator.ofInt(currentHeight, newHeight)
        cardAndActionsViewHeightAnimator.addUpdateListener { valueAnimator ->
            val animatedHeight = valueAnimator.animatedValue as Int
            val layoutParams = cardAndActionsView.layoutParams
            layoutParams.height = animatedHeight
            cardAndActionsView.layoutParams = layoutParams
            if (newHeight == animatedHeight) {
                // animation is complete
                showingActions = !showingActions
                expandCollapseButton.isEnabled = true
                // so containing fragment can set its obscuring overlay visibility to INVISIBLE rather
                // than leaving it VISIBLE with alpha 0, which could affect performance
                if (show) {
                    listener?.onExpandEnd()
                } else {
                    listener?.onCollapseEnd()
                }
            }
        }
        cardAndActionsViewHeightAnimator.duration = animationDurationMs
        cardAndActionsViewHeightAnimator.interpolator = AccelerateDecelerateInterpolator()

        // card view position
        val cardViewLayoutParams = cardView.layoutParams as ConstraintLayout.LayoutParams
        val cardViewTopMarginAnimator = ValueAnimator.ofInt(cardViewTopMarginStart, cardViewTopMarginEnd)
        cardViewTopMarginAnimator.addUpdateListener { valueAnimator ->
            cardViewLayoutParams.topMargin = valueAnimator.animatedValue as Int
            cardView.requestLayout()
        }
        cardViewTopMarginAnimator.duration = animationDurationMs
        cardViewTopMarginAnimator.interpolator = AccelerateDecelerateInterpolator()

        // card actions alpha
        val cardActionsAlphaAnimation = AlphaAnimation(cardActionsAlphaStart, cardActionsAlphaEnd)
        cardActionsAlphaAnimation.duration = animationDurationMs
        cardActionsAlphaAnimation.fillAfter = true
        cardActionsAlphaAnimation.interpolator = AccelerateInterpolator()

        // expand/collapse button rotation
        val buttonRotationAnimator = expandCollapseButton
                .animate()
                .rotationBy(buttonRotationByAmount)
                .setDuration(buttonRotationAnimationDurationMs)
                .setInterpolator(AccelerateDecelerateInterpolator())

        // expand/collapse button scale
        val buttonScaleXAnimator = expandCollapseButton
                .animate()
                .scaleX(buttonScale)
                .setDuration(buttonRotationAnimationDurationMs)
                .setInterpolator(AccelerateDecelerateInterpolator())
        val buttonScaleYAnimator = expandCollapseButton
                .animate()
                .scaleY(buttonScale)
                .setDuration(buttonRotationAnimationDurationMs)
                .setInterpolator(AccelerateDecelerateInterpolator())

        // shadow above bottom edge alpha
        val shadowAlphaAnimation = AlphaAnimation(shadowAlphaStart, shadowAlphaEnd)
        shadowAlphaAnimation.duration = animationDurationMs
        shadowAlphaAnimation.fillAfter = true

        // start animations
        cardAndActionsViewHeightAnimator.start()
        cardViewTopMarginAnimator.start()
        actionsView.startAnimation(cardActionsAlphaAnimation)
        buttonRotationAnimator.start()
        buttonScaleXAnimator.start()
        buttonScaleYAnimator.start()
        shadowAboveTransparentBar.startAnimation(shadowAlphaAnimation)

        if (show) {
            listener?.onExpandStart()
            cardView.setCondensed(false, true, animationDurationMs, cardViewAnimationDelayMs)
        } else {
            listener?.onCollapseStart()
            cardView.setCondensed(true, true, animationDurationMs, cardViewAnimationDelayMs)
        }
    }

    fun showExpandCollapseButton(show: Boolean) {
        expandCollapseButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    fun setExpandableListItems(items: List<ExpandableViewListItem>) {
        val item1 = findViewById<LinearLayout>(R.id.item1Layout)
        val item2 = findViewById<LinearLayout>(R.id.item2Layout)
        val item3 = findViewById<LinearLayout>(R.id.item3Layout)
        val item4 = findViewById<LinearLayout>(R.id.item4Layout)

        item1.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
        item2.visibility = if (items.size > 1) View.VISIBLE else View.GONE
        item3.visibility = if (items.size > 2) View.VISIBLE else View.GONE
        item4.visibility = if (items.size > 3) View.VISIBLE else View.GONE

        if (items.isNotEmpty()) {
            item1.visibility = View.VISIBLE
            val item1Icon = item1.findViewById<AppCompatImageView>(R.id.itemIcon)
            val item1Label = item1.findViewById<TextView>(R.id.itemLabel)
            item1Icon.setImageDrawable(getIconForExpandableItem(items[0]))
            item1Label.text = getLabelForExpandableItem(items[0])
            item1.setOnClickListener(getClickListenerForExpandableItem(items[0]))
        } else {
            item1.visibility = View.GONE
        }

        if (items.size > 1) {
            item2.visibility = View.VISIBLE
            val item2Icon = item2.findViewById<AppCompatImageView>(R.id.itemIcon)
            val item2Label = item2.findViewById<TextView>(R.id.itemLabel)
            item2Icon.setImageDrawable(getIconForExpandableItem(items[1]))
            item2Label.text = getLabelForExpandableItem(items[1])
            item2.setOnClickListener(getClickListenerForExpandableItem(items[1]))
        } else {
            item2.visibility = View.GONE
        }

        if (items.size > 2) {
            item3.visibility = View.VISIBLE
            val item3Icon = item3.findViewById<AppCompatImageView>(R.id.itemIcon)
            val item3Label = item3.findViewById<TextView>(R.id.itemLabel)
            item3Icon.setImageDrawable(getIconForExpandableItem(items[2]))
            item3Label.text = getLabelForExpandableItem(items[2])
            item3.setOnClickListener(getClickListenerForExpandableItem(items[2]))
        } else {
            item3.visibility = View.GONE
        }

        if (items.size > 3) {
            item4.visibility = View.VISIBLE
            val item4Icon = item4.findViewById<AppCompatImageView>(R.id.itemIcon)
            val item4Label = item4.findViewById<TextView>(R.id.itemLabel)
            item4Icon.setImageDrawable(getIconForExpandableItem(items[3]))
            item4Label.text = getLabelForExpandableItem(items[3])
            item4.setOnClickListener(getClickListenerForExpandableItem(items[3]))
        } else {
            item4.visibility = View.GONE
        }
    }

    private fun getIconForExpandableItem(item: ExpandableViewListItem): Drawable {
        val id = when (item) {
            is ExpandableViewListItem.ShowCardDetailsItem -> { R.drawable.ic_dashboard_card_details_show }
            is ExpandableViewListItem.HideCardDetailsItem -> { R.drawable.ic_dashboard_card_details_hide }
            is ExpandableViewListItem.MoveMoneyItem -> { R.drawable.ic_dashboard_move_money }
            is ExpandableViewListItem.LockCardItem -> { R.drawable.ic_dashboard_card_lock }
            is ExpandableViewListItem.UnlockCardItem -> { R.drawable.ic_dashboard_card_unlock }
            is ExpandableViewListItem.ChangeCardPinItem -> { R.drawable.ic_dashboard_change_pin }
            is ExpandableViewListItem.ReplaceCardItem -> { R.drawable.ic_dashboard_replace_card }
            is ExpandableViewListItem.ReportLostStolenItem -> { R.drawable.ic_dashboard_report_lost_stolen }
            is ExpandableViewListItem.CancelCardItem -> { R.drawable.ic_dashboard_cancel_card }
            is ExpandableViewListItem.MoreOptionsItem -> { R.drawable.ic_dashboard_more_options }
        }
        return ContextCompat.getDrawable(context, id)!!
    }

    private fun getLabelForExpandableItem(item: ExpandableViewListItem): String {
        val id = when (item) {
            is ExpandableViewListItem.ShowCardDetailsItem -> { R.string.OVERVIEW_SHOW_CARD_DETAILS }
            is ExpandableViewListItem.HideCardDetailsItem -> { R.string.OVERVIEW_HIDE_CARD_DETAILS }
            is ExpandableViewListItem.MoveMoneyItem -> { R.string.OVERVIEW_MOVE_MONEY }
            is ExpandableViewListItem.LockCardItem -> { R.string.OVERVIEW_LOCK_MY_CARD }
            is ExpandableViewListItem.UnlockCardItem -> { R.string.OVERVIEW_UNLOCK_MY_CARD }
            is ExpandableViewListItem.ChangeCardPinItem -> { R.string.OVERVIEW_CHANGE_CARD_PIN }
            is ExpandableViewListItem.ReplaceCardItem -> { R.string.OVERVIEW_REPLACE_CARD }
            is ExpandableViewListItem.ReportLostStolenItem -> { R.string.OVERVIEW_REPORT_LOST_STOLEN }
            is ExpandableViewListItem.CancelCardItem -> { R.string.OVERVIEW_CANCEL_CARD }
            is ExpandableViewListItem.MoreOptionsItem -> { R.string.OVERVIEW_MORE_OPTIONS }
        }
        return context.getString(id)
    }

    private fun getClickListenerForExpandableItem(item: ExpandableViewListItem): OnClickListener {
        return when (item) {
            is ExpandableViewListItem.ShowCardDetailsItem -> { OnClickListener {
                listener?.onShowCardDetails()
            } }
            is ExpandableViewListItem.HideCardDetailsItem -> { OnClickListener {
                listener?.onHideCardDetails()
            } }
            is ExpandableViewListItem.MoveMoneyItem -> { OnClickListener {
                listener?.onMoveMoney()
            } }
            is ExpandableViewListItem.LockCardItem -> { OnClickListener {
                listener?.onLockCard()
            } }
            is ExpandableViewListItem.UnlockCardItem -> { OnClickListener {
                listener?.onUnlockCard()
            } }
            is ExpandableViewListItem.ChangeCardPinItem -> { OnClickListener {
                listener?.onChangePin()
            } }
            is ExpandableViewListItem.ReplaceCardItem -> { OnClickListener {
                listener?.onReplaceCard()
            } }
            is ExpandableViewListItem.ReportLostStolenItem -> { OnClickListener {
                listener?.onReportCardLostStolen()
            } }
            is ExpandableViewListItem.CancelCardItem -> { OnClickListener {
                listener?.onCancelCard()
            } }
            is ExpandableViewListItem.MoreOptionsItem -> { OnClickListener {
                showMoreOptionsBottomNav(item.options)
            } }
        }
    }

    fun showCardViewMessageString(message: String) {
        cardView.showMessage(message)
    }

    fun hideCardMessageString() {
        cardView.hideMessage()
    }

    interface DashboardExpandableViewListener {
        fun onExpandImmediate()
        fun onExpandStart()
        fun onExpandEnd()
        fun onCollapseStart()
        fun onCollapseEnd()

        fun onShowCardDetails()
        fun onHideCardDetails()
        fun onMoveMoney()
        fun onLockCard()
        fun onUnlockCard()
        fun onChangePin()
        fun onReplaceCard()
        fun onReportCardLostStolen()
        fun onCancelCard()
    }
}

sealed class ExpandableViewListItem {
    object ShowCardDetailsItem : ExpandableViewListItem()
    object HideCardDetailsItem : ExpandableViewListItem()
    object MoveMoneyItem : ExpandableViewListItem()
    object LockCardItem : ExpandableViewListItem()
    object UnlockCardItem : ExpandableViewListItem()
    object ChangeCardPinItem : ExpandableViewListItem()
    class ReplaceCardItem(val featureCurrentlyAvailable: Boolean) : ExpandableViewListItem()
    class ReportLostStolenItem(val featureCurrentlyAvailable: Boolean) : ExpandableViewListItem()
    class CancelCardItem(val featureCurrentlyAvailable: Boolean) : ExpandableViewListItem()
    class MoreOptionsItem(val options: List<ExpandableViewListItem>) : ExpandableViewListItem()
}