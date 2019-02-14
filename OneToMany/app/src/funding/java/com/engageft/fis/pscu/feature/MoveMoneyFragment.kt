package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentMoveMoneyBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.config.MobileCheckDepositConfig

/**
 * MoveMoneyFragment
 * </p>
 * This fragment presents the user with options to move money
 * </p>
 * Created by Travis Tkachuk 11/2/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class MoveMoneyFragment : BaseEngagePageFragment() {
    private lateinit var moveMoneyViewModel: MoveMoneyViewModel
    override fun createViewModel(): BaseViewModel? {
        moveMoneyViewModel = ViewModelProviders.of(this).get(MoveMoneyViewModel::class.java)
        return null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMoveMoneyBinding.inflate(inflater,container,false)
        binding.apply {
            palette = Palette
            cardLoad.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_moveMoneyFragment_to_accountsAndTransfersListFragment)
            }
            mobileCheckDeposit.setOnClickListener {
                if (MobileCheckDepositConfig.isIngoPackageInstalled(activity!!)) {
                    binding.root.findNavController().navigate(R.id.action_moveMoneyFragment_to_mobileCheckDepositOpenFragment)
                } else {
                    binding.root.findNavController().navigate(R.id.action_moveMoneyFragment_to_mobileCheckDepositDownloadFragment)
                }
            }
            directDeposit.setOnClickListener {
                binding.root.findNavController().navigate(R.id.action_move_money_fragment_to_directDepositFragment)
            }
        }
        moveMoneyViewModel.apply {
            cardLoadVisibilityObservable.observe(this@MoveMoneyFragment, Observer {
                if (it) {
                    binding.cardLoad.visibility = View.VISIBLE
                    binding.cardLoadDivider.visibility = View.VISIBLE
                } else {
                    binding.cardLoad.visibility = View.GONE
                    binding.cardLoadDivider.visibility = View.GONE
                }
            })
            mobileCheckDepositVisibilityObservable.observe(this@MoveMoneyFragment, Observer {
                if (it) {
                    binding.mobileCheckDeposit.visibility = View.VISIBLE
                    binding.mobileCheckDepositDivider.visibility = View.VISIBLE
                } else {
                    binding.mobileCheckDeposit.visibility = View.GONE
                    binding.mobileCheckDepositDivider.visibility = View.GONE
                }
            })
            directDepositVisibilityObservable.observe(this@MoveMoneyFragment, Observer {
                if (it) {
                    binding.directDeposit.visibility = View.VISIBLE
                } else {
                    binding.directDeposit.visibility = View.GONE
                }
            })
        }
        return binding.root
    }
}