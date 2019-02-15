package com.engageft.fis.pscu.feature.achtransfer

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.ScheduledLoadRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.ScheduledLoadsResponse
import com.ob.ws.dom.utility.AchAccountInfo
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils

class CreateEditTransferViewModel: BaseEngageViewModel() {

    enum class FormMode {
        CREATE,
        EDIT
    }

    enum class ButtonState {
        SHOW,
        HIDE
    }

    data class CardInfo(var cardId: Long, var name: String, var lastFour: String)
    val fromAccountObservable = MutableLiveData<AchAccountInfo>()
    val toAccountObservable = MutableLiveData<CardInfo>()

    val deleteSuccessObservable = SingleLiveEvent<Unit>()

    val buttonStateObservable: MutableLiveData<ButtonState> = MutableLiveData()

    val fromAccount = ObservableField("")
    val toAccount  = ObservableField("")
    val amount = ObservableField("")
    val frequency = ObservableField("")
    val date1 = ObservableField("")
    val date2 = ObservableField("")
    val dayOfWeek = ObservableField("")
    var dayOfWeekShow = ObservableField(false)
    var date1Show = ObservableField(false)
    var date2Show = ObservableField(false)

    var achAccountInfo: AchAccountInfo? = null
    var currentCard: DebitCardInfo? = null

    private var currentScheduledLoad: ScheduledLoad? = null
    private var achAccountList: List<AchAccountInfo> = mutableListOf()

    init {
        buttonStateObservable.value = ButtonState.HIDE

        fromAccount.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        amount.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        frequency.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
                when (frequency.get()) {
                    FREQUENCY_ONETIME -> {
                        date1Show.set(false)
                        date2Show.set(false)
                        dayOfWeekShow.set(false)
                    }
                    FREQUENCY_WEEKLY -> {
                        dayOfWeekShow.set(true)
                        date1Show.set(false)
                        date2Show.set(false)
                    }
                    FREQUENCY_MONTHLY -> {
                        date1Show.set(true)
                        date2Show.set(false)
                        dayOfWeekShow.set(false)
                    }
                    FREQUENCY_BIMONTHLY -> {
                        date1Show.set(true)
                        date2Show.set(true)
                        dayOfWeekShow.set(false)
                    }
                }
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

        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    dismissProgressOverlay()
                    if (response is LoginResponse) {
                        val isAchFundingAllowed = LoginResponseUtils.getCurrentAccountInfo(response).accountPermissionsInfo.isFundingAchEnabled
                        if (isAchFundingAllowed) {
                            //TODO(aHashimi): populate the To and From account fields since multiple cards and ACH out is not supported.
                            if (response.achAccountList.isNotEmpty()) {
                                // multiple ACH Banks aren't allowed
                                achAccountInfo = response.achAccountList[0]
                                fromAccountObservable.value = achAccountInfo
                            }
                            currentCard = LoginResponseUtils.getCurrentCard(response)
                            toAccountObservable.value = getCardInfo(currentCard!!)
                        } else {
                            dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.OTHER)
                        }
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    fun initScheduledLoads(scheduledLoadId: Long) {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // don't hide the progressOverlay yet
                    if (response is LoginResponse) {
                        getScheduledLoads(scheduledLoadId, LoginResponseUtils.getCurrentCard(response))
                    } else {
                        dialogInfoObservable.value = DialogInfo()
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private fun getScheduledLoads(scheduledLoadId: Long, currentCard: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().getScheduledLoadsResponseObservable(currentCard, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is ScheduledLoadsResponse) {
                                for (scheduledLoad in ScheduledLoadUtils.getScheduledLoads(response)) {
                                    if (scheduledLoad.scheduledLoadId == scheduledLoadId) {
                                        populateFields(scheduledLoad)
                                        break
                                    }
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    fun updateButtonState() {
        if (amount.get()!!.isNotEmpty() && hasFrequencySelected() && hasUnsavedChanges()) {
            buttonStateObservable.value = ButtonState.SHOW
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
    }

    fun hasUnsavedChanges(): Boolean {
        if (formMode == FormMode.CREATE && (amount.get()!!.isNotEmpty() || frequency.get()!!.isNotEmpty())) {
            return true
        }
        return false
    }

    fun onDeleteScheduledLoad() {
        showProgressOverlayImmediate()

        currentScheduledLoad?.let { scheduledLoad ->
            val request = ScheduledLoadRequest(scheduledLoad.scheduledLoadId)
            postCancelScheduledLoad(request.fieldMap) { // first is successful
                if (scheduledLoad.isHasDuplicate) {
                    val request2 = ScheduledLoadRequest(scheduledLoad.scheduledLoadIdDup)
                    postCancelScheduledLoad(request2.fieldMap) { // successful
                        EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
                        deleteSuccessObservable.call()
                    }
                } else { // Does not have duplicate
                    EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
                    deleteSuccessObservable.call()
                }

                EngageService.getInstance().storageManager.clearForLoginWithDataLoad(false)
            }
        }
    }

    private var formMode = FormMode.CREATE

    private fun populateFields(scheduledLoad: ScheduledLoad) {
        currentScheduledLoad = scheduledLoad
        formMode = FormMode.EDIT

        for (achAccountInfo in achAccountList) {
            if (achAccountInfo.achAccountId.toString() == scheduledLoad.achAccountId) {
                // format and set from account
                fromAccountObservable.value = achAccountInfo
                break
            }
        }

        currentCard?.let {
            toAccountObservable.value = getCardInfo(it)
        }

        amount.set(scheduledLoad.amount)

        val dateTime = DisplayDateTimeUtils.shortDateFormatter.parseDateTime(scheduledLoad.scheduleDate)

        when (scheduledLoad.typeString) {
            ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> {
                frequency.set(FREQUENCY_WEEKLY)
                dayOfWeekShow.set(true)
                dayOfWeek.set(dateTime.dayOfWeek().asText)
            }
            ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> {
                frequency.set(FREQUENCY_MONTHLY)
                date1Show.set(true)
                date1.set(DisplayDateTimeUtils.getMediumFormatted(dateTime))
            }
            ScheduledLoad.SCHED_LOAD_TYPE_TWICE_MONTHLY -> {
                frequency.set(FREQUENCY_BIMONTHLY)
                date1Show.set(true)
                date2Show.set(true)
                date1.set(DisplayDateTimeUtils.getMediumFormatted(dateTime))
                val dateTime2 = DisplayDateTimeUtils.shortDateFormatter.parseDateTime(scheduledLoad.scheduleDate2)
                date2.set(DisplayDateTimeUtils.getMediumFormatted(dateTime2))
            }
        }
    }

    private fun getCardInfo(debitCardInfo: DebitCardInfo) : CardInfo? {
        BrandingInfoRepo.cards?.let { brandingCardsList ->
            for (brandingCard in brandingCardsList) {
                if (debitCardInfo.cardType == brandingCard.type) {
                    return CardInfo(cardId = debitCardInfo.debitCardId, name = brandingCard.name, lastFour = debitCardInfo.lastFour)
                }
            }
        }

        return null
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

    private fun postCancelScheduledLoad(map: MutableMap<String, String>, cancelLoadSuccessObserver: () -> Unit) {
        compositeDisposable.add(EngageService.getInstance().engageApiInterface.postCancelScheduledLoad(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response: BasicResponse ->
                    if (response.isSuccess) {
                        cancelLoadSuccessObserver()
                    } else {
                        dismissProgressOverlay()
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private companion object {
        const val FREQUENCY_ONETIME = "One-time"
        const val FREQUENCY_WEEKLY = "Once a week"
        const val FREQUENCY_MONTHLY = "Once a month"
        const val FREQUENCY_BIMONTHLY = "Twice a month"
    }
}