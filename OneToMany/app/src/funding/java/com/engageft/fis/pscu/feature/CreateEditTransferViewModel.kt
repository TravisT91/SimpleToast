package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.LoginResponseUtils
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.AccountInfo
import com.ob.ws.dom.utility.AchAccountInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class CreateEditTransferViewModel: BaseEngageViewModel() {

    private companion object {
       const val FREQUENCY_ONETIME = "One-time"
       const val FREQUENCY_WEEKLY = "Once a week"
       const val FREQUENCY_MONTHLY = "Once a month"
       const val FREQUENCY_BIMONTHLY = "Twice a month"
    }

    enum class ButtonState {
        SHOW,
        HIDE
    }

    val achAccountListObservable: MutableLiveData<List<AchAccountInfo>> = MutableLiveData()
    val currentAccountListObservable: MutableLiveData<List<AccountInfo>> = MutableLiveData()
    val buttonStateObservable: MutableLiveData<ButtonState> = MutableLiveData()

    val fromAccount : ObservableField<String> = ObservableField("")
    val toAccount : ObservableField<String> = ObservableField("")
    val amount : ObservableField<String> = ObservableField("")
    val frequency : ObservableField<String> = ObservableField("")
    val date1 : ObservableField<String> = ObservableField("")
    val date2 : ObservableField<String> = ObservableField("")
    val dayOfWeek : ObservableField<String> = ObservableField("")

    var achAccountInfoId = -1L
    var cardId = -1L

    init {
        buttonStateObservable.value = ButtonState.HIDE

        fromAccount.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        frequency.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        date1.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        date2.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        dayOfWeek.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        progressOverlayShownObservable.value = true
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressOverlayShownObservable.value = false
                    if (response is LoginResponse) {
                        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                        val currentCard = LoginResponseUtils.getCurrentCard(response)
                        achAccountListObservable.value = response.achAccountList
                        cardId = currentCard.debitCardId
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    progressOverlayShownObservable.value = false
                    handleThrowable(e)
                })
        )
    }

    fun updateButtonState() {
        if (fromAccount.get()!!.isNotEmpty() && toAccount.get()!!.isNotEmpty() && amount.get()!!.isNotEmpty()
                && hasFrequencySelected()) {
            buttonStateObservable.value = ButtonState.SHOW
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
    }

    private fun hasFrequencySelected(): Boolean {
        val frequencyType = frequency.get()!!
        if (frequencyType == FREQUENCY_ONETIME
                || (frequencyType == FREQUENCY_WEEKLY && dayOfWeek.get()!!.isNotEmpty())
                || (frequencyType == FREQUENCY_MONTHLY && date1.get()!!.isNotEmpty())
                || (frequencyType == FREQUENCY_BIMONTHLY && date1.get()!!.isNotEmpty() && date2.get()!!.isNotEmpty())) {
            return true
        }
        return false
    }

    fun hasUnsavedChanges(): Boolean {
        return false
    }

}