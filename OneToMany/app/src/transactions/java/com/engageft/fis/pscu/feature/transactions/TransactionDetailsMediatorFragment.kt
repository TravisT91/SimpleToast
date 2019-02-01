package com.engageft.fis.pscu.feature.transactions

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.NavigationOverrideClickListener
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.BaseEngagePageFragment
import com.engageft.fis.pscu.feature.infoDialogGenericUnsavedChangesNewInstance

/**
 * TransactionDetailsMediatorFragment
 * </p>
 * TODO: Class Description
 * </p>
 * Created by Travis Tkachuk 1/31/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

class TransactionDetailsMediatorFragment : BaseEngagePageFragment() {
    override fun createViewModel(): BaseViewModel? {
        return null
    }

    private lateinit var frameLayout: FrameLayout
    lateinit var transactionDetailsFragment: TransactionDetailsFragment
    var categoryFragment: CategoryFragment? = null

    private val onBackClicked = object : NavigationOverrideClickListener {
        override fun onClick(): Boolean {
            categoryFragment?.let{
                removeCategoryFragment()
                return true
            } ?: run {
                if (transactionDetailsFragment.detailsViewModel.checkForChanges()){
                    infoDialogGenericUnsavedChangesNewInstance(
                            context = context!!,
                            listener = object: InformationDialogFragment.InformationDialogFragmentListener{
                                override fun onDialogFragmentNegativeButtonClicked() {
                                    //doNothing
                                }

                                override fun onDialogFragmentPositiveButtonClicked() {
                                    findNavController().navigateUp()
                                }

                                override fun onDialogCancelled() {
                                    //doNothing
                                }

                            }
                    ).show(
                            this@TransactionDetailsMediatorFragment.childFragmentManager,
                            "unsavedDialog")
                    return true
                }
                return false
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        frameLayout = FrameLayout(context!!).apply {
            layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            )
            id = View.generateViewId()
        }

        transactionDetailsFragment = TransactionDetailsFragment().apply {
            val id = this@TransactionDetailsMediatorFragment
                    .arguments?.getString(TransactionDetailsFragment.ARG_TRANSACTION_ID)

            this.arguments = Bundle().apply {
                putString(TransactionDetailsFragment.ARG_TRANSACTION_ID, id)
            }

        }

        childFragmentManager.beginTransaction().apply {
            add(frameLayout.id, transactionDetailsFragment)
            commit()
        }

        backButtonOverrideProvider.setBackButtonOverride(onBackClicked)
        upButtonOverrideProvider.setUpButtonOverride(onBackClicked)

        return frameLayout
    }

    fun goToCategoryFragment() {
        categoryFragment = CategoryFragment().apply{
            onCategorySelectedListener = { category ->
                transactionDetailsFragment.detailsViewModel.txCategory.postValue(category)
                removeCategoryFragment()
            }
            onBackClicked = this@TransactionDetailsMediatorFragment.onBackClicked
            arguments = Bundle().apply {
                putString(
                        CategoryFragment.ARG_CURRENTLY_SELECTED_SUB_CATEGORY,
                        transactionDetailsFragment.detailsViewModel.txCategory.value?.toString())
            }
        }

        childFragmentManager.beginTransaction().apply{
            setCustomAnimations(
                    R.anim.nav_enter_anim,
                    R.anim.nav_exit_anim,
                    R.anim.nav_pop_enter_anim,
                    R.anim.nav_default_pop_exit_anim)
            replace(frameLayout.id, categoryFragment!!)
            addToBackStack("categoryFragment")
            commit()
            toolbarController.setToolbarTitle(getString(R.string.TRANSACTIONS_CATEGORIES_TITLE))
        }
    }

    private fun removeCategoryFragment(){
        categoryFragment?.let{
            childFragmentManager.popBackStack()
            toolbarController.setToolbarTitle(getString(R.string.TRANSACTION_DETAILS_TITLE))
            categoryFragment = null
        }
    }
}