package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.GoalTransferBalanceRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.feature.budgets.extension.isEqualTo
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class GoalSingleTransferConfirmationViewModel(
        val goalId: Long,
        val transferAmount: BigDecimal,
        val transferFromType: GoalSingleTransferViewModel.TransferType): GoalDeleteViewModel() {


    val transferSuccessObservable = SingleLiveEvent<Unit>()
    val goalInfoObservable = MutableLiveData<GoalInfo>()

    init {
        initGoalData()
    }

    fun shouldPromptToDeleteGoal(): Boolean {
        return goalInfoObservable.value?.let { goalInfo ->
            transferFromType == GoalSingleTransferViewModel.TransferType.GOAL
                    && goalInfo.isAchieved
                    && goalInfo.fundAmount.isEqualTo(transferAmount)
        } ?: false
    }

    fun transfer() {
        val request = GoalTransferBalanceRequest(goalId, transferAmount.toString())
        when (transferFromType) {
            GoalSingleTransferViewModel.TransferType.SPENDING_BALANCE -> oneTimeTransfer(engageApi().postGoalTransferToGoal(request.fieldMap))
            GoalSingleTransferViewModel.TransferType.GOAL -> oneTimeTransfer(engageApi().postGoalTransferToBalance(request.fieldMap))
        }
    }

    fun onTransferAndDelete() {
        onTransferAndDelete(goalId)
    }

    private fun initGoalData() {
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
                                    goalInfoObservable.value = goalInfo
                                } ?: kotlin.run {
                                    throw IllegalStateException("Goal not found")
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

    private fun oneTimeTransfer(oneTimeTransferObservable: Observable<BasicResponse>) {
        showProgressOverlayImmediate()

        compositeDisposable.add(oneTimeTransferObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response.isSuccess) {
                        refreshLoginResponse(transferSuccessObservable)
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
class GoalSingleTransferConfirmationViewModelFactory(private val goalId: Long, val amount: BigDecimal, private val transferType: GoalSingleTransferViewModel.TransferType) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalSingleTransferConfirmationViewModel(goalId, amount, transferType) as T
    }
}