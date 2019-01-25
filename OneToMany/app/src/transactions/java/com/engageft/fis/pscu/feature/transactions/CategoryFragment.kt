package com.engageft.fis.pscu.feature.transactions

import android.os.Bundle
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.BaseEngagePageFragment

/**
 * TODO: Class Name
 * </p>
 * TODO: Class Description
 * </p>
 * Created by Travis Tkachuk 1/24/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CategoryFragment : BaseEngagePageFragment() {

    companion object {
        const val ARG_NEW_CATEGORY = "ARG_NEW_CATEGORY"
    }

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.transactionDetailsFragment) as? TransactionDetailsFragment).apply {
            this?.arguments?.apply { putString(ARG_NEW_CATEGORY, "NEW CATEGORY!") }
        }
    }
}