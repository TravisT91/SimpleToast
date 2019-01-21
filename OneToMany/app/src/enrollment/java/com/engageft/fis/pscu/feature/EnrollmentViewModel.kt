package com.engageft.fis.pscu.feature

import android.os.Handler
import androidx.navigation.NavController
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.ActivationRequest
import com.ob.domain.lookup.DebitCardStatus
import com.ob.ws.dom.ActivationCardInfo
import com.ob.ws.dom.ActivationResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentViewModel : BaseEngageViewModel() {
    private companion object {
        const val TAG = "EnrollmentViewModel"
        const val NAV_DELAY_TIME_MS = 1000L
    }

    // This ViewModel delegate will always be instantiated. It is part of the logic for the first fragment seen.
    val getStartedDelegate by lazy {
        GetStartedDelegate(this, navController, getStartedNavigations)
    }

    // These providers are here to later check isInitialized to determine if the delegates are null or not.
    private val cardPinDelegateProvider = lazy { EnrollmentCardPinDelegate(this, navController, cardPinNavigations) }
    private val createAccountDelegateProvider = lazy { CreateAccountDelegate(this, navController, createAccountNavigations) }
    private val verifyIdentityDelegateProvider = lazy { VerifyIdentityDelegate(this, navController, verifyIdentityNavigations) }
    private val termsOfUseDelegateProvider = lazy { TermsOfUseDelegate() }

    // These delegates may or may not be instantiated. It depends on what the gateKeepers determine
    // is necessary. The GateKeepers read values from the activationCardInfo to determine which are needed.
    val cardPinDelegate by cardPinDelegateProvider
    val createAccountDelegate by createAccountDelegateProvider
    val verifyIdentityDelegate by verifyIdentityDelegateProvider
    val termsOfUseDelegate by termsOfUseDelegateProvider
    val cardActiveDelegate by lazy {
        CardActiveDelegate(this, navController, activeNavigations)
    }
    val cardLinkedDelegate by lazy {
        CardLinkedDelegate(this, navController, linkedNavigations)
    }

    private lateinit var navController: NavController
    private lateinit var getStartedNavigations: EnrollmentNavigations.GetStartedNavigations
    private lateinit var cardPinNavigations: EnrollmentNavigations.EnrollmentCardPinNavigations
    private lateinit var createAccountNavigations: EnrollmentNavigations.CreateAccountNavigations
    private lateinit var verifyIdentityNavigations: EnrollmentNavigations.VerifyIdentityNavigations
    private lateinit var termsNavigations: EnrollmentNavigations.TermsNavigations
    private lateinit var sendingNavigations: EnrollmentNavigations.SendingNavigations
    private lateinit var linkedNavigations: EnrollmentNavigations.LinkedNavigations
    private lateinit var activeNavigations: EnrollmentNavigations.ActiveNavigations

    // Intended only to be used by ViewModel delegate objects.
    lateinit var activationCardInfo: ActivationCardInfo
    lateinit var activationResponse: ActivationResponse

    // Intended to only be used by the EnrollmentActivity during creation time.
    fun initEnrollmentNavigation(navController: NavController, getStartedNavigations: EnrollmentNavigations.GetStartedNavigations,
                                 cardPinNavigations: EnrollmentNavigations.EnrollmentCardPinNavigations, createAccountNavigations: EnrollmentNavigations.CreateAccountNavigations,
                                 verifyIdentityNavigations: EnrollmentNavigations.VerifyIdentityNavigations, termsNavigations: EnrollmentNavigations.TermsNavigations,
                                 sendingNavigations: EnrollmentNavigations.SendingNavigations, linkedNavigations: EnrollmentNavigations.LinkedNavigations,
                                 activeNavigations: EnrollmentNavigations.ActiveNavigations) {
        this.navController = navController
        this.getStartedNavigations = getStartedNavigations
        this.cardPinNavigations = cardPinNavigations
        this.createAccountNavigations = createAccountNavigations
        this.verifyIdentityNavigations = verifyIdentityNavigations
        this.termsNavigations = termsNavigations
        this.sendingNavigations = sendingNavigations
        this.linkedNavigations = linkedNavigations
        this.activeNavigations = activeNavigations
    }

    /*
    Check each delegate to determine if it was instantiated. If the delegate is instantiated, that means
    the navigation gatekeepers determined that information was necessary for the card activation
    enrollment. Therefore, we need to bundle the information into the activation request to the
    backend.
     */
    fun finalSubmit() {
        //TODO(aHashimi): when ThreatMetrix is added, pass the session-id!
        val request = ActivationRequest(
                cardNumber = getStartedDelegate.cardNumber,
                dob = getStartedDelegate.birthDate)

        // Check which delegates were instantiated:
        if (cardPinDelegateProvider.isInitialized()) {
            request.pin = cardPinDelegate.pinNumber.toString()
        }

        if (createAccountDelegateProvider.isInitialized()) {
            request.email = createAccountDelegate.userEmail
            request.password = createAccountDelegate.userPassword
        }

        if (verifyIdentityDelegateProvider.isInitialized()) {
            request.ssn = verifyIdentityDelegate.ssNumber
        }

        //TODO(aHashimi): will we need this later once terms of use is finalized?
        if (termsOfUseDelegateProvider.isInitialized()) {
            // termsOfUseDelegate is filled out
        }

        submitActivation(request)
    }

    enum class ActivationStatus {
        SUCCESS,
        FAIL
    }
    enum class CardActivationStatus {
        ACTIVE,
        LINKED
    }

    val successSubmissionObservable = SingleLiveEvent<ActivationStatus>()
    var backendError: String = ""

    private fun submitActivation(request: ActivationRequest) {
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postActivation(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess && response is ActivationResponse) {
                                activationResponse = response
                                successSubmissionObservable.value = ActivationStatus.SUCCESS

                                if (activationCardInfo.cardStatus == DebitCardStatus.PENDING_ACTIVATION.toString()) {
                                    navigateAfterDelay(sendingNavigations.sendingToActive)
                                } else {
                                    navigateAfterDelay(sendingNavigations.sendingToLinked)
                                }
                            } else {
                                successSubmissionObservable.value = ActivationStatus.FAIL
                                backendError = getBackendErrorForForms(response)
                                navController.navigate(sendingNavigations.sendingToError)
                            }
                        }) { e ->
                            successSubmissionObservable.value = ActivationStatus.FAIL
                            handleThrowable(e)
                        }
        )
    }

    private fun navigateAfterDelay(id: Int) {
        if (id != -1) {
            //let the user see the success screen for 1 second!
            Handler().postDelayed({
                navController.navigate(id)
            }, NAV_DELAY_TIME_MS)
        }
    }

    //TODO(aHashimi): Not completed, this should be its own class
    inner class TermsOfUseDelegate {
        fun onAcceptTermsClicked() {
            navController.navigate(termsNavigations.termsToSending)
        }
    }

    sealed class EnrollmentNavigations {
        class GetStartedNavigations(val getStartedToPin: Int, val getStartedToCreateAccount: Int, val getStartedToVerifyIdentity: Int, val getStartedToTerms: Int, val getStartedToSending: Int)
        class EnrollmentCardPinNavigations(val cardPINToCreateAccount: Int, val cardPINToVerifyIdentity: Int, val cardPINToTerms: Int, val cardPINToSending: Int)
        class CreateAccountNavigations(val createAccountToVerifyIdentity: Int, val createAccountToTerms: Int, val createAccountToSending: Int)
        class VerifyIdentityNavigations(val verifyIdentityToTerms: Int, val verifyIdentityToSending: Int)
        class TermsNavigations(val termsToSending: Int)
        class SendingNavigations(val sendingToError: Int, val sendingToActive: Int, val sendingToLinked: Int)
        class LinkedNavigations(val linkedToDashboard: Int, val linkedToLogin: Int)
        class ActiveNavigations(val activeToDashboard: Int, val activeToLogin: Int)
    }
}
