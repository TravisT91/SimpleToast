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
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.handleBackendErrorForForms
import com.ob.ws.dom.BasicResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CardLoadTransferConfirmationViewModel(private val cardLoadTransfer: CardLoadTransfer): BaseEngageViewModel() {

    val createTransferSuccessObservable = SingleLiveEvent<Unit>()

    fun onCreateTransfer() {
        when (cardLoadTransfer.fundSourceType) {
            FundSourceType.STANDARD_DEBIT -> {
                transferFromDebitSource(cardLoadTransfer)
            }
            FundSourceType.STANDARD_ACH -> {
                transferFromAchAccount(cardLoadTransfer)
            }
        }
    }

    private fun transferFromAchAccount(transferFundModel: CardLoadTransfer) {
        val scheduledLoad = getNewScheduleLoad(transferFundModel)
        scheduledLoad.achAccountId = this.cardLoadTransfer.fromId.toString()
        //TODO(aHashimi): Pass ThreatMetrix sessionId when integrated
        val request = ScheduledLoadAchAddRequest(scheduledLoad, "")

        val observable: Observable<BasicResponse> = when (transferFundModel.frequency) {
            ScheduleLoadFrequencyType.WEEKLY -> engageApi().postScheduledLoadACHAddWeekly(request.fieldMap)
            ScheduleLoadFrequencyType.EVERY_OTHER_WEEK -> engageApi().postScheduledLoadACHAddAltWeekly(request.fieldMap)
            ScheduleLoadFrequencyType.MONTHLY -> engageApi().postScheduledLoadACHAddMonthly(request.fieldMap)
            ScheduleLoadFrequencyType.ONCE -> {
                val req = FundingFundAchAccountRequest(
                        achAccountId = transferFundModel.fromId,
                        cardId = transferFundModel.toId,
                        amount = transferFundModel.amount.toString())
                engageApi().postFundAchAccount(req.fieldMap)
            }
        }

        transfer(observable)
    }

    private fun transferFromDebitSource(transferFundModel: CardLoadTransfer) {
        val scheduledLoad = getNewScheduleLoad(transferFundModel)
        scheduledLoad.ccAccountId = transferFundModel.fromId.toString()
        //TODO(aHashimi): Pass ThreatMetrix sessionId when integrated
        val request = ScheduledLoadDebitAddRequest(scheduledLoad)

        val observable: Observable<BasicResponse> = when (transferFundModel.frequency) {
            ScheduleLoadFrequencyType.WEEKLY -> engageApi().postScheduledLoadDebitAddWeekly(request.fieldMap)
            ScheduleLoadFrequencyType.EVERY_OTHER_WEEK -> engageApi().postScheduledLoadDebitAddAltWeekly(request.fieldMap)
            ScheduleLoadFrequencyType.MONTHLY -> engageApi().postScheduledLoadDebitAddMonthly(request.fieldMap)
            ScheduleLoadFrequencyType.ONCE -> {
                val req = FundingFundFromDebitRequest(
                        ccAccountId = transferFundModel.fromId,
                        cardId = transferFundModel.toId,
                        amount = transferFundModel.amount)
                engageApi().postFundingFundFromDebit(req.fieldMap)
            }
        }
        transfer(observable)
    }

    private fun getNewScheduleLoad(fundsModel: CardLoadTransfer) : ScheduledLoad {
        val scheduledLoad = ScheduledLoad()
        // this is not needed by backend but Retrofit serialization
        scheduledLoad.scheduledLoadType = ScheduledLoad.PLANNED_LOAD_METHOD_BANK_TRANSFER
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
        const val TAG = "CardLoadTransferConfirmationViewModel"
    }
}
class CreateTransferConfirmationViewModelFactory(private val transferFundsModel: CardLoadTransfer) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CardLoadTransferConfirmationViewModel(transferFundsModel) as T
    }
}