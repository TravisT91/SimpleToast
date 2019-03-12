package com.engageft.feature.goals

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.GoalRequest
import com.engageft.engagekit.utils.engageApi
import com.ob.ws.dom.LoginResponse
import io.reactivex.schedulers.Schedulers

open class GoalDeleteViewModel: BaseGoalsViewModel() {

    val deleteSuccessObservable = SingleLiveEvent<Unit>()

    fun onTransferAndDelete(goalId: Long) {
        showProgressOverlayImmediate()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        // don't hide progress here yet
                        deleteGoal(goalId)
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

    private fun deleteGoal(goalId: Long) {
        val request = GoalRequest(goalId)
        compositeDisposable.add(engageApi().postGoalRemove(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe({ response ->
                            if (response.isSuccess) {
                                // hide progressbar after loginResponse & goals refresh
                                refreshLoginResponse(deleteSuccessObservable)
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