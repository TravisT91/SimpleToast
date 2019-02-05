package com.engageft.fis.pscu.feature.secondaryusers

import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.apptoolbox.util.isDigitsOnly
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils

/**
 * Created by joeyhutchins on 2/5/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class AddSecondaryUserViewModel : BaseEngageViewModel() {
    val firstName : ObservableField<String> = ObservableField("")
    val lastName : ObservableField<String> = ObservableField("")
    val phoneNumber : ObservableField<String> = ObservableField("")
    val dob : ObservableField<String> = ObservableField("")
    val ssn = ObservableField("")

    val addButtonStateObservable = MutableLiveData<AddButtonState>()
    val firstNameValidationObservable = MutableLiveData<FirstNameValidationError>()
    val lastNameValidationObservable = MutableLiveData<LastNameValidationError>()
    val phoneNumberValidationObservable = MutableLiveData<PhoneNumberValidationError>()
    val dobValidationObservable = MutableLiveData<DOBValidationError>()
    val ssnValidationObservable = MutableLiveData<SSNValidationError>()
    val ssnVisibilityObservable = MutableLiveData<Boolean>()

    enum class AddButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    enum class FirstNameValidationError {
        NONE,
        EMPTY
    }

    enum class LastNameValidationError {
        NONE,
        EMPTY
    }

    enum class PhoneNumberValidationError {
        NONE,
        EMPTY,
        NOT_TEN
    }

    enum class DOBValidationError {
        NONE,
        EMPTY,
        INVALID,
        UNDER_13
    }

    enum class SSNValidationError {
        NONE,
        EMPTY,
        INVALID // must be 9-digits
    }

    private val firstNameInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateFirstName(true)
            validateAddButtonState()
        }
    }
    private val lastNameInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateLastName(true)
            validateAddButtonState()
        }
    }
    private val phoneNumberInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validatePhoneNumber(true)
            validateAddButtonState()
        }
    }
    private val dobInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateDOB(true)
            validateAddButtonState()
        }
    }
    private val ssnInputTextWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateSSN(true)
            validateAddButtonState()
        }
    }

    init {
        addButtonStateObservable.value = AddButtonState.GONE
        firstNameValidationObservable.value = FirstNameValidationError.NONE
        lastNameValidationObservable.value = LastNameValidationError.NONE
        phoneNumberValidationObservable.value = PhoneNumberValidationError.NONE
        dobValidationObservable.value = DOBValidationError.NONE
        ssnValidationObservable.value = SSNValidationError.NONE
        firstName.addOnPropertyChangedCallback(firstNameInputTextWatcher)
        lastName.addOnPropertyChangedCallback(lastNameInputTextWatcher)
        phoneNumber.addOnPropertyChangedCallback(phoneNumberInputTextWatcher)
        dob.addOnPropertyChangedCallback(dobInputTextWatcher)
        ssn.addOnPropertyChangedCallback(ssnInputTextWatcher)
        ssnVisibilityObservable.value = false

        loadSettings()
    }

    fun onAddClicked() {
        validateFirstName(false)
        validateLastName(false)
        validatePhoneNumber(false)
        validateDOB(false)
        if (ssnVisibilityObservable.value!!) {
            validateSSN(false)
        }
        if (checkAllFieldsValid()) {

        }
    }

    private fun loadSettings() {
        showProgressOverlayDelayed()
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is LoginResponse) {
                                val debitCardInfo = LoginResponseUtils.getCurrentCard(response)
                                ssnVisibilityObservable.value = debitCardInfo.cardPermissionsInfo.isCardSecondarySsnRequired
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }

                        }) { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        }
        )
    }

    private fun validateAddButtonState() {
        val enabled = firstNameValidationObservable.value == FirstNameValidationError.NONE &&
                lastNameValidationObservable.value == LastNameValidationError.NONE &&
                phoneNumberValidationObservable.value == PhoneNumberValidationError.NONE &&
                dobValidationObservable.value == DOBValidationError.NONE &&
                ssnValidationObservable.value == SSNValidationError.NONE &&
                firstName.get()!!.isNotEmpty() && lastName.get()!!.isNotEmpty() &&
                phoneNumber.get()!!.isNotEmpty() && dob.get()!!.isNotEmpty() &&
                (ssn.get()!!.isNotEmpty() || ssnVisibilityObservable.value == false)
        addButtonStateObservable.value = if (!enabled) {
            AddButtonState.GONE
        } else {
            AddButtonState.VISIBLE_ENABLED
        }
        Log.e("Joey", "first ${firstNameValidationObservable.value} last ${lastNameValidationObservable.value} phone ${phoneNumberValidationObservable.value} dob ${dobValidationObservable.value} " +
                "ssn ${ssnValidationObservable.value} ssnVisibility ${ssnVisibilityObservable.value}")
        Log.e("Joey", "first ${firstName.get()!!} last ${lastName.get()!!} phone ${phoneNumber.get()!!} dob ${dob.get()!!} " +
                "ssn ${ssn.get()!!}")
    }

    fun validateFirstName(conditionallyIfError: Boolean) {
        val currentState = firstNameValidationObservable.value
        if ((conditionallyIfError && currentState != FirstNameValidationError.NONE) || !conditionallyIfError) {
            val newState = if (firstName.get()!!.isEmpty()) {
                FirstNameValidationError.EMPTY
            } else {
                FirstNameValidationError.NONE
            }

            if (currentState != newState) {
                firstNameValidationObservable.value = newState
                validateAddButtonState()
            }
        }
    }

    fun validateLastName(conditionallyIfError: Boolean) {
        val currentState = lastNameValidationObservable.value
        if ((conditionallyIfError && currentState != LastNameValidationError.NONE) || !conditionallyIfError) {
            val newState = if (lastName.get()!!.isEmpty()) {
                LastNameValidationError.EMPTY
            } else {
                LastNameValidationError.NONE
            }

            if (currentState != newState) {
                lastNameValidationObservable.value = newState
                validateAddButtonState()
            }
        }
    }

    fun validatePhoneNumber(conditionallyIfError: Boolean) {
        val currentState = phoneNumberValidationObservable.value
        if (conditionallyIfError && currentState != PhoneNumberValidationError.NONE || !conditionallyIfError) {
            val pN = phoneNumber.get()!!
            val newState= if (pN.isBlank()) {
                PhoneNumberValidationError.EMPTY
            } else if (pN.length != 10) {
                PhoneNumberValidationError.NOT_TEN
            } else {
                PhoneNumberValidationError.NONE
            }

            if (currentState != newState) {
                phoneNumberValidationObservable.value = newState
                validateAddButtonState()
            }
        }
    }

    fun validateDOB(conditionallyIfError: Boolean) {
        val currentState = dobValidationObservable.value
        if ((conditionallyIfError && currentState != DOBValidationError.NONE) || !conditionallyIfError) {
            val pN = dob.get()!!
            val newState = if (pN.isEmpty()) {
                DOBValidationError.EMPTY
            } else {
                if (pN.length == 10) {
                    try {
                        val dateTime = getDateForInput()
                        val dateTime13YearsAgo = DateTime.now().minusYears(13)
                        if (dateTime.isAfter(dateTime13YearsAgo)) {
                            DOBValidationError.UNDER_13
                        } else {
                            DOBValidationError.NONE
                        }
                    } catch (e: Exception) {
                        DOBValidationError.INVALID
                    }
                } else {
                    DOBValidationError.INVALID
                }
            }

            if (currentState != newState) {
                dobValidationObservable.value = newState
                validateAddButtonState()
            }
        }
    }

    fun validateSSN(conditionallyIfError: Boolean) {
        val currentState = ssnValidationObservable.value
        if ((conditionallyIfError && currentState != SSNValidationError.NONE && currentState != SSNValidationError.EMPTY)
                || !conditionallyIfError) {
            val newState: SSNValidationError = when {
                ssn.get()!!.isEmpty() -> SSNValidationError.EMPTY
                isSsnValid() -> SSNValidationError.NONE
                else -> SSNValidationError.INVALID
            }

            if (currentState != newState) {
                ssnValidationObservable.value = newState
                validateAddButtonState()
            }
        }
    }

    private fun checkAllFieldsValid(): Boolean {
        val firstNameValid = firstNameValidationObservable.value == FirstNameValidationError.NONE
        val lastNameValid = lastNameValidationObservable.value == LastNameValidationError.NONE
        val phoneNumberValid = phoneNumberValidationObservable.value == PhoneNumberValidationError.NONE
        val dobValid = dobValidationObservable.value == DOBValidationError.NONE
        val ssnValid = ssnValidationObservable.value == SSNValidationError.NONE

        return firstNameValid && lastNameValid && phoneNumberValid && dobValid && ssnValid
    }

    private fun isSsnValid(): Boolean {
        return ssn.get()!!.isDigitsOnly() && ssn.get()!!.length == 9
    }

    private fun getDateForInput(): DateTime {
        return DisplayDateTimeUtils.shortDateFormatter.parseDateTime(dob.get()!!)
    }
}