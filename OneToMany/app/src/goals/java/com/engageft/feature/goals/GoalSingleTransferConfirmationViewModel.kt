package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.GoalTransferBalanceRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class GoalSingleTransferConfirmationViewModel(val goalId: Long, val amount: BigDecimal, val transferFromType: GoalSingleTransferViewModel.TransferType): BaseEngageViewModel() {
    var goalNameObservable = MutableLiveData<String>()

    init {
        initGoalData()
    }

    fun initGoalData() {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        initGoal(LoginResponseUtils.getCurrentCard(response))
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

    private fun initGoal(debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is GoalsResponse) {
                                response.goals.find { goalInfo ->
                                    goalInfo.goalId == goalId
                                }?.let { goalInfo ->
                                    if (transferFromType == GoalSingleTransferViewModel.TransferType.GOAL) {
                                        goalNameObservable.value = goalInfo.name
                                    }
                                } ?: kotlin.run {
                                    throw IllegalArgumentException("Goal not found")
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    fun transferFunds() {
        val request = GoalTransferBalanceRequest(goalId, amount.toString())
        when (transferFromType) {
            GoalSingleTransferViewModel.TransferType.SPENDING_BALANCE -> oneTimeTransfer(engageApi().postGoalTransferToBalance(request.fieldMap))
            GoalSingleTransferViewModel.TransferType.GOAL -> oneTimeTransfer(engageApi().postGoalTransferToGoal(request.fieldMap))
        }
    }

    private fun oneTimeTransfer(oneTimeTransferObservable: Observable<BasicResponse>) {
        showProgressOverlayImmediate()

        compositeDisposable.add(oneTimeTransferObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response.isSuccess) {
                        // todo refresh Login Response
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
class GoalSingleTransferConfirmationViewModelFactory(private val goalId: Long, val amount: BigDecimal, val transferType: GoalSingleTransferViewModel.TransferType) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalSingleTransferConfirmationViewModel(goalId, amount, transferType) as T
    }
}