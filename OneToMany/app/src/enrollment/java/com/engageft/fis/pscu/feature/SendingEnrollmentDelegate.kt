package com.engageft.fis.pscu.feature

import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.rest.request.ActivationRequest
import com.ob.domain.lookup.DebitCardStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class SendingEnrollmentDelegate(private val viewModel: EnrollmentViewModel) {

    enum class ActivationStatus {
        SUCCESS,
        FAIL
    }
    enum class CardActivationStatus {
        ACTIVE,
        LINKED
    }
    val successSubmissionObservable = SingleLiveEvent<ActivationStatus>()
    val cardActivationStatusObservable = SingleLiveEvent<CardActivationStatus>()

    fun submitAcceptTerms() {

        viewModel.compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postActivation(getActivationRequest().fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess) {
                                successSubmissionObservable.value = ActivationStatus.SUCCESS

                                if (viewModel.activationCardInfo.cardStatus == DebitCardStatus.PENDING_ACTIVATION.toString()) {
                                    cardActivationStatusObservable.value = CardActivationStatus.ACTIVE
                                } else {
                                    cardActivationStatusObservable.value = CardActivationStatus.LINKED
                                }
                            } else {
                                successSubmissionObservable.value = ActivationStatus.FAIL
                            }
                        }) { e ->
                            successSubmissionObservable.value = ActivationStatus.FAIL
                            viewModel.handleThrowable(e)
                        }
        )
    }

    private fun getActivationRequest(): ActivationRequest {
//        val request = ActivationRequest(viewModel.getStartedDelegate.cardNumber,
//                viewModel.getStartedDelegate.birthDate,
//                viewModel.cardPinDelegate.pinNumber.toString(),
//                "", "", "")
        val request = ActivationRequest("",
                DateTime.now(),
                "",
                "", "", "")

        if (viewModel.createAccountDelegate.userEmail.isNotEmpty()) {
            request.setEmail(viewModel.createAccountDelegate.userEmail)
        }
        if (viewModel.verifyIdentityDelegate.ssNumber.isNotEmpty()) {
            request.setSsn(viewModel.verifyIdentityDelegate.ssNumber)
        }
        return request
    }
}