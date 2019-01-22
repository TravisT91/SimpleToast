package com.engageft.fis.pscu.feature.secondaryusers

import com.engageft.fis.pscu.feature.BaseEngageViewModel

/**
 * Created by joeyhutchins on 1/15/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SecondaryUserListViewModel: BaseEngageViewModel() {

    private companion object {
//        private const val HISTORICAL_LOADS_LIST_MAX_COUNT = 5
    }

//    enum class BankAccountStatus {
//        NO_BANK_ACCOUNT,
//        UNVERIFIED_BANK_ACCOUNT,
//        VERIFIED_BANK_ACCOUNT
//    }
//
//    var achBankAccountId = 0L
//    var accountInfo: AccountInfo? = null
//    val achAccountsListAndStatusObservable = MutableLiveData<AchBankAccountListAndStatus>()
//    val achScheduledLoadListObservable = MutableLiveData<List<ScheduledLoad>>()
//    val achHistoricalLoadListObservable = MutableLiveData<List<AchLoadInfo>>()
//
//    private var loginResponse: LoginResponse? = null
//    private var shouldHideProgressOverlay = false

    init {
        initSecondaryUsersList()
    }

    fun refreshViews() {
        // for this main screen to show the updated to item correctly.
//        if (this.loginResponse != EngageService.getInstance().storageManager.loginResponse) {
//            initSecondaryUsersList()
//        }
    }

    fun isBankVerified(): Boolean {
//        achAccountsListAndStatusObservable.value?.let {
//            if (it.bankStatus == BankAccountStatus.VERIFIED_BANK_ACCOUNT) {
//                return true
//            }
//        }
        return false
    }

    private fun initSecondaryUsersList() {
//        progressOverlayShownObservable.value = true
//
//        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ response ->
//                    // don't hide progressOverlay just yet
//                    if (response is LoginResponse) {
//                        loginResponse = response
//                        accountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
//                        val currentCard = LoginResponseUtils.getCurrentCard(response)
//                        // the order of invoking these methods don't matter
//                        initBankAccountStatusAndList(response.achAccountList)
//                        // hide progress when the following two API calls are done.
//                        shouldHideProgressOverlay = false
//                        getScheduledLoads(currentCard)
//                        getHistoricalLoads(currentCard)
//                    } else {
//                        progressOverlayShownObservable.value = false
//                        handleUnexpectedErrorResponse(response)
//                    }
//                }, { e ->
//                    progressOverlayShownObservable.value = false
//                    handleThrowable(e)
//                })
//        )
    }

//    private fun getScheduledLoads(currentCard: DebitCardInfo) {
//        compositeDisposable.add(
//                EngageService.getInstance().getScheduledLoadsResponseObservable(EngageService.getInstance().authManager.authToken, currentCard, false)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({ response ->
//                            shouldHideProgressOverlay(true)
//                            if (response.isSuccess && response is ScheduledLoadsResponse) {
//                                achScheduledLoadListObservable.value = ScheduledLoadUtils.getScheduledLoads(response)
//                            } else {
//                                handleUnexpectedErrorResponse(response)
//                            }
//                        }, { e ->
//                            shouldHideProgressOverlay(true)
//                            handleThrowable(e)
//                        })
//        )
//    }
//
//    private fun getHistoricalLoads(currentCard: DebitCardInfo) {
//        val cardRequest = CardRequest(EngageService.getInstance().authManager.authToken, currentCard.debitCardId)
//        compositeDisposable.add(
//                EngageService.getInstance().engageApiInterface.postListHistoricalLoads(cardRequest.fieldMap)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({ response ->
//                            shouldHideProgressOverlay(true)
//                            if (response.isSuccess && response is AchLoadsResponse) {
//                                val loads = response.loads
//                                // sort by date
//                                loads.sortWith(Comparator { one, two ->
//                                    val date1 = BackendDateTimeUtils.parseDateTimeFromIso8601String(one.isoLoadDate)
//                                    val date2 = BackendDateTimeUtils.parseDateTimeFromIso8601String(two.isoLoadDate)
//                                    date2!!.compareTo(date1)
//                                })
//                                val historicalLoadList = loads.subList(0, Math.min(loads.size, HISTORICAL_LOADS_LIST_MAX_COUNT))
//                                achHistoricalLoadListObservable.value = historicalLoadList
//                            } else {
//                                handleUnexpectedErrorResponse(response)
//                            }
//                        }, { e ->
//                            shouldHideProgressOverlay(true)
//                            handleThrowable(e)
//                        })
//        )
//    }

//    private fun initBankAccountStatusAndList(achAccountInfoList: List<AchAccountInfo>) {
//        if (achAccountInfoList.isNotEmpty()) {
//
//            var verified = false
//            for (achAccountInfo in achAccountInfoList) {
//
//                if (achAccountInfo.achAccountStatus == AchAccountStatus.VERIFIED) {
//
//                    achAccountsListAndStatusObservable.value = AchBankAccountListAndStatus(
//                            bankStatus = BankAccountStatus.VERIFIED_BANK_ACCOUNT,
//                            achAccountInfoList = achAccountInfoList)
//                    verified = true
//                    break
//                }
//                achBankAccountId = achAccountInfo.achAccountId
//            }
//            if (!verified) {
//                achAccountsListAndStatusObservable.value = AchBankAccountListAndStatus(
//                        bankStatus = BankAccountStatus.UNVERIFIED_BANK_ACCOUNT,
//                        achAccountInfoList = achAccountInfoList)
//            }
//        } else {
//            achAccountsListAndStatusObservable.value = AchBankAccountListAndStatus(
//                    bankStatus = BankAccountStatus.NO_BANK_ACCOUNT,
//                    achAccountInfoList = achAccountInfoList)
//        }
//    }

//    private fun shouldHideProgressOverlay(apiCallDone: Boolean) {
//        if (shouldHideProgressOverlay && apiCallDone) {
//            progressOverlayShownObservable.value = false
//        }
//        shouldHideProgressOverlay = true
//    }

//    fun isAllowedToAddAccount(): Boolean {
//        accountInfo?.let { currentAccountInfo ->
//            return currentAccountInfo.accountPermissionsInfo.isAllowAddAchAccount
//        }
//        return false
//    }

//    data class AchBankAccountListAndStatus(val bankStatus: BankAccountStatus, val achAccountInfoList: List<AchAccountInfo>)
}