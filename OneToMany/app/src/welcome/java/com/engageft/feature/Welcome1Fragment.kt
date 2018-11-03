package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.Welcome1FragmentBinding

class Welcome1Fragment: LotusFullScreenFragment() {
    lateinit var binding: Welcome1FragmentBinding

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.welcome1_fragment, container, false)

        binding.imageViewIcon.background = ContextCompat.getDrawable(context!!, R.drawable.ic_splash)

        binding.messageTextView.visibility = View.GONE
        binding.titleTextView.visibility = View.GONE
        return binding.root
    }
}