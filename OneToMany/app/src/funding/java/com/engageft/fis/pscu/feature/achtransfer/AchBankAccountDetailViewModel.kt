package com.engageft.fis.pscu.feature.achtransfer

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.AchAccountRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.engagekit.utils.engageApi
import com.ob.domain.lookup.AchAccountStatus
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AccountInfo
import com.ob.ws.dom.utility.AchAccountInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * AchBankAccountDetailViewModel
 * </p>
 * ViewModel that manages display data of an ACH bank account. Also handles deletion of an ACH account and navigating to verification step.
 * </p>
 * Created by Atia Hashimi 12/20/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AchBankAccountDetailViewModel: BaseCardLoadViewModel() {

    private companion object {
        const val ACCOUNT_NUMBER_FORMAT = "*******%s"
    }

    val accountName: ObservableField<String> = ObservableField("")
    val routingNumber: ObservableField<String> = ObservableField("")
    val accountNumber: ObservableField<String> = ObservableField("")
    var showButton: ObservableField<Boolean> = ObservableField(false)

    val bankDeleteSuccessObservable = SingleLiveEvent<Unit>()

    var achAccountInfoId: Long = 0L
    var achAccountInfo: AchAccountInfo? = null
    val checkingAccountTypeObservable = MutableLiveData<Boolean>()
    private var currentAccountInfo: AccountInfo? = null

    init {
        initData()
    }

    private fun initData() {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response.isSuccess && response is LoginResponse) {
                        if (achAccountInfoId != 0L) {
                            achAccountInfo = LoginResponseUtils.getAchAccountInfoById(response, achAccountInfoId)
                        }
                        currentAccountInfo = LoginResponseUtils.getCurrentAccountInfo(response)

                        achAccountInfo?.let { account ->
                            // populate data
                            accountName.set(account.bankName)
                            accountNumber.set(String.format(ACCOUNT_NUMBER_FORMAT, account.accountLastDigits))
                            routingNumber.set(account.routeNumber)
                            checkingAccountTypeObservable.value = account.isChecking
                            showButton.set(account.achAccountStatus != AchAccountStatus.VERIFIED)
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

    fun onDeleteAccount() {
        showProgressOverlayDelayed()
        compositeDisposable.add(engageApi().postDeleteAchAccount(AchAccountRequest(achAccountInfoId).fieldMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response.isSuccess) {
                        val loginResponse = EngageService.getInstance().storageManager.loginResponse
                        val currentCard = LoginResponseUtils.getCurrentCard(loginResponse)
                        EngageService.getInstance().storageManager.removeLoginResponse()
                        EngageService.getInstance().storageManager.clearScheduledLoadsResponse(currentCard)
                        refreshLoginResponse(bankDeleteSuccessObservable)
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
