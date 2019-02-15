package com.engageft.fis.pscu.feature.transactions

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.engageft.fis.pscu.feature.transactions.utils.TransactionUtils
import com.ob.domain.lookup.TransactionType
import kotlinx.android.synthetic.main.transaction_details_fragment.category
import utilGen1.TransactionInfoUtils
import java.lang.IndexOutOfBoundsException
import java.math.BigDecimal
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
        setHasOptionsMenu(true)
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

            detailsViewModel.transaction.observe(this@TransactionDetailsFragment, Observer
            {
                if (!TransactionUtils.showOffBudget(it)){
                    binding.offBudgetFrame.visibility = View.GONE
                }

                val transactionType = TransactionUtils.getTransactionType(it)
                if (!(transactionType == TransactionType.LOAD
                                || transactionType == TransactionType.TRANSFER
                                || transactionType == TransactionType.REVERSE_LOAD
                                || transactionType == TransactionType.FEE
                                || TextUtils.isEmpty(it.category))) {
                    binding.apply {
                        categoryFrame.setOnClickListener { _ ->
                            val parent = this@TransactionDetailsFragment.parentFragment
                            (parent as TransactionDetailsMediatorFragment).apply {
                                goToCategoryFragment()
                            }
                        }
                        category.setEditTextOnClickListener {
                            categoryFrame.performClick()
                        }
                    }
                } else if (TextUtils.isEmpty(it.category)) {
                    category.visibility = View.GONE
                }

                if (it.amount > BigDecimal.ZERO){
                    binding.amount.setTextColor(Palette.successColor)
                }
            })

            category.apply {
                isEnabled = true
                setMaxLines(Int.MAX_VALUE)
            }

            notes.setMaxLines(Int.MAX_VALUE)


            saveButton.setOnClickListener {
                saveChanges()
            }
        }

        return binding.root
    }

    private fun saveChanges() {
        if (detailsViewModel.categoryHasChanged) {
            ListBottomSheetDialogFragment.newInstance(
                    listener = object : ListBottomSheetDialogFragment.ListBottomSheetDialogListener {
                        override fun onOptionSelected(index: Int, optionText: CharSequence) {
                            when (index) {
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

            ).show(activity!!.supportFragmentManager, "setAllOrOnlyThisDialog")
        } else {
            detailsViewModel.saveChanges(false)
        }
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
            menu?.getItem(0)?.isVisible = hasChanges
        }
    }

    private fun onChangeSuccess() {
        binding.root.findNavController().popBackStack(R.id.transactionDetailsFragment, true)
    }

    private var menu: Menu? = null

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.general_options_menu_save, menu)
        menu?.getItem(0)?.isVisible = false
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let{
            if (it.itemId == R.id.menu_item_save){
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}