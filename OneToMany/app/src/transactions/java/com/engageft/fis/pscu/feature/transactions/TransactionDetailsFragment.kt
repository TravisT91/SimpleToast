package com.engageft.fis.pscu.feature.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.ListBottomSheetDialogFragment
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.TransactionDetailsFragmentBinding
import com.engageft.fis.pscu.feature.BaseEngageSubFragment
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.transactions.utils.TransactionId
import java.lang.IndexOutOfBoundsException
import java.util.Locale

/**
 * TransactionDetailsFragment
 * </p>
 * This fragment shows the details of a transaction, as well as allows the user to edit the
 * transaction details.
 * </p>
 * Created by Travis Tkachuk 1/23/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class TransactionDetailsFragment : BaseEngageSubFragment() {

    companion object {
        const val ARG_TRANSACTION_ID = "ARG_TRANSACTION_ID"
    }

    private val checkChangeObserver = Observer<Any?> { detailsViewModel.checkForChanges() }
    private val onChangeObserver = Observer<Boolean> { onChangeChecked(it) }
    private val onChangeSuccessObserver = Observer<Unit> { onChangeSuccess() }

    lateinit var detailsViewModel: TransactionDetailsViewModel
    lateinit var binding: TransactionDetailsFragmentBinding

    override fun createViewModel(): BaseViewModel? {
        arguments?.getString(ARG_TRANSACTION_ID)?.let {
            val transactionId = TransactionId(it)
            detailsViewModel = ViewModelProviders
                    .of(this, TransactionViewModelFactory(transactionId))
                    .get(TransactionDetailsViewModel::class.java)
        } ?: run {
            throw IllegalArgumentException("TRANSACTION ID ARGUMENT NOT FOUND IN ${this::class.java}")
        }
        return detailsViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailsViewModel.apply {

            val tdf = this@TransactionDetailsFragment
            transaction.observe(tdf, Observer {
                isOffBudget.observe(tdf, checkChangeObserver)
                txNotes.observe(tdf, checkChangeObserver)
                txCategory.observe(tdf, checkChangeObserver)
                hasChanges.observe(tdf, onChangeObserver)
            })

            repoLiveData.observe(tdf, Observer {
                setTransaction(it)
            })

            txCategory.observe(tdf, Observer {
                val lang = Locale.getDefault().language
                val sm = EngageService.getInstance().storageManager
                val onBudgetText = isOffBudget.value?.let { isOffBudget ->
                    if (isOffBudget) "(" + getString(R.string.TRANSACTION_LIST_OFF_BUDGET_LABEL) + ") "
                    else ""
                } ?: ""
                txCategoryDisplayString.postValue(
                        onBudgetText + sm.getBudgetCategoryDescription(it.toString(), lang))
            })

            isOffBudget.observe(tdf, Observer {
                //this post is to refresh the display string when the budget is changed
                txCategory.value = txCategory.value
            })

            // To pop this fragment when changes are successfully made
            changeSuccess.observe(tdf, onChangeSuccessObserver)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = TransactionDetailsFragmentBinding.inflate(
                inflater,
                container,
                false).apply {
            palette = Palette
            viewModel = detailsViewModel
            setLifecycleOwner(this@TransactionDetailsFragment)

            category.apply {
                isEnabled = true
                setMaxLines(Int.MAX_VALUE)
                setEditTextOnClickListener {
                    categoryFrame.performClick()
                }
            }

            notes.setMaxLines(Int.MAX_VALUE)

            categoryFrame.apply {
                setOnClickListener {
                    val parent = this@TransactionDetailsFragment.parentFragment
                    (parent as TransactionDetailsMediatorFragment).apply {
                        goToCategoryFragment()
                    }
                }
            }

            saveButton.setOnClickListener {
                if(detailsViewModel.categoryHasChanged) {
                    ListBottomSheetDialogFragment.newInstance(
                            listener = object : ListBottomSheetDialogFragment.ListBottomSheetDialogListener{
                                override fun onOptionSelected(index: Int, optionText: CharSequence) {
                                    when(index){
                                        0 -> detailsViewModel.saveChanges(true)
                                        1 -> detailsViewModel.saveChanges(false)
                                        else -> throw IndexOutOfBoundsException()
                                    }
                                }

                                override fun onDialogCancelled() {
                                    //intentionally left blank
                                }

                            },
                            title = context!!.getString(R.string.TRANSACTION_CATEGORY_CHANGE_TITLE),
                            subtitle = String.format(
                                    context!!.getString(R.string.TRANSACTION_CATEGORY_CHANGE_SUBTITLE),
                                    detailsViewModel.txCategory.value,
                                    detailsViewModel.transaction.value?.store
                                    ),
                            options = listOf(
                                    context!!.getString(R.string.TRANSACTION_CATEGORY_CHANGE_ALL_TRANSACTION),
                                    context!!.getString(R.string.TRANSACTION_CATEGORY_CHANGE_ONLY_THIS))
                                    .toCollection(ArrayList()),
                            titleTextAppearance = R.style.ListOptionBottomSheetDialogFragmentStyle

                    ).show(activity!!.supportFragmentManager,"setAllOrOnlyThisDialog")
                } else {
                    detailsViewModel.saveChanges(false)
                }
            }
        }

        return binding.root
    }


    private val alphaAnimation= AlphaAnimation(0f, 1f).apply {
        duration = 500
        startOffset = 50
    }

    private fun onChangeChecked(hasChanges: Boolean){
        binding.saveButton.apply {
            if (hasChanges) {
                if (visibility != View.VISIBLE) {
                    visibility = View.VISIBLE
                    startAnimation(alphaAnimation)
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun onChangeSuccess() {
        binding.root.findNavController().popBackStack(R.id.transactionDetailsFragment, true)
    }
}