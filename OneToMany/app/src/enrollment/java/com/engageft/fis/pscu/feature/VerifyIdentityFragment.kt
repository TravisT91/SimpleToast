package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.util.applyTypefaceAndColorToSubString
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentVerifyIdentityBinding
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * VerifyIdentityFragment
 * <p>
 * A screen in the enrollment flow.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class VerifyIdentityFragment : BaseEngagePageFragment() {
    private lateinit var verifyIdentityDelegate: VerifyIdentityDelegate

    override fun createViewModel(): BaseViewModel? {
        val enrollmentViewModel = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
        verifyIdentityDelegate = enrollmentViewModel.verifyIdentityDelegate
        return enrollmentViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentVerifyIdentityBinding.inflate(inflater, container, false).apply {
            viewModelDelegate = verifyIdentityDelegate
            palette = Palette

            subHeaderTextView.text = getString(R.string.ENROLLMENT_VERIFY_IDENTITY_SUBHEADER).applyTypefaceAndColorToSubString(
                    ResourcesCompat.getFont(context!!, R.font.font_bold)!!,
                    Palette.primaryColor,
                    getString(R.string.ENROLLMENT_VERIFY_IDENTITY_SUBHEADER_SUBSTRING))

            SSNInputWithLabel.addEditTextFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    verifyIdentityDelegate.validateSSNConditionally(false)
                }
            })

            verifyIdentityDelegate.apply {

                nextButtonObservable.observe(this@VerifyIdentityFragment, Observer {
                    when (it) {
                        VerifyIdentityDelegate.NextButtonState.GONE -> {
                            nextButton.visibility = View.GONE
                            activity?.invalidateOptionsMenu()
                        }
                        VerifyIdentityDelegate.NextButtonState.VISIBLE_ENABLED -> {
                            nextButton.visibility = View.VISIBLE
                        }
                    }
                    activity?.invalidateOptionsMenu()
                })

                ssnValidationErrorObservable.observe(this@VerifyIdentityFragment, Observer {
                    when (it) {
                        VerifyIdentityDelegate.ssnValidationError.INVALID -> {
                            if (SSNInputWithLabel.isEnabled) {
                                SSNInputWithLabel.setErrorTexts(listOf(getString(R.string.ENROLLMENT_VERIFY_IDENTITY_ERROR_MESSAGE)))
                            }
                        }
                        else -> {
                            SSNInputWithLabel.setErrorTexts(null)
                        }
                    }
                })
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.get_started_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            val saveMenuItem = it.findItem(R.id.next)
            saveMenuItem.isVisible = verifyIdentityDelegate.nextButtonObservable.value == VerifyIdentityDelegate.NextButtonState.VISIBLE_ENABLED
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.next -> { verifyIdentityDelegate.onNextClicked() }
        }
        return super.onOptionsItemSelected(item)
    }
}