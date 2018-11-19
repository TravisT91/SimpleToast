package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.adapter.HeaderLabelTitleWithSubtitleSection
import com.engageft.apptoolbox.adapter.HorizontalRuleSection
import com.engageft.apptoolbox.adapter.SelectableLabelsSection
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.engagekit.EngageService
import com.engageft.feature.StatementsViewModel
import com.engageft.feature.util.DisplayDateTimeUtils
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import org.joda.time.DateTime
import com.engageft.fis.pscu.databinding.FragmentStatementsBinding

class StatementsFragment: LotusFullScreenFragment() {

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

        statementsViewModel.statementsObservable.observe(this, Observer { statementsList ->
            updateRecyclerView(statementsList)
        })

        //TODO(aHashimi): make this reusable.
        statementsViewModel.dialogInfoObservable.observe(this, Observer { dialogInfo ->
            when (dialogInfo.dialogType) {
                DialogInfo.DialogType.GENERIC_ERROR -> {
                    showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                }
                DialogInfo.DialogType.SERVER_ERROR -> {
                    showDialog(infoDialogGenericErrorTitleMessageConditionalNewInstance(context!!, dialogInfo))
                }
                DialogInfo.DialogType.NO_INTERNET_CONNECTION -> {
                    showDialog(infoDialogGenericErrorTitleMessageNewInstance(
                            context!!, message = getString(R.string.alert_error_message_no_internet_connection)))
                }
                DialogInfo.DialogType.CONNECTION_TIMEOUT -> {
                    showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!, getString(R.string.alert_error_message_connection_timeout)))
                }
                else -> {}
            }
        })

        return binding.root
    }

    private fun updateRecyclerView(statementsList: List<DateTime>) {
        sectionAdapter.removeAllSections()

        if (statementsList.isNotEmpty()) {
            val title = getString(R.string.statements_description)
                    .applyTypefaceAndColorToSubString(ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            ContextCompat.getColor(context!!, R.color.statementSubstringDescription), getString(R.string.statements_description_substring))
            val subTitle = String.format(getString(R.string.statements_subDescription_format),
                    DisplayDateTimeUtils.getOrdinal(context!!, statementsViewModel.dayOfMonthStatementAvailable))

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
                        val pdfTitle = String.format(getString(R.string.STATEMENT_PDF_HEADER), DisplayDateTimeUtils.getMonthAbbrYear(selectedMonth.year, selectedMonth.monthOfYear))
                        findNavController().navigate(R.id.action_statementsFragment_to_webViewFragment,
                                WebViewFragment.getBundle(pdfTitle, statementUrl, true, false))
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
                            ContextCompat.getColor(context!!, R.color.statementSubstringDescription), getString(R.string.statements_description_empty_substring))
            val subTitle = getString(R.string.statements_subDescription_empty)
            sectionAdapter.addSection(HeaderLabelTitleWithSubtitleSection(title, subTitle, R.layout.statements_header_section))
        }

        sectionAdapter.notifyDataSetChanged()
    }
}