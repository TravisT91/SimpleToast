package com.engageft.feature

import android.os.Handler
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.domain.lookup.States
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AddressInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers

/**
 * ChangeSecurityQuestionsViewModel
 * <p>
 * ViewModel for change security questions.
 *
 * FYI: Address details will only update if the zip code is changed as well. Zip code cannot be
 * changed in isolation or an error will occur (Backend implementation). There is also a limit on
 * changing the address (3x per month), so an error will occur if that limit is reached.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ProfileViewModel : BaseEngageViewModel() {
    /**
     * Data class holding the results of a save event. If the result is null, that update did not happen
     * during the save event.
     */
    class ProfileSaveEvent(val emailResult: ProfileSaveResult?, val phoneResult: ProfileSaveResult?, val addressResult: ProfileSaveResult?)

    /**
     * Result class holding details of each API request. Message will only be null if the API call was
     * successful.
     */
    class ProfileSaveResult(val isSuccess: Boolean, val message: String?)

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

    enum class EmailInputValidationError {
        NONE,
        EMPTY,
        AT_REQUIRED
    }

    inner class UpdateResponse(val emailResponse: BasicResponse?, val phoneResponse: BasicResponse?, val addressResponse: BasicResponse?)
    inner class FinalResponse(val updateResponse: UpdateResponse, val refreshResponse: BasicResponse)

    private val compositeDisposable = CompositeDisposable()
    val saveEventObservable = MutableLiveData<ProfileSaveEvent>()
    val saveButtonStateObservable = MutableLiveData<SaveButtonState>()
    val emailValidationObservable = MutableLiveData<EmailInputValidationError>()
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

    private val eventNotCalled = BasicResponse()

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
        saveEventObservable.value = null
        saveButtonStateObservable.value = SaveButtonState.GONE
        emailValidationObservable.value = EmailInputValidationError.NONE
        phoneValidationObservable.value = PhoneInputValidationError.NONE
        streetValidationObservable.value = InputValidationError.NONE
        cityValidationObservable.value = InputValidationError.NONE
        stateValidationObservable.value = InputValidationError.NONE
        zipValidationObservable.value = ZipInputValidationError.NONE
        loadProfileState()
    }

    fun onSaveClicked() {
        // Before attempting to save the info, do one last validation attempt:
        validateCity(false)
        validateEmail(false)
        validatePhone(false)
        validateState(false)
        validateStreet(false)
        validateZipCode(false)
        if (checkAllFieldsValid()) {
            // All fields are valid, so let's find out what changed. There are potentially 3 API calls
            // that can occur during this save event. We only fire them off if the data has changed.
            val accountInfo = LoginResponseUtils.getCurrentAccountInfo(loginResponse)
            val addressInfo = LoginResponseUtils.getAddressInfoForCurrentDebitCard(loginResponse)

            // Build the email observable. This observable might be a dummy.
            val emailObservable = if (emailAddress.get()!! != accountInfo.email) {
                EngageService.getInstance().updateEmailAddressObservable(emailAddress.get()!!)
            } else {
                // No data changed, so we're not going to make an API call. Instead, just return
                // and Observable that flags us of this later.
                io.reactivex.Observable.just(eventNotCalled)
            }

            // Build the phone observable. This observable might be a dummy.
            val phoneObservable = if (phoneNumber.get() != accountInfo.phone)
                EngageService.getInstance().updatePhoneNumberObservable(phoneNumber.get()!!)
            else {
                // No data changed, so we're not going to make an API call. Instead, just return
                // and Observable that flags us of this later.
                io.reactivex.Observable.just(eventNotCalled)
            }

            // Build the address observable. This observable might be a dummy.
            val addressObservable = if (streetAddress.get()!! != addressInfo.address1 || aptSuite.get()!! != addressInfo.address2 ||
                    city.get()!! != addressInfo.city || States.getStateByAbbreviation(state.get()!!) != addressInfo.state ||
                    zip.get()!! != addressInfo.zip) {
                val newAddressInfo = AddressInfo()

                // existing fields, not editable on this form
                newAddressInfo.addressId = addressInfo.addressId
                newAddressInfo.country = addressInfo.country
                newAddressInfo.familyId = addressInfo.familyId

                // possibly changed fields
                newAddressInfo.address1 = streetAddress.get()!!
                newAddressInfo.address2 = aptSuite.get()!!
                newAddressInfo.city = city.get()!!
                newAddressInfo.state = States.getStateByAbbreviation(state.get()!!)
                newAddressInfo.zip = zip.get()!!
                EngageService.getInstance().updateCardAddressObservable(newAddressInfo)
            } else {
                // No data changed, so we're not going to make an API call. Instead, just return
                // and Observable that flags us of this later.
                io.reactivex.Observable.just(eventNotCalled)
            }


            progressOverlayShownObservable.value = true
            // Zip all 3 of these observables together, and schedule them in parallel. The Function3 will
            // be called ONLY once all three calls succeed.
            val parallelTasksObservable = io.reactivex.Observable.zip(emailObservable.subscribeOn(Schedulers.io()), phoneObservable.subscribeOn(Schedulers.io()), addressObservable.subscribeOn(Schedulers.io()),
                    Function3<BasicResponse?, BasicResponse?, BasicResponse?, UpdateResponse> { emailResponse, phoneResponse, addressResponse ->
                        // If we passed "eventNotCalled" objects into the Observable, we want to check
                        // that here. If we did, just make the BasicResponse null for this result.
                        // This will flag us later to know that this update was really never called.
                        UpdateResponse(if (emailResponse == eventNotCalled) null else emailResponse,
                                if (phoneResponse == eventNotCalled) null else phoneResponse,
                                if (addressResponse == eventNotCalled) null else addressResponse)
                    })

            val refreshLoginObservable = EngageService.getInstance().refreshLoginObservable()

            // After zipping the 3 parallel observables together, now we want to zip them together with a
            // refresh API call and put them on Schedulers.single() so that they are run Synchronously,
            // and only give us a callback when both tasks are done.
            val combinedObservable = io.reactivex.Observable.zip(parallelTasksObservable.subscribeOn(Schedulers.single()), refreshLoginObservable.subscribeOn(Schedulers.single()),
                    BiFunction<UpdateResponse, BasicResponse, FinalResponse> { updateResponse, refreshResponse ->
                        // Both tasks are done, let's put the ParallelResponse and the RefreshResponse together
                        // in a data object and pass it to our final callback.
                        FinalResponse(updateResponse, refreshResponse)
                    })


            // Now we only need to observe the final combinedOvservable, and we will only get a callback when everything is done.
            // The final result will be wrapped up in a FinalResponse data class.
            compositeDisposable.add(
                    combinedObservable
                            .subscribeOn(Schedulers.single())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ finalResponse ->
                                // Everything is done, regardless of error. Now let's see how things went:
                                progressOverlayShownObservable.value = false

                                // If the events did not happen, we want to pass the ProfileSaveResults
                                // back as null so the UI knows.
                                var emailSaveResult: ProfileSaveResult? = null
                                var phoneSaveResult: ProfileSaveResult? = null
                                var addressSaveResult: ProfileSaveResult? = null
                                finalResponse.updateResponse.emailResponse?.let {
                                    emailSaveResult = if (!it.isSuccess) {
                                        ProfileSaveResult(false, it.message)
                                    } else {
                                        ProfileSaveResult(true, null)
                                    }
                                }
                                finalResponse.updateResponse.phoneResponse?.let {
                                    phoneSaveResult = if (!it.isSuccess) {
                                        ProfileSaveResult(false, it.message)
                                    } else {
                                        ProfileSaveResult(true, null)
                                    }
                                }
                                finalResponse.updateResponse.addressResponse?.let {
                                    addressSaveResult = if (!it.isSuccess) {
                                        ProfileSaveResult(false, it.message)
                                    } else {
                                        ProfileSaveResult(true, null)
                                    }
                                }
                                // Pass the results to the UI.
                                saveEventObservable.value = ProfileSaveEvent(emailSaveResult, phoneSaveResult, addressSaveResult)

                                // Since we refreshed login, let's update our local reference to loginResponse and then update the UI.
                                this.loginResponse = finalResponse.refreshResponse as LoginResponse
                                updateFieldsWithBackendData()
                            }, { e ->
                                progressOverlayShownObservable.value = false
                                handleThrowable(e)
                            })
            )
        }
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
            valuesChanged = true
            validateSaveButtonState()
        }
    }

    private fun checkAllFieldsValid(): Boolean {
        val emailValid = emailValidationObservable.value == EmailInputValidationError.NONE
        val phoneValid = phoneValidationObservable.value == PhoneInputValidationError.NONE
        val streetAddressValid = streetValidationObservable.value == InputValidationError.NONE
        val cityValid = cityValidationObservable.value == InputValidationError.NONE
        val stateValid = stateValidationObservable.value == InputValidationError.NONE
        val zipValid = zipValidationObservable.value == ZipInputValidationError.NONE

        return emailValid && phoneValid && streetAddressValid && cityValid && stateValid && zipValid
    }

    private fun validateSaveButtonState() {
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
        if (conditionallyIfError && currentState != EmailInputValidationError.NONE || !conditionallyIfError) {
            val newState = if (emailAddress.get()!!.isBlank()) {
                EmailInputValidationError.EMPTY
            } else if (!emailAddress.get()!!.contains("@")) {
                EmailInputValidationError.AT_REQUIRED
            } else {
                EmailInputValidationError.NONE
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