package com.engageft.feature

import android.os.Handler
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ChangeSecurityQuestionsViewModel
 * <p>
 * ViewModel for change security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ProfileViewModel : BaseEngageViewModel() {
    enum class ProfileNavigation {
        NONE,
        CHANGE_SUCCESSFUL,
        CREATE_SUCCESSFUL
    }

    enum class SaveButtonState {
        GONE,
        VISIBLE_DISABLED,
        VISIBLE_ENABLED
    }

    enum class InputValidationError {
        NONE,
        EMPTY
    }

    enum class PhoneInputValidationError {
        NONE,
        EMPTY,
        NOT_TEN
    }

    enum class ZipInputValidationError {
        NONE,
        EMPTY,
        NOT_FIVE
    }

    private val compositeDisposable = CompositeDisposable()
    val navigationObservable = MutableLiveData<ProfileNavigation>()
    val saveButtonStateObservable = MutableLiveData<SaveButtonState>()
    val emailValidationObservable = MutableLiveData<InputValidationError>()
    val phoneValidationObservable = MutableLiveData<PhoneInputValidationError>()
    val streetValidationObservable = MutableLiveData<InputValidationError>()
    val cityValidationObservable = MutableLiveData<InputValidationError>()
    val stateValidationObservable = MutableLiveData<InputValidationError>()
    val zipValidationObservable = MutableLiveData<ZipInputValidationError>()

    val legalName : ObservableField<String> = ObservableField("")
    val emailAddress : ObservableField<String> = ObservableField("")
    val phoneNumber : ObservableField<String> = ObservableField("")
    val streetAddress : ObservableField<String> = ObservableField("")
    val aptSuite : ObservableField<String> = ObservableField("")
    val city : ObservableField<String> = ObservableField("")
    val state : ObservableField<String> = ObservableField("")
    val zip : ObservableField<String> = ObservableField("")

    val emailInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateEmail(true)
            setFormValuesChanged()
        }
    }
    val phoneInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validatePhone(true)
            setFormValuesChanged()
        }
    }
    val streetInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateStreet(true)
            setFormValuesChanged()
        }
    }
    val aptSuiteInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            setFormValuesChanged()
        }
    }
    val cityInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateCity(true)
            setFormValuesChanged()
        }
    }
    val stateInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateState(true)
            setFormValuesChanged()
        }
    }
    val zipInputWatcher = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateZipCode(true)
            setFormValuesChanged()
        }
    }

    private lateinit var loginResponse: LoginResponse
    private var valuesChanged = false

    init {
        progressOverlayShownObservable.value = true
        navigationObservable.value = ProfileNavigation.NONE
        saveButtonStateObservable.value = SaveButtonState.GONE
        emailValidationObservable.value = InputValidationError.NONE
        phoneValidationObservable.value = PhoneInputValidationError.NONE
        streetValidationObservable.value = InputValidationError.NONE
        cityValidationObservable.value = InputValidationError.NONE
        stateValidationObservable.value = InputValidationError.NONE
        zipValidationObservable.value = ZipInputValidationError.NONE
        loadProfileState()
    }

    fun onSaveClicked() {
        // TODO
    }

    private fun loadProfileState() {
        compositeDisposable.add(
                EngageService.getInstance().loginResponseAsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess && response is LoginResponse) {
                                loginResponse = response
                                updateFieldsWithBackendData()
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }

                        }) { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        }
        )
    }

    private fun updateFieldsWithBackendData() {
        // First remove our property watchers so we don't trigger self update change flags
        emailAddress.removeOnPropertyChangedCallback(emailInputWatcher)
        phoneNumber.removeOnPropertyChangedCallback(phoneInputWatcher)
        streetAddress.removeOnPropertyChangedCallback(streetInputWatcher)
        aptSuite.removeOnPropertyChangedCallback(aptSuiteInputWatcher)
        city.removeOnPropertyChangedCallback(cityInputWatcher)
        state.removeOnPropertyChangedCallback(stateInputWatcher)
        zip.removeOnPropertyChangedCallback(zipInputWatcher)

        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
        val addressInfo = LoginResponseUtils.getAddressInfoForCurrentDebitCard(loginResponse)
        legalName.set(accountInfo.firstName + " " + accountInfo.lastName)
        emailAddress.set(accountInfo.email)
        phoneNumber.set(accountInfo.phone)
        streetAddress.set(addressInfo.address1)
        aptSuite.set(addressInfo.address2)
        city.set(addressInfo.city)
        state.set(addressInfo.state.abbreviation)
        zip.set(addressInfo.zip)

        // Re-add the watchers, but do it as a "post" because the above "set" calls are asychronous
        // and won't occur until returning to the looper. After the values are set, our runnable
        // below is run to add the listeners.
        Handler().post {
            emailAddress.addOnPropertyChangedCallback(emailInputWatcher)
            phoneNumber.addOnPropertyChangedCallback(phoneInputWatcher)
            streetAddress.addOnPropertyChangedCallback(streetInputWatcher)
            aptSuite.addOnPropertyChangedCallback(aptSuiteInputWatcher)
            city.addOnPropertyChangedCallback(cityInputWatcher)
            state.addOnPropertyChangedCallback(stateInputWatcher)
            zip.addOnPropertyChangedCallback(zipInputWatcher)
        }
    }

    private fun setFormValuesChanged() {
        if (!valuesChanged) {
//            val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
//            val addressInfo = LoginResponseUtils.getAddressInfoForCurrentDebitCard(loginResponse)
//            val legalName = legalName.get()!!
//            val email = emailAddress.get()!!
//            val phone = phoneNumber.get()!!
//            Log.e("Joey", "phone " + phone)
//            val streetAddress = streetAddress.get()!!
//            val aptSuite = aptSuite.get()!!
//            val city = city.get()!!
//            val state = state.get()!!
//            val zip = zip.get()!!
//            if ((legalName != accountInfo.firstName + " " + accountInfo.lastName) || email != accountInfo.email ||
//                    phone != accountInfo.phone || street)
            valuesChanged = true
            validateSaveButtonState()
        }
    }

    private fun checkAllFieldsValid(): Boolean {
        val emailValid = emailValidationObservable.value == InputValidationError.NONE
        val phoneValid = phoneValidationObservable.value == PhoneInputValidationError.NONE
        val streetAddressValid = streetValidationObservable.value == InputValidationError.NONE
        val cityValid = cityValidationObservable.value == InputValidationError.NONE
        val stateValid = stateValidationObservable.value == InputValidationError.NONE
        val zipValid = zipValidationObservable.value == ZipInputValidationError.NONE

        return emailValid && phoneValid && streetAddressValid && cityValid && stateValid && zipValid
    }

    private fun validateSaveButtonState() {
        Log.e("Joey", "validateSaveButtonState")
        saveButtonStateObservable.value = if (!valuesChanged) {
            SaveButtonState.GONE
        } else {
            if (checkAllFieldsValid()) {
                SaveButtonState.VISIBLE_ENABLED
            } else {
                SaveButtonState.VISIBLE_DISABLED
            }
        }
    }

    fun validateEmail(conditionallyIfError: Boolean) {
        val currentState = emailValidationObservable.value
        if (conditionallyIfError && currentState != InputValidationError.NONE || !conditionallyIfError) {
            val newState = if (emailAddress.get()!!.isNotBlank()) {
                InputValidationError.NONE
            } else {
                InputValidationError.EMPTY
            }

            if (currentState != newState) {
                emailValidationObservable.value = newState
                validateSaveButtonState()
            }
        }
    }

    fun validatePhone(conditionallyIfError: Boolean) {
        val currentState = phoneValidationObservable.value
        if (conditionallyIfError && currentState != PhoneInputValidationError.NONE || !conditionallyIfError) {
            val pN = phoneNumber.get()!!
            val newState= if (pN.isBlank()) {
                PhoneInputValidationError.EMPTY
            } else if (pN.length != 10) {
                PhoneInputValidationError.NOT_TEN
            } else {
                PhoneInputValidationError.NONE
            }

            if (currentState != newState) {
                phoneValidationObservable.value = newState
                validateSaveButtonState()
            }
        }
    }

    fun validateStreet(conditionallyIfError: Boolean) {
        val currentState = streetValidationObservable.value
        if (conditionallyIfError && currentState != InputValidationError.NONE || !conditionallyIfError) {
            val newState = if (streetAddress.get()!!.isNotBlank()) {
                InputValidationError.NONE
            } else {
                InputValidationError.EMPTY
            }

            if (currentState != newState) {
                streetValidationObservable.value = newState
                validateSaveButtonState()
            }
        }
    }

    fun validateCity(conditionallyIfError: Boolean) {
        val currentState = cityValidationObservable.value
        if (conditionallyIfError && currentState != InputValidationError.NONE || !conditionallyIfError) {
            val newState = if (city.get()!!.isNotBlank()) {
                InputValidationError.NONE
            } else {
                InputValidationError.EMPTY
            }

            if (currentState != newState) {
                cityValidationObservable.value = newState
                validateSaveButtonState()
            }
        }
    }

    fun validateState(conditionallyIfError: Boolean) {
        val currentState = stateValidationObservable.value
        if (conditionallyIfError && currentState != InputValidationError.NONE || !conditionallyIfError) {
            val newState = if (state.get()!!.isNotBlank()) {
                InputValidationError.NONE
            } else {
                InputValidationError.EMPTY
            }

            if (currentState != newState) {
                stateValidationObservable.value = newState
                validateSaveButtonState()
            }
        }
    }

    fun validateZipCode(conditionallyIfError: Boolean) {
        val currentState = zipValidationObservable.value
        if (conditionallyIfError && currentState != ZipInputValidationError.NONE || !conditionallyIfError) {
            val zC = zip.get()!!
            val newState = if (zC.isBlank()) {
                ZipInputValidationError.EMPTY
            } else if (zC.length != 5) {
                ZipInputValidationError.NOT_FIVE
            } else {
                ZipInputValidationError.NONE
            }

            if (currentState != newState) {
                zipValidationObservable.value = newState
                validateSaveButtonState()
            }
        }
    }
}