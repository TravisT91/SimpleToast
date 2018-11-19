package com.engageft.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class StatementsViewModel: BaseEngageViewModel() {

    val statementsObservable = MutableLiveData<List<DateTime>>()
    var dayOfMonthStatementAvailable = 0

    init {
        loginResponse?.let {
            initMonthlyStatements()
        } ?: run {
            refreshAndFetchStatements()
        }
    }

    private fun initMonthlyStatements() {
        loginResponse?.let { loginResponse ->
            val familyInfo = loginResponse.familyInfo
            val dateOptions = mutableListOf<DateTime>()

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
            statementsObservable.value = dateOptions
        } ?: run {
            refreshAndFetchStatements()
        }
    }

    private fun refreshAndFetchStatements(): Boolean {
        return compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        loginResponse = response
                        initMonthlyStatements()
                    } else {
                        dialogInfoObservable.value = DialogInfo()
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }
}