package com.engageft.fis.pscu.feature

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AcceptTermsRequest
import com.engageft.engagekit.rest.request.ActivationRequest
import com.ob.ws.dom.ActivationCardInfo
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
    val cardPinDelegateProvider = lazy { EnrollmentCardPinDelegate(this, navController, cardPinNavigations) }
    val createAccountDelegateProvider = lazy { CreateAccountDelegate(this, navController, createAccountNavigations) }
    val verifyIdentityDelegateProvider = lazy { VerifyIdentityDelegate(this, navController, verifyIdentityNavigations) }
    val termsOfUseDelegateProvider = lazy { TermsOfUseDelegate() }

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

    //TODO(aHashimi): Not completed, this should be its own class
    inner class TermsOfUseDelegate {
        fun onAcceptTermsClicked() {
            navController.navigate(termsNavigations.termsToSending)
        }
    }

    enum class ActivationStatus {
        SUCCESS,
        FAIL,
    }
    enum class CardActivationStatus {
        PENDING,
        LINKED
    }
    val successSubmissionObservable = MutableLiveData<ActivationStatus>()
    val cardActivationStatusObservable = MutableLiveData<CardActivationStatus>()
    fun submitAcceptTerms() {
//        progressObservable.value = PROGRESS_2
        val request = ActivationRequest(getStartedDelegate.cardNumber,
                getStartedDelegate.birthDate,
                cardPinDelegate.pinNumber.toString(),
                "",
                "",
                "")
        if (createAccountDelegate.userEmail.isNotEmpty()) {
            request.setEmail(createAccountDelegate.userEmail)
        }

        if (verifyIdentityDelegate.ssNumber.isNotEmpty()) {
            request.setSsn(verifyIdentityDelegate.ssNumber)
        }

        val startTime = System.currentTimeMillis()
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postActivation(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (!response.isSuccess) {
                                val endTime = System.currentTimeMillis()
                                Log.e("enrollmentVM", "timeDiff ${endTime - startTime}")
                                successSubmissionObservable.value = ActivationStatus.SUCCESS
                                Log.e("enrollment", "Error submitting accept terms after card creation: " + response.message)
                                if (activationCardInfo.cardStatus == "ACTIVE") {
                                    cardActivationStatusObservable.value = CardActivationStatus.PENDING
                                } else {
                                    cardActivationStatusObservable.value = CardActivationStatus.LINKED
                                }
                            } else {
                                successSubmissionObservable.value = ActivationStatus.FAIL
                            }
                        }) { e ->
                            Log.e("enrollment", "Error submitting accept terms after card creation: ", e)
                            successSubmissionObservable.value = ActivationStatus.FAIL
                            handleThrowable(e)
                        }
        )
    }

    sealed class EnrollmentNavigations {
        class GetStartedNavigations(val getStartedToPin: Int, val getStartedToCreateAccount: Int, val getStartedToVerifyIdentity: Int, val getStartedToTerms: Int, val getStartedToSending: Int)
        class EnrollmentCardPinNavigations(val cardPINToCreateAccount: Int, val cardPINToVerifyIdentity: Int, val cardPINToTerms: Int, val cardPINToSending: Int)
        class CreateAccountNavigations(val createAccountToVerifyIdentity: Int, val createAccountToTerms: Int, val createAccountToSending: Int)
        class VerifyIdentityNavigations(val verifyIdentityToTerms: Int, val verifyIdentityToSending: Int)
        class TermsNavigations(val termsToSending: Int)
    }
}
