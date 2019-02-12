package com.engageft.feature.goals

import com.engageft.engagekit.rest.request.GoalTransferBalanceRequest
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.BasicResponse
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class GoalSingleTransferConfirmationViewModel(val goalId: Long, val amount: BigDecimal, val transferType: GoalSingleTransferViewModel.TransferType): BaseEngageViewModel() {

    fun transferFunds() {
//        goalInfo?.let {
//            val request = GoalTransferBalanceRequest(it.goalId, amount)
//            when (toSelectionType) {
//                GoalSingleTransferViewModel.TransferType.SPENDING_BALANCE -> oneTimeTransfer(engageApi().postGoalTransferToBalance(request.fieldMap))
//                GoalSingleTransferViewModel.TransferType.GOAL -> oneTimeTransfer(engageApi().postGoalTransferToGoal(request.fieldMap))
//            }
//        }
    }

    private fun oneTimeTransfer(oneTimeTransferObservable: Observable<BasicResponse>) {
        showProgressOverlayImmediate()

        compositeDisposable.add(oneTimeTransferObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response.isSuccess) {
                        // refresh Login Response
                    } else {
                        dismissProgressOverlay()
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }
}