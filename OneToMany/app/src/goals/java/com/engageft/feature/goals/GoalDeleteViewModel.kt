package com.engageft.feature.goals

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.GoalRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class GoalDeleteViewModel: BaseEngageViewModel() {

    enum class DeleteStatus {
        SUCCESS
    }

    val deleteStatusObservable = MutableLiveData<DeleteStatus>()

    fun onTransferAndDelete(goalId: Long) {
        showProgressOverlayImmediate()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess) {
                                // hide progressbar after loginResponse & goals refresh
                                refreshData()
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


    private fun refreshData() {
        compositeDisposable.add(EngageService.getInstance().refreshLoginObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // if response is success hide progressOverlay after refreshing goals
                    if (response is LoginResponse) {
                        refreshGoals(LoginResponseUtils.getCurrentCard(response))
                    } else {
                        dismissProgressOverlay()
                        // take user to Goals Success even if refresh is not successful
                        deleteStatusObservable.value = DeleteStatus.SUCCESS
                    }
                }, {
                    dismissProgressOverlay()
                    // take user to Goals Success even if refresh is not successful
                    deleteStatusObservable.value = DeleteStatus.SUCCESS
                })
        )
    }

    private fun refreshGoals(debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            dismissProgressOverlay()
                            // take user to Goals Success even if refresh is not successful
                            deleteStatusObservable.value = DeleteStatus.SUCCESS
                        }, {
                            dismissProgressOverlay()
                            // take user to Goals Success even if refresh is not successful
                            deleteStatusObservable.value = DeleteStatus.SUCCESS
                        })
        )
    }
}