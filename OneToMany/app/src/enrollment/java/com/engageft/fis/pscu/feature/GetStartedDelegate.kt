package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.ActivationCardInfoRequest
import com.engageft.fis.pscu.feature.branding.BrandingManager
import com.ob.ws.dom.ActivationCardInfo
import com.ob.ws.dom.BrandingInfoResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils

/**
 * GetStartedDelegate
 * <p>
 * ViewModel delegate for the Enrollment View Model.
 * </p>
 * Created by joeyhutchins on 12/18/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class GetStartedDelegate(private val viewModel: EnrollmentViewModel, private val navController: NavController, private val getStartedNavigations: EnrollmentViewModel.EnrollmentNavigations.GetStartedNavigations) {
    val cardInput: ObservableField<String> = ObservableField("")
    val dateOfBirth: ObservableField<String> = ObservableField("")

    enum class NextButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    enum class CardInputValidationError {
        NONE,
        EMPTY,
        NOT_SIXTEEN
    }

    enum class DOBInputValidationError {
        NONE,
        EMPTY,
        INVALID,
        UNDER_13
    }
    private val cardInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateCardNumber(true)
            validateNextButtonState()
        }
    }
    private val dateOfBirthInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateDOB(true)
            validateNextButtonState()
        }
    }
    val nextButtonStateObservable = MutableLiveData<NextButtonState>()
    val cardNumberValidationObservable = MutableLiveData<CardInputValidationError>()
    val dateOfBirthValidationObservable = MutableLiveData<DOBInputValidationError>()

    init {
        nextButtonStateObservable.value = NextButtonState.GONE
        cardNumberValidationObservable.value = CardInputValidationError.NONE
        dateOfBirthValidationObservable.value = DOBInputValidationError.NONE
        cardInput.addOnPropertyChangedCallback(cardInputTextWatcher)
        dateOfBirth.addOnPropertyChangedCallback(dateOfBirthInputTextWatcher)
    }

    fun onButton1Clicked() {
        navController.navigate(getStartedNavigations.getStartedToPin)
    }

    fun onButton2Clicked() {
        navController.navigate(getStartedNavigations.getStartedToCreateAccount)
    }

    fun onButton3Clicked() {
        navController.navigate(getStartedNavigations.getStartedToVerifyIdentity)
    }

    fun onButton4Clicked() {
        navController.navigate(getStartedNavigations.getStartedToTerms)
    }

    fun onButton5Clicked() {
        navController.navigate(getStartedNavigations.getStartedToSending)
    }

    fun onNextClicked() {
        validateCardNumber(false)
        validateDOB(false)
        if (checkAllFieldsValid()) {
            val dob = try {
                getDateForInput()
            } catch (e: Exception) {
                viewModel.handleThrowable(e)
                null
            }
            dob?.let { dobDateTime ->
                viewModel.progressOverlayShownObservable.value = true
                val request = ActivationCardInfoRequest(
                        cardInput.get()!!,
                        dobDateTime)
                viewModel.compositeDisposable.add(
                        EngageService.getInstance().engageApiInterface.postActivationCardInfo(request.fieldMap)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ response ->
                                    viewModel.progressOverlayShownObservable.value = false
                                    if (response.isSuccess && response is ActivationCardInfo) {
                                        val cardInfo = response
                                        viewModel.activationCardInfo = cardInfo

                                        BrandingManager.getBrandingWithRefCode(cardInfo.refCode)
                                                .subscribeWithDefaultProgressAndErrorHandling<BrandingInfoResponse>(
                                                        viewModel, {
                                                    // TODO(jhutchins): This should be a gateKeeper...
                                                    if (cardInfo.isParentActivationRequired) {

                                                    } else if (cardInfo.isPinRequired) {

                                                    } else if (cardInfo.isAccountRequired) {

                                                    } else if (cardInfo.isCipRequired) {

                                                    } else if (cardInfo.isTermsRequired) {

                                                    }
                                                }/*, { failedResponse ->
                                                    // TODO(jhutchins): Navigate?
                                                }*/
                                        )
                                    } else {
                                        // TODO(jhutchins): Invalid stuff
                                    }
                                }, { e ->
                                    viewModel.progressOverlayShownObservable.value = false
                                    viewModel.handleThrowable(e)
                                })
                )
            }
        }
    }

    fun validateCardNumber(conditionallyIfError: Boolean) {
        val currentState = cardNumberValidationObservable.value
        if (conditionallyIfError && currentState != CardInputValidationError.NONE || !conditionallyIfError) {
            val newState = if (cardInput.get()!!.isEmpty()) {
                CardInputValidationError.EMPTY
            } else if (cardInput.get()!!.length != 16 ) {
                CardInputValidationError.NOT_SIXTEEN
            } else {
                CardInputValidationError.NONE
            }

            if (currentState != newState) {
                cardNumberValidationObservable.value = newState
                validateNextButtonState()
            }
        }
    }

    fun validateDOB(conditionallyIfError: Boolean) {
        val currentState = dateOfBirthValidationObservable.value
        if (conditionallyIfError && currentState != DOBInputValidationError.NONE || !conditionallyIfError) {
            val pN = dateOfBirth.get()!!
            val newState = if (pN.isEmpty()) {
                DOBInputValidationError.EMPTY
            } else {
                if (pN.length == 10) {
                    try {
                        val dateTime = getDateForInput()
                        val dateTime13YearsAgo = DateTime.now().minusYears(13)
                        if (dateTime.isAfter(dateTime13YearsAgo)) {
                            DOBInputValidationError.UNDER_13
                        } else {
                            DOBInputValidationError.NONE
                        }
                    } catch (e: Exception) {
                        DOBInputValidationError.INVALID
                    }
                } else {
                    DOBInputValidationError.INVALID
                }
            }

            if (currentState != newState) {
                dateOfBirthValidationObservable.value = newState
                validateNextButtonState()
            }
        }
    }

    private fun getDateForInput(): DateTime {
        return DisplayDateTimeUtils.shortDateFormatter.parseDateTime(dateOfBirth.get()!!)
    }

    private fun validateNextButtonState() {
        val enabled = cardNumberValidationObservable.value == CardInputValidationError.NONE &&
                dateOfBirthValidationObservable.value == DOBInputValidationError.NONE &&
                cardInput.get()!!.isNotEmpty() && dateOfBirth.get()!!.isNotEmpty()
        nextButtonStateObservable.value = if (!enabled) {
            NextButtonState.GONE
        } else {
            NextButtonState.VISIBLE_ENABLED
        }
    }

    private fun checkAllFieldsValid(): Boolean {
        val cardValid = cardNumberValidationObservable.value == CardInputValidationError.NONE
        val dobValid = dateOfBirthValidationObservable.value == DOBInputValidationError.NONE

        return cardValid && dobValid
    }
}