package com.engageft.feature.budgets

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.feature.budgets.extension.getCategoriesSortedByBudgetAmountDescending
import com.engageft.feature.budgets.model.BudgetModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import java.math.BigDecimal

/**
 * BudgetsListViewModel
 * <p>
 * ViewModel to support displaying a list of budgets
 * <p>
 * Created by kurteous on 1/16/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class BudgetsListViewModel : BaseEngageViewModel() {

    val budgetsObservable: MutableLiveData<Pair<BudgetModel, List<BudgetModel>>> = MutableLiveData()

    fun init() {
        progressOverlayShownObservable.postValue(true)
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess && response is LoginResponse) {
                                val fractionTimePeriodPassed = fractionOfCurrentMonthPassed() // TODO this will be different within first 30 days

                                response.budgetInfo?.apply {
                                    // setup
                                    val hasBudget = false
                                    val isFirst30 = false

                                    // total spent
                                    val totalBudgetModel = BudgetModel(
                                            // categoryName is not set for total
                                            // title is filled in by fragment, which can access Context
                                            spentAmount = BigDecimal(budgetAmountSpent),
                                            budgetAmount = BigDecimal(budgetAmount),
                                            fractionTimePeriodPassed = fractionTimePeriodPassed)

                                    // categories
                                    val categorySpendings = getCategoriesSortedByBudgetAmountDescending(withOther = false, isInFirst30Days = isFirst30).toMutableList()
                                    // add other spending
                                    categorySpendings.add(otherSpending)
                                    var categoryBudgetModels = mutableListOf<BudgetModel>()
                                    for (categorySpending in categorySpendings) {
                                        categoryBudgetModels.add(
                                                BudgetModel(
                                                        categoryName = categorySpending.category,
                                                        spentAmount = BigDecimal(categorySpending.amountSpent),
                                                        budgetAmount = BigDecimal(categorySpending.budgetAmount),
                                                        fractionTimePeriodPassed =  fractionTimePeriodPassed
                                                )
                                        )
                                    }

                                    progressOverlayShownObservable.postValue(false)
                                    budgetsObservable.postValue(Pair(totalBudgetModel, categoryBudgetModels))
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

    private fun fractionOfCurrentMonthPassed(): Float {
        val dateTime = DateTime.now()
        val dayOfMonth = dateTime.dayOfMonth.toFloat()
        val daysInMonth = dateTime.dayOfMonth().maximumValue.toFloat()
        return (dayOfMonth - 1) / (daysInMonth - 1) // on 1st, returns 0.0f; on last, returns 1.0f
    }
}