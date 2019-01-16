package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.ActivationRequest
import com.ob.domain.lookup.DebitCardStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SendingEnrollmentDelegate(private val viewModel: EnrollmentViewModel) {

    enum class ActivationStatus {
        SUCCESS,
        FAIL,
        NONE
    }
    enum class CardActivationStatus {
        ACTIVE,
        LINKED,
        NONE
    }
    val successSubmissionObservable = MutableLiveData<ActivationStatus>()
    val cardActivationStatusObservable = MutableLiveData<CardActivationStatus>()

    fun submitAcceptTerms() {
        val request = getActivationRequest()

        viewModel.compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postActivation(request.fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess) {
                                successSubmissionObservable.value = ActivationStatus.SUCCESS
                                successSubmissionObservable.value = ActivationStatus.NONE
                                if (viewModel.activationCardInfo.cardStatus == DebitCardStatus.PENDING_ACTIVATION.toString()) {
                                    cardActivationStatusObservable.value = CardActivationStatus.ACTIVE
                                    cardActivationStatusObservable.value = CardActivationStatus.NONE
                                } else {
                                    cardActivationStatusObservable.value = CardActivationStatus.LINKED
                                    cardActivationStatusObservable.value = CardActivationStatus.NONE
                                }
                            } else {
                                successSubmissionObservable.value = ActivationStatus.FAIL
                                successSubmissionObservable.value = ActivationStatus.NONE
                            }
                        }) { e ->
                            successSubmissionObservable.value = ActivationStatus.FAIL
                            successSubmissionObservable.value = ActivationStatus.NONE
                            viewModel.handleThrowable(e)
                        }
        )
    }

    private fun getActivationRequest(): ActivationRequest {
        val request = ActivationRequest(viewModel.getStartedDelegate.cardNumber,
                viewModel.getStartedDelegate.birthDate,
                viewModel.cardPinDelegate.pinNumber.toString(),
                "",
                "",
                "")
        if (viewModel.createAccountDelegate.userEmail.isNotEmpty()) {
            request.setEmail(viewModel.createAccountDelegate.userEmail)
        }

        if (viewModel.verifyIdentityDelegate.ssNumber.isNotEmpty()) {
            request.setSsn(viewModel.verifyIdentityDelegate.ssNumber)
        }
        return request
    }
}