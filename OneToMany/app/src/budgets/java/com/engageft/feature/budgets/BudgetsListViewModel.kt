package com.engageft.feature.budgets

import androidx.annotation.ColorInt
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.feature.budgets.extension.budgetStatus
import com.engageft.feature.budgets.extension.getCategoriesSortedByBudgetAmountDescending
import com.engageft.feature.budgets.extension.isGreaterThan
import com.engageft.feature.budgets.extension.isLessThan
import com.engageft.feature.budgets.extension.isZero
import com.engageft.feature.budgets.recyclerview.BudgetItem
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.ob.ws.dom.LoginResponse
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import utilGen1.StringUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

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

    private lateinit var spentNormalFormat: String
    private lateinit var spentOverFormat: String
    @ColorInt private var spentColorNormal: Int = 0
    @ColorInt private var spentColorOverBudget: Int = 0

    @ColorInt private var progressColorNormal: Int = 0
    @ColorInt private var progressColorHighSpendingTrend: Int = 0
    @ColorInt private var progressColorOverBudget: Int = 0

    fun init(totalSpentTitle: String,
             otherSpendingTitle: String,
             spentNormalFormat: String,
             spentOverFormat: String,
             @ColorInt spentColorNormal: Int,
             @ColorInt spentColorOverBudget: Int,
             @ColorInt progressColorNormal: Int,
             @ColorInt progressColorHighSpendingTrend: Int,
             @ColorInt progressColorOverBudget: Int) {

        this.spentNormalFormat = spentNormalFormat
        this.spentOverFormat = spentOverFormat
        this.spentColorNormal = spentColorNormal
        this.spentColorOverBudget = spentColorOverBudget

        this.progressColorNormal = progressColorNormal
        this.progressColorHighSpendingTrend = progressColorHighSpendingTrend
        this.progressColorOverBudget = progressColorOverBudget

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
                                    var spentAmount = BigDecimal(budgetAmountSpent).abs()   // reused for computing categorySpending values
                                                                                            // amountSpent is always negative from backend
                                    var budgetAmount = BigDecimal(budgetAmount)             // reused for computing categorySpending values
                                    var budgetStatus = budgetStatus()                       // reused for computing categorySpendingValues
                                    val totalBudgetItem = BudgetItem(
                                            categoryName = BudgetConstants.CATEGORY_NAME_FE_TOTAL_SPENDING,
                                            title = totalSpentTitle,
                                            spent = spentString(spentAmount, budgetAmount),
                                            spentColor = spentColor(budgetStatus),
                                            progress = progress(spentAmount, budgetAmount),
                                            progressColor = progressColor(budgetStatus),
                                            fractionTimePeriodPassed = fractionTimePeriodPassed
                                    )

                                    // categories
                                    val categorySpendings = getCategoriesSortedByBudgetAmountDescending(withOther = false, isInFirst30Days = isFirst30).toMutableList()
                                    var categoryBudgetItems = mutableListOf<BudgetItem>()
                                    for (categorySpending in categorySpendings) {
                                        spentAmount = BigDecimal(categorySpending.amountSpent).abs()    // amountSpent is always negative from backend
                                        budgetAmount = BigDecimal(categorySpending.budgetAmount)
                                        budgetStatus = categorySpending.budgetStatus()
                                        categoryBudgetItems.add(
                                                BudgetItem(
                                                        categoryName = categorySpending.category,
                                                        title = EngageService.getInstance().storageManager.getBudgetCategoryDescription(categorySpending.category, Locale.getDefault().language),
                                                        spent = spentString(spentAmount, budgetAmount),
                                                        spentColor = spentColor(budgetStatus),
                                                        progress = progress(spentAmount, budgetAmount),
                                                        progressColor = progressColor(budgetStatus),
                                                        fractionTimePeriodPassed = fractionTimePeriodPassed
                                                )
                                        )
                                    }
                                    // add other spending
                                    otherSpending?.let { otherSpendingCategory ->
                                        spentAmount = BigDecimal(otherSpendingCategory.amountSpent).abs()   // amountSpent is always negative from backend
                                        budgetAmount = BigDecimal(otherSpendingCategory.budgetAmount)
                                        budgetStatus = otherSpendingCategory.budgetStatus()
                                        categoryBudgetItems.add(
                                                BudgetItem(
                                                        categoryName = otherSpendingCategory.category,
                                                        title = otherSpendingTitle,
                                                        spent = spentString(spentAmount, budgetAmount),
                                                        spentColor = spentColor(budgetStatus),
                                                        progress = progress(spentAmount, budgetAmount),
                                                        progressColor = progressColor(budgetStatus),
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

    private fun spentString(spentAmount: BigDecimal, budgetAmount: BigDecimal): String {
        return if (spentAmount.isGreaterThan(budgetAmount)) {
            String.format(spentOverFormat, StringUtils.formatCurrencyString(spentAmount.minus(budgetAmount).toFloat()))
        } else {
            String.format(spentNormalFormat, StringUtils.formatCurrencyString(spentAmount.toFloat()), StringUtils.formatCurrencyString(budgetAmount.toFloat()))
        }
    }

    @ColorInt
    private fun spentColor(budgetStatus: BudgetConstants.BudgetStatus): Int {
        return if (budgetStatus == BudgetConstants.BudgetStatus.OVER_BUDGET) {
            spentColorOverBudget
        } else {
            spentColorNormal
        }
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

    @ColorInt
    private fun progressColor(budgetStatus: BudgetConstants.BudgetStatus): Int {
        return when (budgetStatus) {
            BudgetConstants.BudgetStatus.NORMAL -> progressColorNormal
            BudgetConstants.BudgetStatus.HIGH_SPENDING_TREND -> progressColorHighSpendingTrend
            BudgetConstants.BudgetStatus.OVER_BUDGET -> progressColorOverBudget
        }
    }

    private fun fractionOfCurrentMonthPassed(): Float {
        val dateTime = DateTime.now()
        val dayOfMonth = dateTime.dayOfMonth.toFloat()
        val daysInMonth = dateTime.dayOfMonth().maximumValue.toFloat()
        return (dayOfMonth - 1) / (daysInMonth - 1) // on 1st, returns 0.0f; on last, returns 1.0f
    }
}