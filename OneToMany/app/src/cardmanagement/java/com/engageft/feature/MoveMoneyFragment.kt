package com.engageft.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.engageft.onetomany.R
import com.engageft.onetomany.databinding.FragmentMoveMoneyBinding

/**
 * MoveMoneyFragment
 * </p>
 * This fragment presents the user with options to move money
 * </p>
 * Created by Travis Tkachuk 11/2/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class MoveMoneyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMoveMoneyBinding.inflate(inflater,container,false)
        binding.apply {
            palette = Palette
            bankTransfer.setOnClickListener {
                //TODO(ttkachuk) implement on click listener
                Toast.makeText(context,"Bank Transfer",Toast.LENGTH_SHORT).show()
            }
            creditOrDebitCardLoad.setOnClickListener {
                //TODO(ttkachuk) implement on click listener
                Toast.makeText(context,"Credit or Debit Card Load",Toast.LENGTH_SHORT).show()
            }
            directDeposit.setOnClickListener {
                //TODO(ttkachuk) implement on click listener
                Toast.makeText(context,"Direct Deposit",Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.TITLE_MOVE_MONEY)
    }
}