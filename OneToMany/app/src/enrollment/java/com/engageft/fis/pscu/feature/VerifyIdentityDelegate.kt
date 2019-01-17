package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.engageft.apptoolbox.util.isDigitsOnly
import com.engageft.fis.pscu.feature.gatekeeping.GateKeeperListener
import com.engageft.fis.pscu.feature.gatekeeping.GatedItem
import com.engageft.fis.pscu.feature.gatekeeping.VerifyIdentityEnrollmentGateKeeper
import com.engageft.fis.pscu.feature.gatekeeping.items.TermsRequiredGatedItem

class VerifyIdentityDelegate(private val viewModel: EnrollmentViewModel,
                             private val navController: NavController,
                             private val verifyIdentityNavigations: EnrollmentViewModel.EnrollmentNavigations.VerifyIdentityNavigations) {

    private val gateKeeperListener: GateKeeperListener = object : GateKeeperListener {
        override fun onGateOpen() {
            navController.navigate(verifyIdentityNavigations.verifyIdentityToSending)
        }

        override fun onGatedItemFailed(item: GatedItem) {
            when (item) {
                is TermsRequiredGatedItem -> {
                    navController.navigate(verifyIdentityNavigations.verifyIdentityToTerms)
                }
            }
        }

        override fun onItemError(item: GatedItem, e: Throwable?, message: String?) {
            // Intentionally empty and will never be called.
        }
    }

    enum class ssnValidationError {
        NONE,
        EMPTY,
        INVALID // must be 9-digits
    }

    enum class NextButtonState {
        GONE,
        VISIBLE_ENABLED
    }

    val ssn = ObservableField("")
    lateinit var ssNumber: String

    val nextButtonObservable = MutableLiveData<NextButtonState>()
    val ssnValidationErrorObservable = MutableLiveData<ssnValidationError>()

    init {
        nextButtonObservable.value = NextButtonState.GONE
        ssnValidationErrorObservable.value = ssnValidationError.NONE

        ssn.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                ssNumber = ssn.get()!!
                validateSSNConditionally(true)
                updateButtonState()
            }
        })
    }

    fun validateSSNConditionally(validateConditionally: Boolean) {
        val currentState = ssnValidationErrorObservable.value
        if ((validateConditionally && currentState != ssnValidationError.NONE && currentState != ssnValidationError.EMPTY)
                || !validateConditionally) {
            val newState: ssnValidationError = when {
                ssn.get()!!.isEmpty() -> ssnValidationError.EMPTY
                isSsnValid() -> ssnValidationError.NONE
                else -> ssnValidationError.INVALID
            }

            if (currentState != newState) {
                ssnValidationErrorObservable.value = newState
                updateButtonState()
            }
        }
    }

    fun onNextClicked() {
        validateSSNConditionally(false)
        if (isSsnValid()) {
            ssNumber = ssn.get()!!
            val gateKeeper = VerifyIdentityEnrollmentGateKeeper(viewModel.activationCardInfo, gateKeeperListener)
            gateKeeper.run()
        }
    }

    fun reset() {
        ssn.set("")
        nextButtonObservable.value = NextButtonState.GONE
    }

    private fun updateButtonState() {
        nextButtonObservable.value = if (ssn.get()!!.isEmpty()
                || ssnValidationErrorObservable.value == ssnValidationError.INVALID) {
            NextButtonState.GONE
        } else {
            NextButtonState.VISIBLE_ENABLED
        }
    }

    private fun isSsnValid(): Boolean {
        return ssn.get()!!.isDigitsOnly() && ssn.get()!!.length == 9
    }

}