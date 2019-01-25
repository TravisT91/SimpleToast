package com.engageft.fis.pscu.feature.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.TransactionDetailsFragmentBinding
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.transactions.CategoryFragment.Companion.ARG_NEW_CATEGORY
import com.engageft.fis.pscu.feature.transactions.utils.TransactionId

/**
 * TransactionDetailsFragment
 * </p>
 * This fragment shows the details of a transaction, as well as allows the user to edit the
 * transaction details.
 * </p>
 * Created by Travis Tkachuk 1/23/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class TransactionDetailsFragment : BaseEngagePageFragment() {

    companion object {
        const val ARG_TRANSACTION_ID = "ARG_TRANSACTION_ID"
    }

    private val checkChangeObserver = Observer<Any?> { detailsViewModel.checkForChanges() }

    lateinit var detailsViewModel: TransactionDetailsViewModel
    private lateinit var binding: TransactionDetailsFragmentBinding

    override fun createViewModel(): BaseViewModel? {
        arguments?.getString(ARG_TRANSACTION_ID)?.let {
            val transactionId = TransactionId(it)
            detailsViewModel = ViewModelProviders
                    .of(this, TransactionViewModelFactory(transactionId))
                    .get(TransactionDetailsViewModel::class.java)
        } ?: run{
            throw IllegalArgumentException("TRANSACTION ID ARGUMENT NOT FOUND IN ${this::class.java}")
        }
        return detailsViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailsViewModel.apply {

            transaction.observe(this@TransactionDetailsFragment, Observer {
                isOffBudget.observe(this@TransactionDetailsFragment, checkChangeObserver)
                txNotes.observe(this@TransactionDetailsFragment, checkChangeObserver)
                txCategory.observe(this@TransactionDetailsFragment, checkChangeObserver)
            })

            repoLiveData.observe(this@TransactionDetailsFragment, Observer {
                setTransaction(it)
                arguments?.getString(ARG_NEW_CATEGORY)?.let{ newCategory ->
                    detailsViewModel.txCategory.postValue(newCategory)
                    arguments = null
                }
            })

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TransactionDetailsFragmentBinding.inflate(inflater, container, false).apply {
            palette = Palette
            viewModel = detailsViewModel
            setLifecycleOwner(this@TransactionDetailsFragment)

            category.apply {
                isEnabled = true
                setEditTextOnClickListener {
                    categoryFrame.performClick()
                }
            }

            categoryFrame.apply {
                setOnClickListener {
                    root.findNavController().navigate(R.id.action_transaction_details_to_categories)
                }
            }
        }
        return binding.root
    }



}