package com.engageft.feature.budgets

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.feature.budgets.extension.budgetStatus
import com.engageft.feature.budgets.extension.getCategoriesSortedByBudgetAmountDescending
import com.engageft.feature.budgets.extension.isGreaterThan
import com.engageft.feature.budgets.extension.isLessThan
import com.engageft.feature.budgets.extension.isZero
import com.engageft.feature.budgets.extension.toBigDecimalOrZeroIfEmpty
import com.engageft.feature.budgets.recyclerview.BudgetItem
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.ob.ws.dom.LoginResponse
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * BudgetsListViewModel
 * <p>
 * ViewModel to support displaying a list of budgets
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetsListViewModel : BaseEngageViewModel() {

    val budgetsObservable: MutableLiveData<Pair<BudgetItem, List<BudgetItem>>> = MutableLiveData()

    fun refresh() {
        progressOverlayShownObservable.postValue(true)
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                response.budgetInfo?.apply {
                                    // setup
                                    val hasBudget = false
                                    val isFirst30 = false
                                    val fractionTimePeriodPassed = fractionOfCurrentMonthPassed() // TODO this will be different within first 30 days

                                    // total spent
                                    var spentAmount = budgetAmountSpent.toBigDecimalOrZeroIfEmpty().abs()   // reused for computing categorySpending values
                                                                                            // amountSpent is always negative from backend
                                    var budgetAmount = budgetAmount.toBigDecimalOrZeroIfEmpty()             // reused for computing categorySpending values
                                    var budgetStatus = budgetStatus()                       // reused for computing categorySpendingValues
                                    val totalBudgetItem = BudgetItem(
                                            categoryName = BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING,
                                            spentAmount = spentAmount,
                                            budgetAmount = budgetAmount,
                                            budgetStatus = budgetStatus,
                                            progress = progress(spentAmount, budgetAmount),
                                            fractionTimePeriodPassed = fractionTimePeriodPassed
                                    )

                                    // categories
                                    val categorySpendings = getCategoriesSortedByBudgetAmountDescending(withOther = false, isInFirst30Days = isFirst30).toMutableList()
                                    var categoryBudgetItems = mutableListOf<BudgetItem>()
                                    for (categorySpending in categorySpendings) {
                                        spentAmount = categorySpending.amountSpent.toBigDecimalOrZeroIfEmpty().abs()    // amountSpent is always negative from backend
                                        budgetAmount = categorySpending.budgetAmount.toBigDecimalOrZeroIfEmpty()
                                        budgetStatus = categorySpending.budgetStatus()
                                        categoryBudgetItems.add(
                                                BudgetItem(
                                                        categoryName = categorySpending.category,
                                                        spentAmount = spentAmount,
                                                        budgetAmount = budgetAmount,
                                                        budgetStatus = budgetStatus,
                                                        progress = progress(spentAmount, budgetAmount),
                                                        fractionTimePeriodPassed = fractionTimePeriodPassed
                                                )
                                        )
                                    }
                                    // add other spending
                                    otherSpending?.let { otherSpendingCategory ->
                                        spentAmount = otherSpendingCategory.amountSpent.toBigDecimalOrZeroIfEmpty().abs()   // amountSpent is always negative from backend
                                        budgetAmount = otherSpendingCategory.budgetAmount.toBigDecimalOrZeroIfEmpty()
                                        budgetStatus = otherSpendingCategory.budgetStatus()
                                        categoryBudgetItems.add(
                                                BudgetItem(
                                                        categoryName = BudgetConstants.CATEGORY_NAME_BE_OTHER_SPENDING,
                                                        spentAmount = spentAmount,
                                                        budgetAmount = budgetAmount,
                                                        budgetStatus = budgetStatus,
                                                        progress = progress(spentAmount, budgetAmount),
                                                        fractionTimePeriodPassed = fractionTimePeriodPassed
                                                )
                                        )
                                    }

                                    progressOverlayShownObservable.postValue(false)
                                    budgetsObservable.postValue(Pair(totalBudgetItem, categoryBudgetItems))
                                } ?: run {
                                    // LoginResponse had no budgetInfo. Should never happen.
                                    dialogInfoObservable.postValue(DialogInfo(dialogType = DialogInfo.DialogType.GENERIC_ERROR))
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }) { e -> handleThrowable(e) }
        )
    }

    private fun progress(spentAmount: BigDecimal, budgetAmount: BigDecimal): Float {
        return if (!spentAmount.isGreaterThan(BigDecimal.ZERO) || budgetAmount.isZero()) {
            // Catches cases of spent <= 0, or budget == 0
            0.0f
        }
        else if (spentAmount.isLessThan(budgetAmount)) {
            spentAmount.divide(budgetAmount, RoundingMode.HALF_UP).toFloat()
        }
        else {
            // spent is at or over budget amount, so cap at 1
            1.0f
        }
    }

    private fun fractionOfCurrentMonthPassed(): Float {
        val dateTime = DateTime.now()
        val dayOfMonth = dateTime.dayOfMonth.toFloat()
        val daysInMonth = dateTime.dayOfMonth().maximumValue.toFloat()
        return (dayOfMonth - 1) / (daysInMonth - 1) // on 1st, returns 0.0f; on last, returns 1.0f
    }
}