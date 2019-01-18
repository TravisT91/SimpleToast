package com.engageft.fis.pscu.feature.budgets

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.budgets.extension.getCategories
import com.engageft.fis.pscu.feature.budgets.model.BudgetModel
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
        progressOverlayShownObservable.value = true
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
                                    var totalBudgetModel: BudgetModel? = null
                                    val spent = BigDecimal(budgetAmountSpent)
                                    val total = BigDecimal(budgetAmount)
                                    totalBudgetModel = BudgetModel(
                                            // Title is filled in by fragment, which can access Context
                                            spentAmount = spent,
                                            budgetAmount = total,
                                            fractionTimePeriodPassed = fractionTimePeriodPassed)

                                    // categories
                                    val categorySpendings = getCategories(true)

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