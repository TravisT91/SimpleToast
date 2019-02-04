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
import com.engageft.fis.pscu.feature.utils.CardStatusUtils
import com.ob.domain.lookup.DebitCardStatus
import com.ob.ws.dom.ActivationCardInfo

/**
 * EnrollmentCardPinDelegate
 * <p>
 * Delegated class handling viewModel operations relating to the Card Pin feature.
 * </p>
 * Created by joeyhutchins on 1/9/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class EnrollmentCardPinDelegate(private val viewModel: EnrollmentViewModel, private val navController: NavController, private val cardPinNavigations: EnrollmentViewModel.EnrollmentNavigations.EnrollmentCardPinNavigations)
    : CardPinViewModelListener {
    companion object {
        const val STRING_LENGTH_CREDIT_CARD = 16
        const val INDEX_LAST_FOUR_DIGITS_START = 12
        const val INDEX_LAST_FOUR_DIGITS_END = 16
    }
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
        productCardModel.cardStatus = CardStatusUtils.productCardModelStatusFromActivationInfo(viewModel.activationCardInfo)
        productCardModel.cardStatusOkay = true
        productCardModel.cardLocked = productCardModel.cardStatus == ProductCardModelCardStatus.CARD_STATUS_LOCKED
        productCardModel.cardNumberPartial = getLastFourFromCreditCard(viewModel.getStartedDelegate.cardNumber)

        cardPinViewModelDelegate.productCardViewModelDelegate.cardInfoModelObservable.value = productCardModel
    }

    private fun getLastFourFromCreditCard(cardNumber: String): String {
        if (cardNumber.length != STRING_LENGTH_CREDIT_CARD) {
            throw IllegalArgumentException("Invalid credit card number string with length ${cardNumber.length}. Expected $STRING_LENGTH_CREDIT_CARD")
        }
        return cardNumber.substring(INDEX_LAST_FOUR_DIGITS_START, INDEX_LAST_FOUR_DIGITS_END)
    }

    override fun onPostPin(pinNumber: Int) {
        this.pinNumber = pinNumber

        val gateKeeper = CardPinEnrollmentGateKeeper(viewModel.activationCardInfo, gateKeeperListener)
        gateKeeper.run()
    }
}