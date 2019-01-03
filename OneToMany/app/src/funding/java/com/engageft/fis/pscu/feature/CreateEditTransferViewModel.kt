package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AccountInfo
import com.ob.ws.dom.utility.AchAccountInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CreateEditTransferViewModel: BaseEngageViewModel() {

    var achAccountList: MutableLiveData<List<AchAccountInfo>> = MutableLiveData()
    var currentAccountList: MutableLiveData<List<AccountInfo>> = MutableLiveData()

    init {
        progressOverlayShownObservable.value = true

        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response is LoginResponse) {
                        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        achAccountList.value = response.achAccountList
                        currentAccountList.value = response.accountList
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