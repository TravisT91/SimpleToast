package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentDirectDepositBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * DirectDepositFragment
 * </p>
 * This baseFragmentIm displays direct deposit info to the user and let's them print a direct deposit form.
 * </p>
 * Created by Travis Tkachuk 12/6/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

class DirectDepositFragment: BaseEngagePageFragment(){

    override fun createViewModel(): BaseViewModel? {
        return ViewModelProviders.of(this).get(DirectDepositViewModel::class.java).apply {
            directDepositViewModel = this
        }
    }

    private lateinit var directDepositViewModel : DirectDepositViewModel
    private lateinit var binding : FragmentDirectDepositBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDirectDepositBinding.inflate(inflater,container,false).apply {
            viewModel = directDepositViewModel.apply {
                accountTypeString = getString(R.string.DIRECT_DEPOSIT_ACCOUNT_TYPE_VALUE)
            }
            palette = Palette
            setLifecycleOwner(this@DirectDepositFragment)
            viewPrintableFormButton.setOnClickListener {
                val pdfTitle = getString(R.string.DIRECT_DEPOSIT_TITLE)
                val unformattedString = getString(R.string.DIRECT_DEPOSIT_PRINTABLE_URL_UNFORMATTED)
                val formattedUrl = directDepositViewModel.formatDirectDepositUrl(unformattedString)
                findNavController().navigate(
                        R.id.action_directDepositFragment_to_webViewFragment,
                        WebViewFragment.getBundle(
                                title = pdfTitle,
                                initialUrl = formattedUrl,
                                forPrint = true,
                                showPdfImmediately = true))
            }
            swipeToRefreshLayout.setOnRefreshListener {
                directDepositViewModel.getDirectDepositInfo()
                swipeToRefreshLayout.isRefreshing = false
            }
        }
        directDepositViewModel.getDirectDepositInfo()
        return binding.root
    }
}