package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.databinding.FragmentMobileCheckDepositBinding

/**
 * MobileCheckDepositFragment
 * <p>
 * Fragment directing user to get Ingo app for Mobile check deposits.
 * </p>
 * Created by joeyhutchins on 12/14/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class MobileCheckDepositFragment : BaseEngageFullscreenFragment() {
    override fun createViewModel(): BaseViewModel? {
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMobileCheckDepositBinding.inflate(inflater,container,false)
//        binding.apply {
//            palette = Palette
//            bankTransfer.setOnClickListener {
//                //TODO(ttkachuk) implement on click listener
//                Toast.makeText(context,"Bank Transfer",Toast.LENGTH_SHORT).show()
//            }
//            creditOrDebitCardLoad.setOnClickListener {
//                //TODO(ttkachuk) implement on click listener
//                Toast.makeText(context,"Credit or Debit Card Load",Toast.LENGTH_SHORT).show()
//            }
//            mobileCheckDeposit.setOnClickListener {
//                binding.root.findNavController().navigate()
//            }
//            directDeposit.setOnClickListener {
//                binding.root.findNavController().navigate(R.id.action_move_money_fragment_to_directDepositFragment)
//            }
//        }
        return binding.root
    }
}