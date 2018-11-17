package com.engageft.feature

import android.animation.ValueAnimator
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.engageft.apptoolbox.BaseActivity
import com.engageft.apptoolbox.view.ListBottomSheetDialogFragment
import com.engageft.apptoolbox.view.ProductCardView
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.tools.MixpanelEvent
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.R.style.OverviewMoreOptionsBottomSheetDialogFragmentStyle

/**
 *  OverviewView
 *  </p>
 *  Manages Overview card view, action items list, and spend/save/transactions info
 *  </p>
 *  Created by Kurt Mueller on 4/18/18.
 *  Copyright (c) 2018 Engage FT. All rights reserved.
 */
class OverviewView : ConstraintLayout {

    private lateinit var cardAndActionsView: ViewGroup
    lateinit var cardView: ProductCardView
        private set
    private lateinit var actionsView: View
    private lateinit var expandCollapseButton: AppCompatImageButton
    private lateinit var transparentBarBelowCardView: View
    private lateinit var shadowAboveTransparentBar: View
    lateinit var overviewShowHideCardDetailsIcon: ImageView
    lateinit var overviewShowHideCardDetailsLabel: TextView
    lateinit var overviewLockUnlockCardIcon: ImageView
    lateinit var overviewLockUnlockCardLabel: TextView

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

    var listener: OverviewViewListener? = null

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

    // Must save expanded/collapsed state so that it is shown correctly after returning to Overview from
    // a fragment like card lock/unlock that is displayed while Overview is expanded. View is recreated
    // in this case, and will always be collapsed unless state is saved and restored.
    // See https://medium.com/@kirillsuslov/how-to-save-android-view-state-in-kotlin-9dbe96074d49
    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState)

        ss.showingActions = this.showingActions

        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            this.showingActions = state.showingActions
            if (this.showingActions) {
                showActionsImmediate()
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : View.BaseSavedState {
        var showingActions: Boolean = false

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel) : super(source) {
            showingActions = source.readByte().toInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
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
        inflater.inflate(R.layout.view_overview, this)

        cardAndActionsView = findViewById(R.id.cl_card_view_and_actions)
        cardView = findViewById(R.id.cv_overview)
        actionsView = findViewById(R.id.layout_card_actions)
        expandCollapseButton = findViewById(R.id.btn_disclose_hide_card_actions)
        transparentBarBelowCardView = findViewById(R.id.view_transparent_bar_below_card_view)
        shadowAboveTransparentBar = findViewById(R.id.view_shadow_above_transparent_bar)
        overviewShowHideCardDetailsIcon = findViewById(R.id.overviewShowHideCardDetailsIcon)
        overviewShowHideCardDetailsLabel = findViewById(R.id.overviewShowHideCardDetailsLabel)
        overviewLockUnlockCardIcon = findViewById(R.id.overviewLockUnlockCardIcon)
        overviewLockUnlockCardLabel = findViewById(R.id.overviewLockUnlockCardLabel)

        findViewById<ViewGroup>(R.id.overviewShowHideCardDetailsLayout).apply {
            if (DashboardConfig.CARD_MANAGEMENT_SHOW_CARD_DETAILS_ENABLED) {
                setOnClickListener {
                    EngageService.getInstance().mixpanel.track(MixpanelEvent.getTapsDashboardMenuEvent(findViewById<TextView>(R.id.overviewShowHideCardDetailsLabel).text))
                    listener?.onShowHideCardDetails()
                }
            } else {
                visibility = View.GONE
            }
        }

        findViewById<ViewGroup>(R.id.overviewMoveMoneyLayout).apply {
            if (DashboardConfig.CARD_MANAGEMENT_MOVE_MONEY_ENABLED) {
                setOnClickListener {
                    EngageService.getInstance().mixpanel.track(MixpanelEvent.getTapsDashboardMenuEvent(findViewById<TextView>(R.id.overviewMoveMoneyLabel).text))
                    listener?.onMoveMoney()
                }
            } else {
                visibility = View.GONE
            }
        }

        findViewById<ViewGroup>(R.id.overviewLockUnlockCardLayout).apply {
            if (DashboardConfig.CARD_MANAGEMENT_LOCK_MY_CARD_ENABLED) {
                setOnClickListener {
                    EngageService.getInstance().mixpanel.track(MixpanelEvent.getTapsDashboardMenuEvent(findViewById<TextView>(R.id.overviewLockUnlockCardLabel).text))
                    listener?.onLockUnlockCard()
                }
            } else {
                visibility = View.GONE
            }
        }

        findViewById<ViewGroup>(R.id.overviewMoreOptionsLayout).apply {
            setOnClickListener {
                EngageService.getInstance().mixpanel.track(MixpanelEvent.getTapsDashboardMenuEvent(findViewById<TextView>(R.id.overviewMoreOptionsLabel).text))
                showMoreOptionsBottomNav()
            }
        }

        expandCollapseButton.setOnClickListener {
            showActions(!showingActions)
        }

        cardAndActionsViewCollapsedHeight = context.resources.getDimensionPixelSize(R.dimen.overview_card_and_actions_height_collapsed)
        cardAndActionsViewExpandedHeight = context.resources.getDimensionPixelSize(R.dimen.overview_card_and_actions_height_expanded)
        cardViewTopMarginCollapsed = context.resources.getDimensionPixelSize(R.dimen.overview_card_view_top_margin_collapsed)
        cardViewTopMarginExpanded = context.resources.getDimensionPixelSize(R.dimen.overview_card_view_top_margin_expanded)

        animationDurationMs = resources.getInteger(R.integer.overview_disclose_hide_duration_ms).toLong()
        cardViewAnimationDelayMs = animationDurationMs * 7 / 10
        buttonRotationAnimationDurationMs = resources.getInteger(R.integer.overview_button_rotation_duration_ms).toLong()
    }

    private fun showMoreOptionsBottomNav() {
        val changeCardPinOption = context.getString(R.string.OVERVIEW_CHANGE_CARD_PIN)
        val replaceCardOption = context.getString(R.string.OVERVIEW_REPLACE_CARD)
        val reportLostStolenOption = context.getString(R.string.OVERVIEW_REPORT_LOST_STOLEN)
        val cancelCardOption = context.getString(R.string.OVERVIEW_CANCEL_CARD)
        val stringOptions = ArrayList<String>().apply {
            add(changeCardPinOption)
            add(replaceCardOption)
            add(reportLostStolenOption)
            add(cancelCardOption)
        }
        val dialog = ListBottomSheetDialogFragment.newInstance(
                object : ListBottomSheetDialogFragment.ListBottomSheetDialogListener{
                    override fun onOptionSelected(index: Int, optionText: CharSequence) {
                        when (optionText) {
                            changeCardPinOption -> listener?.onChangePin()
                            replaceCardOption -> listener?.onReplaceCard()
                            reportLostStolenOption -> listener?.onReportCardLostStolen()
                            cancelCardOption -> listener?.onCancelCard()
                        }
                    }

                    override fun onDialogCancelled() {
                        // Do nothing.
                    }
                }, context.getString(R.string.OVERVIEW_MORE_CARD_OPTIONS), stringOptions, null, OverviewMoreOptionsBottomSheetDialogFragmentStyle)
        dialog.show((context as BaseActivity).supportFragmentManager, MORE_CARD_OPTIONS_DIALOG_TAG)
    }

    // use when restoring state of view to show it expanded immediately
    private fun showActionsImmediate() {
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

    fun showCardViewMessageString(message: String) {
        cardView.showMessage(message)
    }

    fun hideCardMessageString() {
        cardView.hideMessage()
    }

    interface OverviewViewListener {
        fun onExpandImmediate()
        fun onExpandStart()
        fun onExpandEnd()
        fun onCollapseStart()
        fun onCollapseEnd()

        fun onShowHideCardDetails()
        fun onMoveMoney()
        fun onLockUnlockCard()
        fun onChangePin()
        fun onReplaceCard()
        fun onReportCardLostStolen()
        fun onCancelCard()
    }
}