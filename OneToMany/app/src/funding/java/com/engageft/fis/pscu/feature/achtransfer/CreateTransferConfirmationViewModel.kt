package com.engageft.fis.pscu.feature.achtransfer

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.FundingFundAchAccountRequest
import com.engageft.engagekit.rest.request.ScheduledLoadAchAddRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.ValidationErrors
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class CreateTransferConfirmationViewModel: BaseEngageViewModel() {

    enum class NavigationEvent {
        TRANSFER_SUCCESS
    }

    var frequencyType = ""
    var amount = ""
    var scheduledDate1: DateTime? = null
    var scheduledDate2: DateTime? = null
    var achAccountInfoId = -1L
    var cardId = -1L

    val navigationEventObservable: MutableLiveData<NavigationEvent> = MutableLiveData()

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
        scheduledLoad.isExternal = false
        scheduledLoad.scheduledLoadType = ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER
        scheduledLoad.cardId = cardId.toString()
        scheduledLoad.achAccountId = achAccountInfoId.toString()

        scheduledLoad.typeString = frequencyType
        scheduledLoad.amount = amount

        scheduledDate1?.let {
            scheduledLoad.scheduleDate = BackendDateTimeUtils.getYMDStringFromDateTime(it)
        }
        scheduledDate2?.let {
            scheduledLoad.scheduleDate2 = BackendDateTimeUtils.getYMDStringFromDateTime(it)
        }

        return scheduledLoad
    }

    private fun submitScheduleLoad(scheduledLoad: ScheduledLoad, sessionId: String = "") {
        progressOverlayShownObservable.value = true

        val request = ScheduledLoadAchAddRequest(EngageService.getInstance().authManager.authToken,
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
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess) {
                                EngageService.getInstance().clearLoginAndDashboardResponses()
                                navigationEventObservable.value = NavigationEvent.TRANSFER_SUCCESS
                            } else {
                                showBackendErrorOrGenericMessage(response)
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }

    private fun executeOneTimeAchLoad(sessionId: String = "") {
        progressOverlayShownObservable.value = true

        val request = FundingFundAchAccountRequest(
                EngageService.getInstance().authManager.authToken,
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
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess) {
                                EngageService.getInstance().clearLoginAndDashboardResponses()
                                navigationEventObservable.value = NavigationEvent.TRANSFER_SUCCESS
                            } else {
                                showBackendErrorOrGenericMessage(response)
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }

    private fun showBackendErrorOrGenericMessage(response: BasicResponse) {
        if (response.message.isNotEmpty()) {
            dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.message)
        } else if (response is ValidationErrors) {
            if (response.error.isNotEmpty()) {
                dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.error.elementAt(0).message)
            } else {
                handleUnexpectedErrorResponse(response)
            }
        } else {
            handleUnexpectedErrorResponse(response)
        }
    }
}