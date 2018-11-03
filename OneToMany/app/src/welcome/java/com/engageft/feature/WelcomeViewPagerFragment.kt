package com.engageft.feature

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.onetomany.R

class WelcomeViewPagerFragment: LotusFullScreenFragment() {

    companion object {
       private const val WELCOME_FRAGMENT_COUNT = 5
    }

    private lateinit var pageIndicator1: ImageView
    private lateinit var pageIndicator3: ImageView
    private lateinit var pageIndicator2: ImageView
    private lateinit var pageIndicator4: ImageView
    private lateinit var pageIndicator5: ImageView
    private lateinit var selectedDot: Drawable
    private lateinit var unselectedDot: Drawable

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    var pageIndicatorIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_viewpager_fragment, container, false)
        val viewPager = view.findViewById<ViewPager>(R.id.welcomeViewPager)
        viewPager.adapter = EducationPagerAdapter(childFragmentManager)
        (activity as? AppCompatActivity)!!.supportActionBar?.hide()
        pageIndicator1 = view.findViewById(R.id.pageIndicator1ImageView)
        pageIndicator2 = view.findViewById(R.id.pageIndicator2ImageView)
        pageIndicator3 = view.findViewById(R.id.pageIndicator3ImageView)
        pageIndicator4 = view.findViewById(R.id.pageIndicator4ImageView)
        pageIndicator5 = view.findViewById(R.id.pageIndicator5ImageView)

        val pageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                setSelectedPageIndicator(position)
                setUnselectedPageIndicator(pageIndicatorIndex)
                pageIndicatorIndex = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }

        viewPager.addOnPageChangeListener(pageChangeListener)
        unselectedDot = ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_dot)!!
        selectedDot = ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_dot_selected)!!

        val loginButton = view.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_viewPagerFragment_to_loginFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setSelectedPageIndicator(pageIndicatorIndex)
    }

    private fun setUnselectedPageIndicator(position: Int) {
        when (position) {
            0 -> pageIndicator1.setImageDrawable(unselectedDot)
            1 -> pageIndicator2.setImageDrawable(unselectedDot)
            2 -> pageIndicator3.setImageDrawable(unselectedDot)
            3 -> pageIndicator4.setImageDrawable(unselectedDot)
            4 -> pageIndicator5.setImageDrawable(unselectedDot)
        }
    }

    private fun setSelectedPageIndicator(pageIndicator: Int) {
        when (pageIndicator) {
            0 -> pageIndicator1.setImageDrawable(selectedDot)
            1 -> pageIndicator2.setImageDrawable(selectedDot)
            2 -> pageIndicator3.setImageDrawable(selectedDot)
            3 -> pageIndicator4.setImageDrawable(selectedDot)
            4 -> pageIndicator5.setImageDrawable(selectedDot)
        }
    }

    internal inner class EducationPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

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