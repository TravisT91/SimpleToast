package com.engageft.fis.pscu.feature.achtransfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.FundingFundAchAccountRequest
import com.engageft.engagekit.rest.request.FundingFundFromDebitRequest
import com.engageft.engagekit.rest.request.ScheduledLoadAchAddRequest
import com.engageft.engagekit.rest.request.ScheduledLoadDebitAddRequest
import com.engageft.engagekit.utils.BackendDateTimeUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CreateTransferConfirmationViewModel(private val transferFundModel: TransferFundsModel): BaseEngageViewModel() {

    val createTransferSuccessObservable = SingleLiveEvent<Unit>()

    fun onCreateUpdateTransfer() {
        when (transferFundModel.fundSourceType) {
            FundSourceType.STANDARD_DEBIT -> {
                transferFromDebitSource(transferFundModel)
            }
            FundSourceType.STANDARD_ACH -> {
                transferFromAchAccount(transferFundModel)
            }
        }
    }

    private fun transferFromAchAccount(transferFundModel: TransferFundsModel) {
        val scheduledLoad = getNewScheduleLoad(transferFundModel)
        scheduledLoad.achAccountId = this.transferFundModel.fromId.toString()
        //TODO(aHashimi): Pass ThreatMetrix sessionId
        val request = ScheduledLoadAchAddRequest(scheduledLoad, "")

        val observable: Observable<BasicResponse> = when (transferFundModel.frequency) {
            ScheduleLoadFrequencyType.WEEKLY -> engageApi().postScheduledLoadACHAddWeekly(request.fieldMap)
            ScheduleLoadFrequencyType.EVERY_OTHER_WEEK -> engageApi().postScheduledLoadACHAddAltWeekly(request.fieldMap)
            ScheduleLoadFrequencyType.MONTHLY -> engageApi().postScheduledLoadACHAddMonthly(request.fieldMap)
            ScheduleLoadFrequencyType.ONCE -> {
                val req = FundingFundAchAccountRequest(
                        transferFundModel.fromId,
                        transferFundModel.amount.toString(),
                        transferFundModel.toId,
                        "")
                engageApi().postFundAchAccount(req.fieldMap)
            }
        }

        transfer(observable)
    }

    private fun transferFromDebitSource(transferFundModel: TransferFundsModel) {
        val scheduledLoad = getNewScheduleLoad(transferFundModel)
        scheduledLoad.ccAccountId = transferFundModel.fromId.toString()
        //TODO(aHashimi): Pass ThreatMetrix sessionId
        val request = ScheduledLoadDebitAddRequest(scheduledLoad, "")
        val observable: Observable<BasicResponse>
        when (transferFundModel.frequency) {
            ScheduleLoadFrequencyType.WEEKLY -> {
                observable = engageApi().postScheduledLoadDebitAddWeekly(request.fieldMap)
                transfer(observable)
            }
            ScheduleLoadFrequencyType.EVERY_OTHER_WEEK -> {
                observable = engageApi().postScheduledLoadDebitAddAltWeekly(request.fieldMap)
                transfer(observable)
            }
            ScheduleLoadFrequencyType.MONTHLY -> {
                observable = engageApi().postScheduledLoadDebitAddMonthly(request.fieldMap)
                transfer(observable)
            }
            ScheduleLoadFrequencyType.ONCE -> {
                onetimeTransferFromDebit(transferFundModel)
            }
        }
    }

    private fun onetimeTransferFromDebit(transferFundModel: TransferFundsModel) {
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        val debitSource = LoginResponseUtils.getFundDebitSourceById(response, transferFundModel.fromId)
                        val req = FundingFundFromDebitRequest(
                                transferFundModel.amount.toInt(),
                                transferFundModel.toId.toInt(),
                                debitSource,
                                "")
                        transfer(engageApi().postFundingFundFromDebit(req.fieldMap))
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }

    private fun getNewScheduleLoad(fundsModel: TransferFundsModel) : ScheduledLoad {
        val scheduledLoad = ScheduledLoad()
        // this is not needed by backend but Retrofit serialization
        scheduledLoad.scheduledLoadType = ScheduledLoad.PLANNED_LOAD_METHOD_ACH
        scheduledLoad.cardId = fundsModel.toId.toString()

        scheduledLoad.typeString = fundsModel.frequency.toString()
        scheduledLoad.amount = fundsModel.amount.toString()

        fundsModel.scheduleDate?.let { scheduleDate ->
            scheduledLoad.scheduleDate = BackendDateTimeUtils.getIso8601String(scheduleDate)
        }

        return scheduledLoad
    }

    private fun transfer(observable: Observable<BasicResponse>) {
        showProgressOverlayDelayed()

        compositeDisposable.add(observable
                .subscribeOn(Schedulers.io())
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

    private companion object {
        const val TAG = "CreateTransferConfirmationViewModel"
    }
}
class CreateTransferConfirmationViewModelFactory(private val transferFundsModel: TransferFundsModel) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreateTransferConfirmationViewModel(transferFundsModel) as T
    }
}