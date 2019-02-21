package com.engageft.fis.pscu.feature.achtransfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.FundingFundAchAccountRequest
import com.engageft.engagekit.rest.request.ScheduledLoadAchAddRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.feature.goals.GoalDetailViewModel
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.BasicResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class CreateTransferConfirmationViewModel: BaseEngageViewModel() {

    var frequencyType = ""
    var amount = ""
    var scheduledDate1: DateTime? = null
    var scheduledDate2: DateTime? = null
    var achAccountInfoId = -1L
    var cardId = -1L

    val createTransferSuccessObservable = SingleLiveEvent<Unit>()

    fun onCreateUpdateTransfer() {
        // create transfer
        if (frequencyType == ScheduledLoad.SCHED_LOAD_TYPE_ONCE) {
            executeOneTimeAchLoad()
        } else {
            submitScheduleLoad(getNewScheduleLoad())
        }
    }

    private fun getNewScheduleLoad() : ScheduledLoad {
        val scheduledLoad = ScheduledLoad()
        // todo shouldn't need it
//        scheduledLoad.isExternal = false
        scheduledLoad.scheduledLoadType = ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER
        scheduledLoad.cardId = cardId.toString()
        scheduledLoad.achAccountId = achAccountInfoId.toString()

        scheduledLoad.typeString = frequencyType
        scheduledLoad.amount = amount

        scheduledDate1?.let {
            scheduledLoad.scheduleDate = BackendDateTimeUtils.getIso8601String(it)
        }
        scheduledDate2?.let {
            scheduledLoad.scheduleDate2 = BackendDateTimeUtils.getIso8601String(it)
        }

        return scheduledLoad
    }

    private fun submitScheduleLoad(scheduledLoad: ScheduledLoad, sessionId: String = "") {
        dismissProgressOverlayImmediate()

        val request = ScheduledLoadAchAddRequest(
                scheduledLoad,
                sessionId)
        val observable: Observable<BasicResponse> = when (scheduledLoad.typeString) {
            ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY -> EngageService.getInstance().engageApiInterface.postScheduledLoadACHAddTwiceMonthly(request.fieldMap)
            ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> EngageService.getInstance().engageApiInterface.postScheduledLoadACHAddWeekly(request.fieldMap)
            ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> EngageService.getInstance().engageApiInterface.postScheduledLoadACHAddMonthly(request.fieldMap)
            else -> EngageService.getInstance().engageApiInterface.postScheduledLoadACHAddMonthly(request.fieldMap)
        }

        compositeDisposable.add(
                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess) {
                                EngageService.getInstance().clearLoginAndDashboardResponses()
                                createTransferSuccessObservable.call()
                            } else {
                                handleBackendErrorForForms(response, "$TAG: creating a recurring transfer failed.")
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    private fun executeOneTimeAchLoad(sessionId: String = "") {
        showProgressOverlayImmediate()

        val request = FundingFundAchAccountRequest(
                achAccountInfoId,
                amount,
                cardId,
                sessionId
        )
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postFundAchAccount(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess) {
                                EngageService.getInstance().clearLoginAndDashboardResponses()
                                createTransferSuccessObservable.call()
                            } else {
                                handleBackendErrorForForms(response, "$TAG: creating one-time transfer failed.")
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    private companion object {
        const val TAG = "CreateTransferConfirmationViewModel"
    }
}