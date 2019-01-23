package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.HeapUtils
import com.engageft.fis.pscu.MoEngageUtils
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.authentication.AuthenticationSharedPreferencesRepo
import com.ob.domain.lookup.DebitCardStatus
import com.ob.domain.lookup.branding.BrandingCard
import com.ob.ws.dom.ActivationCardInfo
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by joeyhutchins on 1/21/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CardLinkedDelegate(private val viewModel: EnrollmentViewModel, private val navController: NavController,
                         private val linkedNavigations: EnrollmentViewModel.EnrollmentNavigations.LinkedNavigations) {

    val brandingCardObservable = MutableLiveData<BrandingCard>()
    val productCardViewModelDelegate = ProductCardViewDelegate(viewModel)

    init {
        productCardViewModelDelegate.cardStateObservable.value = ProductCardViewCardState.DETAILS_HIDDEN
        val productCardModel = ProductCardModel()
        productCardModel.cardholderName = String.format("%s %s", viewModel.activationCardInfo.firstName, viewModel.activationCardInfo.lastName)
        productCardModel.cardStatusText = viewModel.activationCardInfo.cardStatus
        productCardModel.cardStatus = productCardModelStatusFromActivationInfo(viewModel.activationCardInfo)
        productCardModel.cardStatusOkay = true
        productCardModel.cardLocked = productCardModel.cardStatus == ProductCardModelCardStatus.CARD_STATUS_LOCKED
        productCardModel.cardNumberPartial = getLastFourFromCreditCard(viewModel.getStartedDelegate.cardNumber)

        productCardViewModelDelegate.cardInfoModelObservable.value = productCardModel
    }

    fun onDoneClicked() {
        if (viewModel.activationCardInfo.isParentActivationRequired) {
            navController.navigate(linkedNavigations.linkedToLogin)
        } else {
            EngageService.getInstance().authManager.authTokenInternal = viewModel.activationResponse.token
            viewModel.compositeDisposable.add(
                    EngageService.getInstance().refreshLoginObservable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                if (response.isSuccess && response is LoginResponse) {
                                    handleSuccessfulLoginResponse(response)
                                    navController.navigate(linkedNavigations.linkedToDashboard)
                                } else {
                                    viewModel.handleUnexpectedErrorResponse(response)
                                }
                            }) { e ->
                                viewModel.handleThrowable(e)
                            }
            )
        }
    }

    /**
     * I am attempting to resolve differences in DebitCardStatus enums to ProductCardModelCardStatus
     * enums. Some don't exist in the other.
     */
    private fun productCardModelStatusFromActivationInfo(activationCardInfo: ActivationCardInfo): ProductCardModelCardStatus {
        return when (activationCardInfo.cardStatus) {
            DebitCardStatus.ACTIVE.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_ACTIVE
            }
            DebitCardStatus.PENDING_ACTIVATION.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_PENDING
            }
            DebitCardStatus.CANCELED.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_CANCELED
            }
            DebitCardStatus.REPLACED.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_REPLACED
            }
            DebitCardStatus.LOCKED_USER.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            DebitCardStatus.LOCKED_PARENT.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            DebitCardStatus.LOCKED_CSR.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            DebitCardStatus.LOCKED_ADMIN.toString() -> {
                ProductCardModelCardStatus.CARD_STATUS_LOCKED
            }
            else -> {
                // This is bad...
                throw IllegalStateException("Unknown card status. ")
            }
        }
    }

    private fun getLastFourFromCreditCard(cardNumber: String): String {
        if (cardNumber.length != EnrollmentCardPinDelegate.STRING_LENGTH_CREDIT_CARD) {
            throw IllegalArgumentException("Invalid credit card number string with length ${cardNumber.length}. Expected ${EnrollmentCardPinDelegate.STRING_LENGTH_CREDIT_CARD}")
        }
        return cardNumber.substring(EnrollmentCardPinDelegate.INDEX_LAST_FOUR_DIGITS_START, EnrollmentCardPinDelegate.INDEX_LAST_FOUR_DIGITS_END)
    }

    private fun handleSuccessfulLoginResponse(loginResponse: LoginResponse) {
        // set the Get started flag to true after the successful login, so the Welcome screen doesn't get displayed again
        WelcomeSharedPreferencesRepo.applyHasSeenGetStarted(true)

        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        if (accountInfo != null && accountInfo.accountId != 0L) {
            // Setup unique user identifier for Heap analytics
            HeapUtils.identifyUser(accountInfo.accountId.toString())
            // Setup user attributes for MoEngage
            MoEngageUtils.setUserAttributes(accountInfo)
        }

        val usernameToSave = LoginResponseUtils.getCurrentAccountInfo(loginResponse).email
        val usingTestMode = !EngageAppConfig.isUsingProdEnvironment
        if (usingTestMode) {
            AuthenticationSharedPreferencesRepo.applyDemoSavedUsername(usernameToSave)
        } else {
            AuthenticationSharedPreferencesRepo.applySavedUsername(usernameToSave)
        }
    }
}