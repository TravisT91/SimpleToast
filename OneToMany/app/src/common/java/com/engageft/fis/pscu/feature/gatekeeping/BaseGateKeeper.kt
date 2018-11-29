package com.engageft.fis.pscu.feature.gatekeeping

import android.os.Handler
import android.util.Log

/**
 * BaseGateKeeper
 * <p>
 * GateKeeper abstraction for checking a list of asynchronous items and reporting success, fail, or error
 * to a listener to resolve the items. 
 * </p>
 * Created by joeyhutchins on 11/8/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseGateKeeper(val gateKeeperListener: GateKeeperListener) : GatedItemResultListener {
    companion object {
        const val TAG = "BaseGateKeeper"
        const val INDEX_NOT_STARTED = -1
    }
    protected abstract val gatedItems: List<GatedItem>
    private var itemIndex = INDEX_NOT_STARTED

    private val handler = Handler()

    fun run() {
        // Keep things threadsafe
        handler.post {
            if (!isRunning()) {
                itemIndex = 0

                checkNextItem()
            } else {
                Log.w(TAG, "attempt to start GateKeeper when already started")
            }
        }
    }

    fun isRunning(): Boolean {
        return itemIndex != INDEX_NOT_STARTED
    }

    private fun checkNextItem() {
        if (itemIndex != INDEX_NOT_STARTED) {
            if (itemIndex >= gatedItems.size) {
                gateKeeperListener.onGateOpen()
                reset()
            } else {
                val item = gatedItems[itemIndex]

                item.checkItem(this)
            }
        }
    }

    private fun getCurrentItem() :GatedItem {
        return gatedItems[itemIndex]
    }

    override fun onItemCheckPassed() {
        // Keep things threadsafe
        handler.post{
            itemIndex++
            checkNextItem()
        }
    }

    override fun onItemCheckFailed() {
        // Keep things threadsafe
        handler.post {
            gateKeeperListener.onGatedItemFailed(getCurrentItem())
            reset()
        }
    }

    override fun onItemError(e: Throwable?, message: String?) {
        // Keep things threadsafe
        handler.post {
            gateKeeperListener.onItemError(getCurrentItem(), e, message)
            reset()
        }
    }

    private fun reset() {
        itemIndex = INDEX_NOT_STARTED
    }
}

interface GateKeeperListener {
    fun onItemError(item: GatedItem, e: Throwable?, message: String?)
    fun onGatedItemFailed(item: GatedItem)
    fun onGateOpen()
}


internal interface GatedItemResultListener {
    fun onItemCheckPassed()
    fun onItemCheckFailed()
    fun onItemError(e: Throwable?, message: String?)
}

abstract class GatedItem {
    internal abstract fun checkItem(resultListener: GatedItemResultListener)
}