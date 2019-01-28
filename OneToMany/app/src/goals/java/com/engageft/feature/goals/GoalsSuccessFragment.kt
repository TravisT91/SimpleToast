package com.engageft.feature.goals

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.ToolbarVisibilityState
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.BaseEngagePageFragment

class GoalsSuccessFragment: BaseEngagePageFragment() {

    private val navigationOverrideClickListener = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            return true
        }
    }

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_goals_success, container,false)

        toolbarController.setToolbarVisibility(ToolbarVisibilityState.INVISIBLE)

        upButtonOverrideProvider.setUpButtonOverride(navigationOverrideClickListener)
        backButtonOverrideProvider.setBackButtonOverride(navigationOverrideClickListener)

        val imageViewLayout = view.findViewById<View>(R.id.imageViewLayout)
        imageViewLayout.findViewById<ImageView>(R.id.imageViewIcon).apply {
            setImageResource(R.drawable.ic_check)
        }

        Handler().postDelayed({
            view.findNavController().popBackStack()
        }, 2000)
        return view
    }
}