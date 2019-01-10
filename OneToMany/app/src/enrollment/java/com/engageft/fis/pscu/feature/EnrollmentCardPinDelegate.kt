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
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 1/9/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class EnrollmentCardPinDelegate(private val viewModel: EnrollmentViewModel, private val navController: NavController, private val cardPinNavigations: EnrollmentViewModel.EnrollmentNavigations.EnrollmentCardPinNavigations)
    : CardPinViewModelListener {
    val cardPinViewModelDelegate = CardPinViewModelDelegate(viewModel, this)
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
        productCardModel.cardholderName = String.format("%s %s", viewModel.activationCardInfo.firstName, viewModel.activationCardInfo.lastName)
        productCardModel.cardStatusText = viewModel.activationCardInfo.cardStatus
        productCardModel.cardStatus = productCardModelStatusFromActivationInfo(viewModel.activationCardInfo)
        productCardModel.cardStatusOkay = true
        productCardModel.cardLocked = productCardModel.cardStatus == ProductCardModelCardStatus.CARD_STATUS_LOCKED
        productCardModel.cardNumberPartial = viewModel.getStartedDelegate.cardNumber.substring(11, 15)

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

        val gateKeeper = CardPinEnrollmentGateKeeper(viewModel.activationCardInfo, gateKeeperListener)
        gateKeeper.run()
    }
}