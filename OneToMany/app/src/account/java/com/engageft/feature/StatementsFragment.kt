package com.engageft.feature

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.adapter.SelectableLabelsSection
import com.engageft.onetomany.databinding.FragmentStatementsBinding
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import org.joda.time.DateTime
import DisplayDateTimeUtils
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.view.WebViewFragment
import com.engageft.engagekit.EngageService
import com.engageft.onetomany.R
import com.engageft.onetomany.config.EngageAppConfig


class StatementsFragment: LotusFullScreenFragment() {

    private val sectionAdapter: SectionedRecyclerViewAdapter = SectionedRecyclerViewAdapter()
    private lateinit var statementsViewModel: StatementsViewModel

    override fun createViewModel(): BaseViewModel? {
        statementsViewModel = ViewModelProviders.of(this).get(StatementsViewModel::class.java)
        return statementsViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentStatementsBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(context!!)
        binding.recyclerView.adapter = sectionAdapter

        binding.viewModel = statementsViewModel
        statementsViewModel.statementsObservable.observe(this, Observer { statementsList ->
            if (statementsList.isNotEmpty()) {
                updateRecyclerView(statementsList)
            }
        })

        return binding.root
    }
    val TAG = "StatementsFragment"
    private fun updateRecyclerView(statementsList: List<DateTime>) {
        sectionAdapter.removeAllSections()

        if (statementsList.isNotEmpty()) {

//            sectionAdapter.addSection(HorizontalRuleSection.newInstanceFormBorder())
            val monthsSection = SelectableLabelsSection(context!!, object: SelectableLabelsSection.OnSelectableLabelInteractionListener {
                override fun onLabelClicked(labelId: Int) {
                    Log.e(TAG, "onLabelClicked() + $labelId")
                    if (statementsList.isNotEmpty() && statementsList.size > labelId) {
                        val selectedMonth = statementsList[labelId]
                        var monthParam = selectedMonth.monthOfYear.toString()
                        if (monthParam.length == 1) {
                            // pad "6" to "06", for instance, because server requires two digits for month
                            monthParam = "0$monthParam"
                        }
                        val statementUrl = String.format(getString(R.string.STATEMENT_FILTER_URL),
                                EngageAppConfig.engageKitConfig.prodEnvironment.websiteUrl,
//                                EngageService.getInstance().engageConfig.getWebsiteUrl(),
                                EngageService.getInstance().authManager.authToken,
                                monthParam,
                                selectedMonth.year.toString())

                        val title = String.format(getString(R.string.STATEMENT_PDF_HEADER), DisplayDateTimeUtils.getMonthAbbrYear(selectedMonth.year, selectedMonth.monthOfYear))

                        val webViewFragment = WebViewFragment.newInstance(title, statementUrl, true, false)
                        findNavController().navigate(R.id.action_statementsFragment_to_webViewFragment)
                    }
                }
            })
            for (i in 0 until statementsList.size) {
                val month = statementsList[i]
                monthsSection.addLabel(i, DisplayDateTimeUtils.getMonthFullYear(month.year, month.monthOfYear))
            }

            sectionAdapter.addSection(monthsSection)
//            sectionAdapter.addSection(HorizontalRuleSection.newInstanceFormBorder())
        } else {
//            sectionAdapter.addSection(HeaderLabelSection(getString(R.string.STATEMENTS_NOT_AVAILABLE_MESSAGE)))
        }

        sectionAdapter.notifyDataSetChanged()
    }
}