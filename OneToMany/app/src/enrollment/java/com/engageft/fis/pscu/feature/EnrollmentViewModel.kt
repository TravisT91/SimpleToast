package com.engageft.fis.pscu.feature

import androidx.navigation.NavController
import com.engageft.apptoolbox.view.ProductCardModel
import com.engageft.apptoolbox.view.ProductCardModelCardStatus
import com.engageft.fis.pscu.feature.gatekeeping.CardPinEnrollmentGateKeeper
import com.engageft.fis.pscu.feature.gatekeeping.GateKeeperListener
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.AccountRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.CIPRequiredGatedItem
import com.engageft.fis.pscu.feature.gatekeeping.items.TermsRequiredGatedItem
import com.ob.domain.lookup.DebitCardStatus
import com.ob.ws.dom.ActivationCardInfo

/**
 * EnrollmentViewModel
 * <p>
 * Activity-level viewmodel managing all the Enrollment flow. Each fragment is broken up into lazy-
 * initialized delegates. It is determined on the first fragment which delegates will be needed,
 * and by the end of the flow, only the 'isInitialized' delegates will be needed to submit a final
 * form.
 *
 * This viewModel also has all possible navigations passed in by the Enrollment activity at create time.
 * This allows the delegates to directly call their navigations.
 *
 * Possible considerations: The Delegates don't have to be inner classes here, and can instead be moved
 * to their own files. This class will likely become very large if not. If that is done, the navController
 * and respecting NavigationDirections objects need to be passed to the delegates at init time so they
 * can do their jobs.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentViewModel : BaseEngageViewModel() {
    val getStartedDelegate by lazy {
        GetStartedDelegate(this, navController, getStartedNavigations)
    }

    // These providers are here to later check isInitialized to determine if the delegates are null or not.
    val cardPinDelegateProvider = lazy {EnrollmentCardPinDelegate()}
    val createAccountDelegateProvider = lazy {CreateAccountDelegate()}
    val verifyIdentityDelegateProvider = lazy {VerifyIdentityDelegate()}
    val termsOfUseDelegateProvider = lazy {TermsOfUseDelegate()}

    val cardPinDelegate by cardPinDelegateProvider
    val createAccountDelegate by createAccountDelegateProvider
    val verifyIdentityDelegate by verifyIdentityDelegateProvider
    val termsOfUseDelegate by termsOfUseDelegateProvider

    private lateinit var navController: NavController
    private lateinit var getStartedNavigations: EnrollmentNavigations.GetStartedNavigations
    private lateinit var cardPinNavigations: EnrollmentNavigations.EnrollmentCardPinNavigations
    private lateinit var createAccountNavigations: EnrollmentNavigations.CreateAccountNavigations
    private lateinit var verifyIdentityNavigations: EnrollmentNavigations.VerifyIdentityNavigations
    private lateinit var termsNavigations: EnrollmentNavigations.TermsNavigations

    lateinit var activationCardInfo: ActivationCardInfo

    fun initEnrollmentNavigation(navController: NavController, getStartedNavigations: EnrollmentNavigations.GetStartedNavigations,
                                 cardPinNavigations: EnrollmentNavigations.EnrollmentCardPinNavigations, createAccountNavigations: EnrollmentNavigations.CreateAccountNavigations,
                                 verifyIdentityNavigations: EnrollmentNavigations.VerifyIdentityNavigations, termsNavigations: EnrollmentNavigations.TermsNavigations) {
        this.navController = navController
        this.getStartedNavigations = getStartedNavigations
        this.cardPinNavigations = cardPinNavigations
        this.createAccountNavigations = createAccountNavigations
        this.verifyIdentityNavigations = verifyIdentityNavigations
        this.termsNavigations = termsNavigations
    }

    private fun finalSubmit() {
        // Check which delegates were instantiated:
        if (cardPinDelegateProvider.isInitialized()) {
            // cardPinDelegate is filled out
        }
        if (createAccountDelegateProvider.isInitialized()) {
            // createAccountDelegate is filled out
        }
        if (verifyIdentityDelegateProvider.isInitialized()) {
            // verifyIdentityDelegate is filled out
        }
        if (termsOfUseDelegateProvider.isInitialized()) {
            // termsOfUseDelegate is filled out
        }

        // TODO(jhutchins): Implement this.
    }

    inner class EnrollmentCardPinDelegate : CardPinViewModelListener {
        val cardPinViewModelDelegate = CardPinViewModelDelegate(this@EnrollmentViewModel, this)
        private val gateKeeperListener: GateKeeperListener = object : GateKeeperListener {
            override fun onGateOpen() {
                navController.navigate(cardPinNavigations.cardPINToSending)
            }

            override fun onGatedItemFailed(item: GatedItem) {
                when (item) {
                    is AccountRequiredGatedItem -> {
                        navController.navigate(cardPinNavigations.cardPINToCreateAccount)
                    }
                    is CIPRequiredGatedItem -> {
                        navController.navigate(cardPinNavigations.cardPINToVerifyIdentity)
                    }
                    is TermsRequiredGatedItem -> {
                        navController.navigate(cardPinNavigations.cardPINToTerms)
                    }
                }
            }

            override fun onItemError(item: GatedItem, e: Throwable?, message: String?) {
                // Intentionally empty and will never be called.
            }
        }

        var pinNumber = 0

        init {
            cardPinViewModelDelegate.productCardViewModelDelegate.cardStateObservable.value = ProductCardViewCardState.DETAILS_HIDDEN
            val productCardModel = ProductCardModel()
            productCardModel.cardholderName = String.format("%s %s", activationCardInfo.firstName, activationCardInfo.lastName)
            productCardModel.cardStatusText = activationCardInfo.cardStatus
            productCardModel.cardStatus = productCardModelStatusFromActivationInfo(activationCardInfo)
            productCardModel.cardStatusOkay = true
            productCardModel.cardLocked = productCardModel.cardStatus == ProductCardModelCardStatus.CARD_STATUS_LOCKED
            productCardModel.cardNumberPartial = getStartedDelegate.cardNumber.substring(11, 15)

            cardPinViewModelDelegate.productCardViewModelDelegate.cardInfoModelObservable.value = productCardModel
        }

        private fun productCardModelStatusFromActivationInfo(activationCardInfo: ActivationCardInfo): ProductCardModelCardStatus {
            return when (activationCardInfo.cardStatus) {
                DebitCardStatus.ACTIVE.toString() -> {
                    ProductCardModelCardStatus.CARD_STATUS_ACTIVE
                }
//                DebitCardStatus.PENDING_CREATE.toString() -> {
//                    ProductCardModelCardStatus.CARD_STATUS_PENDING
//                }
                DebitCardStatus.PENDING_ACTIVATION.toString() -> {
                    ProductCardModelCardStatus.CARD_STATUS_PENDING
                }
//                DebitCardStatus.REPLACEMENT_ORDERED.toString() -> {
//                    ProductCardModelCardStatus.CARD_STATUS_REPLACED
//                }
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

        override fun onPostPin(pinNumber: Int) {
            this.pinNumber = pinNumber

            val gateKeeper = CardPinEnrollmentGateKeeper(activationCardInfo, gateKeeperListener)
            gateKeeper.run()
        }

        fun onButton1Clicked() {
            navController.navigate(cardPinNavigations.cardPINToCreateAccount)
        }

        fun onButton2Clicked() {
            navController.navigate(cardPinNavigations.cardPINToVerifyIdentity)
        }

        fun onButton3Clicked() {
            navController.navigate(cardPinNavigations.cardPINToTerms)
        }

        fun onButton4Clicked() {
            navController.navigate(cardPinNavigations.cardPINToSending)
        }
    }

    inner class CreateAccountDelegate {
        init {

        }

        fun onButton1Clicked() {
            navController.navigate(createAccountNavigations.createAccountToVerifyIdentity)
        }

        fun onButton2Clicked() {
            navController.navigate(createAccountNavigations.createAccountToTerms)
        }

        fun onButton3Clicked() {
            navController.navigate(createAccountNavigations.createAccountToSending)
        }
    }

    inner class VerifyIdentityDelegate {
        init {

        }

        fun onButton1Clicked() {
            navController.navigate(verifyIdentityNavigations.verifyIdentityToTerms)
        }

        fun onButton2Clicked() {
            navController.navigate(verifyIdentityNavigations.verifyIdentityToSending)
        }
    }

    inner class TermsOfUseDelegate {
        init {

        }

        fun onButton1Clicked() {
            navController.navigate(termsNavigations.termsToSending)
        }
    }

    sealed class EnrollmentNavigations {
        class GetStartedNavigations(val getStartedToPin: Int, val getStartedToCreateAccount: Int, val getStartedToVerifyIdentity: Int, val getStartedToTerms: Int, val getStartedToSending: Int)
        class EnrollmentCardPinNavigations(val cardPINToCreateAccount: Int, val cardPINToVerifyIdentity: Int, val cardPINToTerms: Int, val cardPINToSending: Int)
        class CreateAccountNavigations(val createAccountToVerifyIdentity: Int, val createAccountToTerms: Int, val createAccountToSending: Int)
        class VerifyIdentityNavigations(val verifyIdentityToTerms: Int, val verifyIdentityToSending: Int)
        class TermsNavigations(val termsToSending: Int)
    }
}
