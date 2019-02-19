package com.engageft.fis.pscu.feature.onboarding

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.DialogFragmentOnboardingViewpagerBinding
import com.engageft.fis.pscu.feature.BaseEngageDialogFragment
import com.engageft.fis.pscu.feature.branding.Palette
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.fragment_dashboard.*


/**
 * Created by joeyhutchins on 2/11/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class OnboardingDialogFragment : BaseEngageDialogFragment() {
    companion object {
        const val PAGE_INDEX_UNINITIALIZED = -1
    }
    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var binding: DialogFragmentOnboardingViewpagerBinding
    private lateinit var adapter: OnboardingPagerAdapter
    private lateinit var selectedDot: Drawable
    private lateinit var unselectedDot: Drawable

    var pageIndicatorIndex = PAGE_INDEX_UNINITIALIZED

    override fun createViewModel(): BaseViewModel? {
        onboardingViewModel = ViewModelProviders.of(this).get(OnboardingViewModel::class.java)

        return onboardingViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use no frame, no title, etc.
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LotusTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_fragment_onboarding_viewpager, container, false)

        unselectedDot = ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_dot)!!
        selectedDot = ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_dot_selected)!!

        adapter = OnboardingPagerAdapter(childFragmentManager)

        binding.viewPager.adapter = adapter

        onboardingViewModel.onboardingItemsObservable.observe(this, Observer { onboardingItems ->
            if (onboardingItems.isEmpty()) {
                pageIndicatorIndex = PAGE_INDEX_UNINITIALIZED
            } else if (pageIndicatorIndex == PAGE_INDEX_UNINITIALIZED) {
                pageIndicatorIndex = 0
            }
            adapter.items = onboardingItems
            invalidatePagerDots(onboardingItems.size)
            adapter.notifyDataSetChanged()
            updatePagerButton()
        })

        val pageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                setSelectedPageIndicator(position)
                setUnselectedPageIndicator(pageIndicatorIndex)
                pageIndicatorIndex = position
                updatePagerButton()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }

        binding.viewPager.addOnPageChangeListener(pageChangeListener)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setSelectedPageIndicator(pageIndicatorIndex)
    }

    private fun updatePagerButton() {
        if (pageIndicatorIndex == PAGE_INDEX_UNINITIALIZED) {
            binding.viewPagerButton.visibility = View.GONE
        } else {
            binding.viewPagerButton.visibility = View.VISIBLE
            if (pageIndicatorIndex == adapter.items.size -1) {
                binding.viewPagerButton.text = getString(R.string.ONBOARDING_PAGER_DONE)
                binding.viewPagerButton.setOnClickListener{
                    dismiss()
                }
            } else {
                binding.viewPagerButton.text = getString(R.string.ONBOARDING_PAGER_NEXT)
                binding.viewPagerButton.setOnClickListener {
                    val index = binding.viewPager.currentItem
                    binding.viewPager.setCurrentItem(index + 1, true)
                }
            }
        }
    }

    private fun invalidatePagerDots(size: Int) {
        binding.indicatorLayout.removeAllViews()
        val layoutInflater = LayoutInflater.from(context)

        for (i in 0 until size) {
            val itemLayout = layoutInflater.inflate(R.layout.onboarding_pager_dot, binding.indicatorLayout, false)

            val itemIcon = itemLayout.findViewById<AppCompatImageView>(R.id.dot)
            if (i == pageIndicatorIndex) {
                itemIcon.setImageDrawable(selectedDot)
            } else {
                itemIcon.setImageDrawable(unselectedDot)
            }
            // Do not add margin if last dot
            if (i < size - 1) {
                val imageParams = itemIcon.layoutParams as LinearLayout.LayoutParams
                imageParams.setMargins(0, 0, context!!.resources.getDimension(R.dimen.onboarding_pager_dot_margin).toInt(), 0)
            }

            binding.indicatorLayout.addView(itemLayout)
        }
    }

    private fun setUnselectedPageIndicator(position: Int) {
        val imageView = binding.indicatorLayout.getChildAt(position) as AppCompatImageView
        imageView.setImageDrawable(unselectedDot)
    }

    private fun setSelectedPageIndicator(pageIndicator: Int) {
        if (pageIndicator != PAGE_INDEX_UNINITIALIZED) {
            val imageView = binding.indicatorLayout.getChildAt(pageIndicator) as AppCompatImageView
            imageView.setImageDrawable(selectedDot)
        }
    }

    internal inner class OnboardingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        var items = listOf<OnboardingListItems>()

        override fun getItem(position: Int): Fragment {
            val fragment = OnboardingItemFragment()
            val item = items[position]
            when (item) {
                is OnboardingListItems.DashboardOnboardingItem -> {
                    fragment.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_smart)
                    fragment.title = getString(R.string.ONBOARDING_DASHBOARD_TITLE_FORMAT).applyTypefaceAndColorToSubString(
                            ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor,
                            getString(R.string.ONBOARDING_DASHBOARD_TITLE_SUBSTRING))
                    fragment.message = getString(R.string.ONBOARDING_DASHBOARD_MESSAGE)
                }
                is OnboardingListItems.CardOnboardingItem -> {
                    fragment.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_control)
                    fragment.title = getString(R.string.ONBOARDING_CARD_TITLE_FORMAT).applyTypefaceAndColorToSubString(
                            ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor,
                            getString(R.string.ONBOARDING_CARD_TITLE_SUBSTRING))
                    fragment.message = getString(R.string.ONBOARDING_CARD_MESSAGE)
                }
                is OnboardingListItems.SearchOnboardingItem -> {
                    fragment.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_search)
                    fragment.title = getString(R.string.ONBOARDING_SEARCH_TITLE_FORMAT).applyTypefaceAndColorToSubString(
                            ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor,
                            getString(R.string.ONBOARDING_SEARCH_TITLE_SUBSTRING))
                    fragment.message = getString(R.string.ONBOARDING_SEARCH_MESSAGE)
                }
                is OnboardingListItems.BudgetsOnboardingItem -> {
                    fragment.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_budget_splash)
                    fragment.title = getString(R.string.ONBOARDING_BUDGETS_TITLE_FORMAT).applyTypefaceAndColorToSubString(
                            ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor,
                            getString(R.string.ONBOARDING_BUDGETS_TITLE_SUBSTRING))
                    fragment.message = getString(R.string.ONBOARDING_BUDGETS_MESSAGE)
                }
                is OnboardingListItems.GoalsOnboardingItem -> {
                    fragment.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_goals)
                    fragment.title = getString(R.string.ONBOARDING_GOALS_TITLE_FORMAT).applyTypefaceAndColorToSubString(
                            ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                            Palette.primaryColor,
                            getString(R.string.ONBOARDING_GOALS_TITLE_SUBSTRING))
                    fragment.message = getString(R.string.ONBOARDING_GOALS_MESSAGE)
                }
            }
            return fragment
        }

        override fun getCount(): Int {
            return items.size
        }
    }
}