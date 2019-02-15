package com.engageft.fis.pscu.feature

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.engageft.fis.pscu.BuildConfig

/**
 * EasterEggGestureDetector
 * <p>
 * This is a util class for applying easter egg Gesture detecting on any view.
 * </p>
 * Created by joeyhutchins on 8/3/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EasterEggGestureDetector(context: Context, viewToGestureDetect: View, private val listener: EasterEggGestureListener) : GestureDetector.OnGestureListener {
    companion object {
        private const val TAG = "EasterEggGestureDetecto"
        private const val VELOCITY_Y = 1000.0f
    }
    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, this)

    private var countSwipesUp: Int = 0
    private var countTaps:Int = 0
    private var countSwipesDown:Int = 0

    init {
        // Only setup gesture detection if we're NOT in production.
        if (BuildConfig.DEBUG) {
            viewToGestureDetect.setOnTouchListener(object : View.OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    // Clone the touch event, do the gesture detector can consume it AND the views underneath
                    // can consume it. If it's not cloned, only one view hierarchy reliably consumes the event
                    // and breaks it's usage for the other. 
                    event!!
                    val motionEvent = MotionEvent.obtain(event.downTime, event.eventTime, event.action, event.x, event.y, event.metaState)
                    gestureDetector.onTouchEvent(motionEvent)
                    return false
                }
            })
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        when {
            velocityY < -VELOCITY_Y -> handleSwipeUp()
            velocityY > VELOCITY_Y -> handleSwipeDown()
            else -> {
                resetGestureCounts()
                Log.d(TAG, "onFling not up or down")
            }
        }

        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        // Do nothing
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        // Do nothing
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        handleTap()

        return false
    }

    fun resetGestureCounts() {
        countTaps = 0
        countSwipesUp = 0
        countSwipesDown = 0
    }

    private fun handleSwipeUp() {
        countSwipesDown = 0
        countSwipesUp = Math.min(countSwipesUp + 1, 2)
        countTaps = 0
    }

    private fun handleTap() {
        countTaps = Math.min(countTaps + 1, 2)

        if (countSwipesUp == 2 && countTaps == 2) {
            listener.onEasterEggActivated()
            resetGestureCounts()
        } else if (countSwipesDown == 2 && countTaps == 2) {
            listener.onEasterEggDeactivated()
            resetGestureCounts()
        }
    }

    private fun handleSwipeDown() {
        countSwipesUp = 0
        countTaps = 0
        countSwipesDown = Math.min(countSwipesDown + 1, 2)
    }
}

interface EasterEggGestureListener {
    fun onEasterEggActivated()
    fun onEasterEggDeactivated()
}