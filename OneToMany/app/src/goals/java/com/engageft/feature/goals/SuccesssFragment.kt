package com.engageft.feature.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.BaseEngagePageFragment

class SuccesssFragment: BaseEngagePageFragment() {

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.imageview_with_circle_background, container,false)
        view.findViewById<ImageView>(R.id.imageViewIcon).apply {
            setImageResource(R.drawable.ic_check)
        }
        return view
    }
}