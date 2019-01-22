package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
/**
 * StatementsViewModel
 *
 * ViewModel for handling of statements lists
 *
 * Created by Atia Hashimi 11/22/218
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class StatementsViewModel: BaseEngageViewModel() {

    val statementsObservable = MutableLiveData<List<DateTime>>()
    var dayOfMonthStatementAvailable = 0

    init {
        refreshAndFetchStatements()
    }

    private fun initMonthlyStatements(loginResponse: LoginResponse) {
        val familyInfo = loginResponse.familyInfo
        val dateOptions = mutableListOf<DateTime>()

        if (familyInfo.isoStatementBeginDate.isNotEmpty() && familyInfo.isoStatementEndDate.isNotEmpty()) {
            val startDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(familyInfo.isoStatementBeginDate)
            val lastDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(familyInfo.isoStatementEndDate)

            dayOfMonthStatementAvailable = familyInfo.dayOfMonthStatementAvailable

            // add from latest available months so that it's displayed from recent to oldest
            dateOptions.add(lastDate)
            var tempDate = lastDate
            while (startDate.isBefore(tempDate)) {
                tempDate = tempDate.minusMonths(1)
                dateOptions.add(tempDate)
            }
        }
        statementsObservable.value = dateOptions
    }

    private fun refreshAndFetchStatements() {
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        initMonthlyStatements(response)
                    } else {
                        dialogInfoObservable.value = DialogInfo()
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }
}