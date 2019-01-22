package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo

class GoalsListViewModel: BaseEngageViewModel() {
    var canEditGoal = false
    var goalsContributed = ""
    val goalsListObservable = MutableLiveData<List<GoalInfo>>()
    private var goalsList = mutableListOf<GoalInfo>()

    init {
        initData(false)
    }

    fun refreshViews(useCache: Boolean) {
        initData(useCache)
    }

    private fun initData(useCache: Boolean) {
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response is LoginResponse) {
                        goalsContributed = response.goalsContributed
                        canEditGoal = LoginResponseUtils.canEditGoals(response)
                        getGoals(LoginResponseUtils.getCurrentCard(response), useCache)
                    } else {
                        dialogInfoObservable.value = DialogInfo()
                    }
                }, { e ->
                    handleThrowable(e)
                })
        )
    }

    private fun getGoals(debitCardInfo: DebitCardInfo, useCache: Boolean) {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, useCache)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess && response is GoalsResponse) {
                                if (goalsList != response.goals) {
                                    // refresh views
                                    goalsList = response.goals
                                    goalsListObservable.value = goalsList
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        })
        )
    }
}