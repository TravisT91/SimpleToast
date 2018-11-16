package com.engageft.fis.pscu.feature

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentWelcomeViewpagerBinding
/**
 * WelcomeViewPagerFragment
 *
 * Manages showing of welcome screens, updates the page indicator, and invokes navigation based on button clicks.
 *
 * Created by Atia Hashimi 11/6/2018.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class WelcomeViewPagerFragment: LotusFullScreenFragment() {

    companion object {
       private const val WELCOME_FRAGMENT_COUNT = 5
    }

    private lateinit var binding: FragmentWelcomeViewpagerBinding
    private lateinit var selectedDot: Drawable
    private lateinit var unselectedDot: Drawable

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    var pageIndicatorIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome_viewpager, container, false)

        binding.welcomeViewPager.adapter = WelcomePagerAdapter(childFragmentManager)

        val pageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                setSelectedPageIndicator(position)
                setUnselectedPageIndicator(pageIndicatorIndex)
                pageIndicatorIndex = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }

        binding.welcomeViewPager.addOnPageChangeListener(pageChangeListener)
        //TODO: use a library that handles this: https@ //engageft.atlassian.net/browse/SHOW-435
        unselectedDot = ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_dot)!!
        selectedDot = ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_dot_selected)!!

        binding.loginButton.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_welcomeViewPagerFragment_to_loginFragment)
        }

        binding.getStartedButton.setOnClickListener {
            //TODO(aHashimi): navigate to card activity
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setSelectedPageIndicator(pageIndicatorIndex)
    }

    private fun setUnselectedPageIndicator(position: Int) {
        when (position) {
            0 -> binding.pageIndicator1ImageView.setImageDrawable(unselectedDot)
            1 -> binding.pageIndicator2ImageView.setImageDrawable(unselectedDot)
            2 -> binding.pageIndicator3ImageView.setImageDrawable(unselectedDot)
            3 -> binding.pageIndicator4ImageView.setImageDrawable(unselectedDot)
            4 -> binding.pageIndicator5ImageView.setImageDrawable(unselectedDot)
        }
    }

    private fun setSelectedPageIndicator(pageIndicator: Int) {
        when (pageIndicator) {
            0 -> binding.pageIndicator1ImageView.setImageDrawable(selectedDot)
            1 -> binding.pageIndicator2ImageView.setImageDrawable(selectedDot)
            2 -> binding.pageIndicator3ImageView.setImageDrawable(selectedDot)
            3 -> binding.pageIndicator4ImageView.setImageDrawable(selectedDot)
            4 -> binding.pageIndicator5ImageView.setImageDrawable(selectedDot)
        }
    }

    internal inner class WelcomePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> Welcome1Fragment()
                1 -> Welcome2Fragment()
                2 -> Welcome3Fragment()
                3 -> Welcome4Fragment()
                4 -> Welcome5Fragment()
                else -> Welcome1Fragment()
            }
        }

        override fun getCount(): Int {
            return WELCOME_FRAGMENT_COUNT
        }
    }
}