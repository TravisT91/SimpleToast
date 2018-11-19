package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding

class CardPINFragment : LotusFullScreenFragment() {

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(CardPINViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCardPinBinding.inflate(inflater, container, false)

        return binding.root
    }
}