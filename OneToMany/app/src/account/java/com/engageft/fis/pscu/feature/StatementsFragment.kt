package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.adapter.HeaderLabelTitleWithSubtitleSection
import com.engageft.apptoolbox.adapter.HorizontalRuleSection
import com.engageft.apptoolbox.adapter.SelectableLabelsSection
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentStatementsBinding
import com.engageft.fis.pscu.feature.branding.Palette
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils

/**
 * StatementsFragment
 *
 * Fragment that lists the available monthly statements
 *
 * Created by Kurt Mueller on 2/13/17.
 * Imported, and modified by Atia Hashimi 11/22/218
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class StatementsFragment: BaseEngagePageFragment() {
    private val sectionAdapter: SectionedRecyclerViewAdapter = SectionedRecyclerViewAdapter()
    private lateinit var statementsViewModel: StatementsViewModel

    override fun createViewModel(): BaseViewModel? {
        statementsViewModel = ViewModelProviders.of(this).get(StatementsViewModel::class.java)
        return statementsViewModel
    }

    private lateinit var binding: FragmentStatementsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatementsBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context!!)
        binding.recyclerView.adapter = sectionAdapter

        binding.viewModel = statementsViewModel

        statementsViewModel.statementsObservable.observe(viewLifecycleOwner, Observer { statementsList ->
            updateRecyclerView(statementsList)
        })

        return binding.root
    }

    private fun updateRecyclerView(statementsList: List<DateTime>) {
        sectionAdapter.removeAllSections()

        if (statementsList.isNotEmpty()) {
            val title = getString(R.string.statements_description)
                    .applyTypefaceAndColorToSubString(ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor, getString(R.string.statements_description_substring))
            val availableDate = statementsViewModel.dayOfMonthStatementAvailable.toString() + DisplayDateTimeUtils.getOrdinal(context!!, statementsViewModel.dayOfMonthStatementAvailable)
            val subTitle = String.format(getString(R.string.statements_subDescription_format), availableDate)

            sectionAdapter.addSection(HeaderLabelTitleWithSubtitleSection(title, subTitle, R.layout.statements_header_section))

            sectionAdapter.addSection(HorizontalRuleSection())
            val monthsSection = SelectableLabelsSection(context!!, R.style.StatementsListItemsTextAppearance, object: SelectableLabelsSection.OnSelectableLabelInteractionListener {
                override fun onLabelClicked(labelId: Int) {
                    if (statementsList.isNotEmpty() && statementsList.size > labelId) {
                        val selectedMonth = statementsList[labelId]
                        var monthParam = selectedMonth.monthOfYear.toString()
                        if (monthParam.length == 1) {
                            // pad "6" to "06", for instance, because server requires two digits for month
                            monthParam = "0$monthParam"
                        }
                        val webSiteUrl = if (EngageAppConfig.isUsingProdEnvironment) {
                            EngageAppConfig.engageKitConfig.prodEnvironment.websiteUrl
                        } else {
                            EngageAppConfig.engageKitConfig.devEnvironment.websiteUrl
                        }

                        val statementUrl = String.format(getString(R.string.STATEMENT_FILTER_URL),
                                webSiteUrl,
                                EngageService.getInstance().authManager.authToken,
                                monthParam,
                                selectedMonth.year.toString())

                        //TODO(aHashimi): can't change the toolbar title dynamically.
                        //TODO(aHashimi): don't use the WebView to show the PDF: https://engageft.atlassian.net/browse/SHOW-449
                        val pdfTitle = DisplayDateTimeUtils.getMonthFullYear(selectedMonth.year, selectedMonth.monthOfYear)
                        findNavController().navigate(R.id.action_statementsFragment_to_webViewFragment,
                                WebViewFragment.getBundle(pdfTitle, statementUrl, true, true))
                    }
                }
            })
            for (i in 0 until statementsList.size) {
                val month = statementsList[i]
                monthsSection.addLabel(i, DisplayDateTimeUtils.getMonthFullYear(month.year, month.monthOfYear))
            }

            sectionAdapter.addSection(monthsSection)
            sectionAdapter.addSection(HorizontalRuleSection())
        } else {
            binding.statementsLayout.gravity = Gravity.CENTER_VERTICAL
            val title = getString(R.string.statements_description_empty)
                    .applyTypefaceAndColorToSubString(ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor, getString(R.string.statements_description_empty_substring))
            val subTitle = getString(R.string.statements_subDescription_empty)
            sectionAdapter.addSection(HeaderLabelTitleWithSubtitleSection(title, subTitle, R.layout.statements_header_section))
        }

        sectionAdapter.notifyDataSetChanged()
    }
}