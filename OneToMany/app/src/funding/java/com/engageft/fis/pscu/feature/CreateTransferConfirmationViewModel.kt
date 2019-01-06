package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.FundingFundAchAccountRequest
import com.engageft.engagekit.rest.request.ScheduledLoadAchAddRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.ValidationErrors
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class CreateTransferConfirmationViewModel: BaseEngageViewModel() {

    enum class NavigationEvent {
        TRANSFER_SUCCESS
    }

    private var currentCard: DebitCardInfo? = null

    var frequencyType = ""
    var amount = ""
    var scheduledDate1: DateTime? = null
    var scheduledDate2: DateTime? = null
    var achAccountInfoId = -1L
    var cardId = -1L

    val navigationEventObservable: MutableLiveData<NavigationEvent> = MutableLiveData()

    init {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response is LoginResponse) {
                        currentCard = LoginResponseUtils.getCurrentCard(response)
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )
    }

    fun onCreateUpdateTransfer() {
        if (frequencyType == ScheduledLoad.SCHED_LOAD_TYPE_ONCE) {
            executeOneTimeAchLoad()
        } else {
            submitScheduleLoad()
        }
    }

    private fun submitScheduleLoad(sessionId: String = "") {
        progressOverlayShownObservable.value = true

        // schedule it
        val newLoad = ScheduledLoad()
        newLoad.isExternal = false
        newLoad.scheduledLoadType = ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER
        newLoad.typeString = frequencyType
        newLoad.amount = amount

        scheduledDate1?.let {
            newLoad.scheduleDate = BackendDateTimeUtils.getYMDStringFromDateTime(it)
        }
        scheduledDate2?.let {
            newLoad.scheduleDate2 = BackendDateTimeUtils.getYMDStringFromDateTime(it)
        }

        newLoad.achAccountId = achAccountInfoId.toString()
        newLoad.cardId = cardId.toString()

        val request = ScheduledLoadAchAddRequest(EngageService.getInstance().authManager.authToken,
                newLoad,
                sessionId)
        val observable: Observable<BasicResponse> = when (frequencyType) {
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
                                if (response is ValidationErrors) {
                                    if (response.error.isNotEmpty()) {
                                        dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.error.elementAt(0).message)
                                    } else {
                                        handleUnexpectedErrorResponse(response)
                                    }
                                } else {
                                    handleUnexpectedErrorResponse(response)
                                }
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
                                if (response is ValidationErrors) {
                                    if (response.error.isNotEmpty()) {
                                        dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.SERVER_ERROR, message = response.error.elementAt(0).message)
                                    } else {
                                        handleUnexpectedErrorResponse(response)
                                    }
                                } else {
                                    handleUnexpectedErrorResponse(response)
                                }
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }
}