package com.engageft.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.feature.util.DisplayDateTimeUtils
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.FamilyInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class StatementsViewModel: BaseEngageViewModel() {
    private val dateOptions = mutableListOf<DateTime>()

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
            dateOptions.clear()

            val currDate = DateTime.now()
            var startDate = currDate.minusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay()
            val currDay = currDate.dayOfMonth

            //TODO get from backend DayOfMonth
            // if before 11th, then don't show last month
//            if (currDay < 11) {
//                // go back another month
//                startDate = startDate.minusMonths(1)
//            }
            if (currDay < familyInfo.dayOfMonthStatementAvailable) {
                // go back another month
                startDate = startDate.minusMonths(1)
            }
            dayOfMonthStatementAvailable = familyInfo.dayOfMonthStatementAvailable

            if (familyInfo != null && hasStatementsAvailable(familyInfo)) {
                var lastDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(familyInfo.isoCreateDate)
                if (lastDate == null) {
                    // go back 6 months
                    lastDate = DateTime.now().minusMonths(6)
                }

                // loop through previous dates and add to array
                dateOptions.add(startDate)
                var prevDate = startDate
                while (lastDate!!.isBefore(prevDate)) {
                    prevDate = prevDate.minusMonths(1)
                    dateOptions.add(prevDate)
                }
            }
            statementsObservable.value = dateOptions
        } ?: run {
            // throw error
//            refreshLoginResponse()
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
                        // TODO show error dialog
                    }
                }, { e ->
                    // TODO errorshow dialog
                    handleThrowable(e)
                })
        )
    }

    /**
     * Only show statements list if today is past the 11th of the month after the account was created
     *
     * @param familyInfo `FamilyInfo`
     * @return true if statements are available, false otherwise
     */
    fun hasStatementsAvailable(familyInfo: FamilyInfo): Boolean {
        val createDate = BackendDateTimeUtils.parseDateTimeFromIso8601String(familyInfo.isoCreateDate)
        if (createDate == null) {
            return false
        } else {
            val today = DateTime.now()
            if (DisplayDateTimeUtils.datesAreSameMonthAndYear(today, createDate)) {
                return false
            } else {
                val todayMinusOneMonth = today.minusMonths(1)
                if (today.dayOfMonth <= 11 && DisplayDateTimeUtils.datesAreSameMonthAndYear(todayMinusOneMonth, createDate)) {
                    return false
                }
            }
        }
        return true
    }
}