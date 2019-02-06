package com.engageft.fis.pscu.feature

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * AccountSettingsViewModel
 * <p>
 * ViewModel for Account Settings landing screen.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class AccountSettingsViewModel : BaseEngageViewModel() {
    enum class AccountSettingsNavigation {
        NONE,
        LOGOUT
    }

    val navigationObservable = MutableLiveData<AccountSettingsNavigation>()
    var cardSecondaryEnable = ObservableField<Boolean>(false)
    var cardStatementsEnable = ObservableField<Boolean>(false)

    init {
        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response is LoginResponse) {
                        val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                        if (debitCardInfo.cardPermissionsInfo.isCardStatementsEnabled
                                && debitCardInfo.cardPermissionsInfo.isCardStatementsAllowable) {
                            cardStatementsEnable.set(true)
                        }
                        if (debitCardInfo.cardPermissionsInfo.isCardSecondaryEnabled) {
                            cardSecondaryEnable.set(true)
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

    fun onLogoutClicked() {
        EngageService.getInstance().authManager.logout()
        navigationObservable.value = AccountSettingsNavigation.LOGOUT
    }
}