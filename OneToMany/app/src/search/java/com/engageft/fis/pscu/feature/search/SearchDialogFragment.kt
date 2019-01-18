package com.engageft.fis.pscu.feature.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.hideKeyboard
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.DialogFragmentSearchBinding
import com.engageft.fis.pscu.feature.BaseEngageDialogFragment
import com.engageft.fis.pscu.feature.search.adapter.TransactionsSearchAdapter
import com.engageft.fis.pscu.feature.transactions.adapter.TransactionListener

/**
 * SearchDialogFragment
 * <p>
 * Fragment for transaction searches
 * </p>
 * Created by kurteous on 1/6/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SearchDialogFragment : BaseEngageDialogFragment(), TransactionListener {
    override fun createViewModel(): BaseViewModel? {
        viewModel = ViewModelProviders.of(this).get(SearchDialogFragmentViewModel::class.java)

        return viewModel
    }

    private lateinit var viewModel: SearchDialogFragmentViewModel
    private lateinit var binding: DialogFragmentSearchBinding

    private val searchAdapter: TransactionsSearchAdapter by lazy {
        binding.searchRecyclerView.adapter = TransactionsSearchAdapter(this)
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.searchRecyclerView.adapter as TransactionsSearchAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use no frame, no title, etc.
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LotusTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_fragment_search, container, false)

        searchAdapter.resetSearch()

        binding.searchBackButton.setOnClickListener {
            dismiss()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // Do nothing
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding.searchClearButton.visibility = if (charSequence.isNotBlank()) View.VISIBLE else View.INVISIBLE
            }

            override fun afterTextChanged(editable: Editable) {
                // Do nothing
            }
        })

        binding.searchEditText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_ACTION_UNSPECIFIED && event.action == KeyEvent.ACTION_DOWN)) {
                textView?.apply {
                    viewModel.searchTransactions(text.trim().toString())
                }
                true
            } else false
        }

        binding.searchClearButton.setOnClickListener {
            searchAdapter.resetSearch()
            binding.searchEditText.setText("")
        }

        viewModel.searchTransactions.observe(this, Observer<List<Transaction>> { transactionList ->
            if (transactionList.isEmpty()) searchAdapter.showNoResults(getString(R.string.EMPTY_SEARCH_MESSAGE)) else searchAdapter.updateTransactions(transactionList)
        })

        viewModel.searchErrorObservable.observe(this, Observer { searchStringStatusType ->
            when (searchStringStatusType) {
                SearchDialogFragmentViewModel.SearchStringStatus.SEARCH_STRING_NOT_MINIMUM_LENGTH ->
                    Toast.makeText(context, getString(R.string.TRANSACTIONS_SEARCH_MESSAGE_MINIMUM_CHARS), Toast.LENGTH_LONG).show()
                else ->
                    binding.searchEditText.hideKeyboard()
            }
        })

        return binding.root
    }

    override fun onTransactionSelected(transaction: Transaction) {
        Toast.makeText(activity, "Transaction selected: " + transaction.store, Toast.LENGTH_SHORT).show()
    }
}