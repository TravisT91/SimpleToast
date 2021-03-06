/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.engageft.fis.pscu.feature.transactions.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.engageft.engagekit.repository.util.NetworkState
import com.engageft.engagekit.repository.util.NetworkTaskStatus
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * A View Holder that can display a loading or have click action.
 * It is used to show the network state of paging.
 */
class NetworkStateItemViewHolder(view: View, retryCallback: (() -> Unit)?)
    : RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMsg = view.findViewById<TextView>(R.id.error_msg)
    init {
        retryCallback?.let { retryCallback ->
            retry.setOnClickListener {
                retryCallback()
            }
        }
    }
    fun bindTo(networkState: NetworkState?) {
        progressBar.indeterminateTintList = ColorStateList.valueOf(Palette.infoColor)
        progressBar.visibility = toVisibility(networkState?.status == NetworkTaskStatus.RUNNING)
        errorMsg.visibility = toVisibility(networkState?.status == NetworkTaskStatus.FAILED)
        retry.visibility = toVisibility(networkState?.status == NetworkTaskStatus.FAILED)
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: (() -> Unit)?): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(view, retryCallback)
        }

        fun toVisibility(constraint : Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}