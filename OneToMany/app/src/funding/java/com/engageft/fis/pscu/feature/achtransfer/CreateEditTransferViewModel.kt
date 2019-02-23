package com.engageft.fis.pscu.feature.achtransfer

import android.os.Parcelable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.model.ScheduledLoad
import com.engageft.engagekit.rest.request.ScheduledLoadRequest
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.ob.domain.lookup.AchAccountStatus
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.ScheduledLoadsResponse
import com.ob.ws.dom.utility.AchAccountInfo
import com.ob.ws.dom.utility.CcAccountInfo
import com.ob.ws.dom.utility.DebitCardInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import utilGen1.DisplayDateTimeUtils
import utilGen1.ScheduledLoadUtils
import java.math.BigDecimal

class CreateEditTransferViewModel(scheduledLoadId: Long): BaseEngageViewModel() {

    enum class FormMode {
        CREATE,
        EDIT
    }
    enum class ButtonState {
        SHOW,
        HIDE
    }

    data class CardInfoModel(val cardId: Long, val name: String, val lastFour: String, val balance: BigDecimal)
    data class AccountFundSourceModel(val cardId: Long, val lastFour: String, val name: String? = null, val sourceType: FundSourceType)

    val fromAccountObservable = MutableLiveData<List<AccountFundSourceModel>>()
    val toAccountObservable = MutableLiveData<List<CardInfoModel>>()

    val formModeObservable = MutableLiveData<FormMode>()

    val deleteSuccessObservable = SingleLiveEvent<Unit>()

    val buttonStateObservable: MutableLiveData<ButtonState> = MutableLiveData()
    val isInErrorStateObservable = MutableLiveData<Boolean>()

    val fromAccount = ObservableField("")
    val toAccount  = ObservableField("")
    val amount = ObservableField("")
    val frequency = ObservableField("")
    val date1 = ObservableField("")
    val dayOfWeek = ObservableField("")
    var dayOfWeekShow = ObservableField(false)
    var date1Show = ObservableField(false)

    private var currentScheduledLoad: ScheduledLoad? = null

    var formMode = FormMode.CREATE

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
                        dayOfWeekShow.set(false)
                    }
                    FREQUENCY_MONTHLY -> {
                        date1Show.set(true)
                        dayOfWeekShow.set(false)
                    }
                    else -> {
                        dayOfWeekShow.set(true)
                        date1Show.set(false)
                    }
                }
            }
        })

        date1.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        dayOfWeek.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateButtonState()
            }
        })

        initData(scheduledLoadId)
    }

    private fun initData(scheduledLoadId: Long) {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // don't hide the progressOverlay yet
                    if (response is LoginResponse) {
                        getScheduledLoads(scheduledLoadId, response)
                    } else {
                        handleUnexpectedErrorResponse(response)
                    }
                }, { e ->
                    dismissProgressOverlay()
                    handleThrowable(e)
                })
        )
    }

    private fun getScheduledLoads(scheduledLoadId: Long, loginResponse: LoginResponse) {
        val currentCard = LoginResponseUtils.getCurrentCard(loginResponse)
        compositeDisposable.add(
                EngageService.getInstance().getScheduledLoadsResponseObservable(currentCard, true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is ScheduledLoadsResponse) {
                                ScheduledLoadUtils.getScheduledLoads(response).find { scheduleLoad ->
                                    scheduledLoadId == scheduleLoad.scheduledLoadId
                                }?.let { scheduledLoad ->
                                    currentScheduledLoad = scheduledLoad
                                    formMode = FormMode.EDIT
                                    populateDataFormModeEdit(loginResponse, scheduledLoad)
                                } ?: run {
                                    formMode = FormMode.CREATE
                                    initDataFormModeCreate(loginResponse)
                                }
                                formModeObservable.value = formMode
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }, { e ->
                            dismissProgressOverlay()
                            handleThrowable(e)
                        })
        )
    }

    private fun populateDataFormModeEdit(loginResponse: LoginResponse, scheduledLoad: ScheduledLoad) {
        // ensure this method is invoked for the correct mode
        if (formMode == FormMode.EDIT) {

            val cardInfoModelList = mutableListOf<CardInfoModel>()
            val fundSourceList = mutableListOf<AccountFundSourceModel>()

            //TODO(aHashimi): this may have to change for multi-card
            val currentCard = LoginResponseUtils.getCurrentCard(loginResponse)
            getCardInfo(currentCard)?.let { cardInfoModel ->
                cardInfoModelList.add(cardInfoModel)
            }

            // todo use ScheduledLoad.PLANNED_LOAD_STANDARD_SCH
            if (scheduledLoad.ccAccountId.isNotEmpty()) {
                loginResponse.ccAccountList.find { ccAccountInfo ->
                    scheduledLoad.ccAccountId.toLong() == ccAccountInfo.ccAccountId
                }?.let { ccAccountInfo ->
//                    fundSourceType = FundSourceType.DEBIT_CREDIT_CARD
                    fundSourceList.add(getFundSourceModelForCcAccount(ccAccountInfo))
                } ?: run {
                    throw IllegalStateException("CcAccountId not found")
                }
            } else if (scheduledLoad.achAccountId.isNotEmpty()) {
                loginResponse.achAccountList.find { achAccountInfo ->
                    scheduledLoad.achAccountId.toLong() == achAccountInfo.achAccountId
                }?.let { achAccountInfo ->
//                    fundSourceType = FundSourceType.ACH_ACCOUNT
                    fundSourceList.add(getFundSourceModelForAchAccount(achAccountInfo))
                } ?: run {
                    throw IllegalStateException("AchAccountInfo not found")
                }
            }

            // super annoying to set some fields here and some fields in View because View has to format the strings.
            toAccountObservable.value = cardInfoModelList
            fromAccountObservable.value = fundSourceList

            amount.set(scheduledLoad.amount)

            val dateTime = DisplayDateTimeUtils.shortDateFormatter.parseDateTime(scheduledLoad.scheduleDate)

            when (scheduledLoad.typeString) {
                ScheduledLoad.SCHED_LOAD_TYPE_WEEKLY -> {
                    frequency.set(FREQUENCY_WEEKLY)
                    dayOfWeekShow.set(true)
                    dayOfWeek.set(dateTime.dayOfWeek().asText)
                }
                ScheduledLoad.SCHED_LOAD_TYPE_ALT_WEEKLY -> {
                    frequency.set(FREQUENCY_EVERY_OTHER_WEEK)
                    dayOfWeekShow.set(true)
                    dayOfWeek.set(dateTime.dayOfWeek().asText)
                }
                ScheduledLoad.SCHED_LOAD_TYPE_MONTHLY -> {
                    frequency.set(FREQUENCY_MONTHLY)
                    date1Show.set(true)
                    date1.set(DisplayDateTimeUtils.getMediumFormatted(dateTime))
                }
            }
        }
    }

    private fun initDataFormModeCreate(loginResponse: LoginResponse) {
        // ensure this method is invoked for the correct mode
        if (formMode == FormMode.CREATE) {
            val cardInfoModelList = mutableListOf<CardInfoModel>()
            val fundSourceList = mutableListOf<AccountFundSourceModel>()

            val cardsList = LoginResponseUtils.getAllCardsSorted(loginResponse)

            if (cardsList.isNotEmpty()) {
                cardsList.forEach { debitCard ->
                    getCardInfo(debitCard)?.let { cardInfoModel ->
                        cardInfoModelList.add(cardInfoModel)
                    }
                }
            } else {
                throw IllegalStateException("Must have at least one DebitCardInfo")
            }

            loginResponse.achAccountList.forEach { achAccountInfo ->
                if (achAccountInfo.achAccountStatus == AchAccountStatus.VERIFIED) {
                    fundSourceList.add(getFundSourceModelForAchAccount(achAccountInfo))
                }
            }

            loginResponse.ccAccountList.forEach { ccAccountInfo ->
                fundSourceList.add(getFundSourceModelForCcAccount(ccAccountInfo))
            }

            toAccountObservable.value = cardInfoModelList
            fromAccountObservable.value = fundSourceList
        }
    }

    private fun getFundSourceModelForAchAccount(achAccountInfo: AchAccountInfo): AccountFundSourceModel {
        return AccountFundSourceModel(
                cardId = achAccountInfo.achAccountId,
                name = achAccountInfo.bankName,
                lastFour = achAccountInfo.accountLastDigits,
                sourceType = FundSourceType.ACH_ACCOUNT)
    }

    private fun getFundSourceModelForCcAccount(ccAccountInfo: CcAccountInfo): AccountFundSourceModel {
        return AccountFundSourceModel(
                cardId = ccAccountInfo.ccAccountId,
                lastFour = ccAccountInfo.lastDigits,
                sourceType = FundSourceType.DEBIT_CREDIT_CARD)
    }

    lateinit var transferFundsModel: TransferFundsModel

    fun updateButtonState() {
        if (amount.get()!!.isNotEmpty() && hasFrequencySelected() && hasUnsavedChanges()) {
            // init data
            setTransferFundModel()
            buttonStateObservable.value = ButtonState.SHOW
        } else {
            buttonStateObservable.value = ButtonState.HIDE
        }
    }

    private fun setTransferFundModel() {
        val transferAmount = BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, amount.get()!!))
        val fundSource = fundSourceMediator[fromAccount.get()!!]
        val fundDestination = fundDestinationMediator[toAccount.get()!!]

        var date: DateTime? = null

        val frequencyType: ScheduleLoadFrequencyType = when (frequency.get()!!) {
            FREQUENCY_WEEKLY -> {
                date = getDayOfWeek(dayOfWeek.get()!!)
                ScheduleLoadFrequencyType.WEEKLY
            }
            FREQUENCY_EVERY_OTHER_WEEK -> {
                date = getDayOfWeek(dayOfWeek.get()!!)
                ScheduleLoadFrequencyType.EVERY_OTHER_WEEK }

            FREQUENCY_MONTHLY -> {
                date = DisplayDateTimeUtils.mediumDateFormatter.parseDateTime(date1.get()!!)
                ScheduleLoadFrequencyType.MONTHLY
            }
            else -> {
                ScheduleLoadFrequencyType.ONCE
            }
        }

        if (fundSource != null && fundDestination != null) {
            transferFundsModel = TransferFundsModel(
                    fromId = fundSource.cardId,
                    toId = fundDestination.cardId,
                    amount = transferAmount,
                    frequency = frequencyType,
                    scheduleDate = date,
                    fundSourceType = fundSource.sourceType)
        }
    }

    private fun getDayOfWeek(dayOfWeek: String) : DateTime {
        val selectedDay = DisplayDateTimeUtils.getDayOfWeekNumber(dayOfWeek)
        val now = DateTime.now()
        val today: Int = DateTime.now().dayOfWeek + 1 // jodaTime is zero-based
        val nextRecurringDay = if (selectedDay > today) {
            selectedDay - today
        } else {
            DAYS_IN_A_WEEK - today + selectedDay
        }
        return now.plusDays(nextRecurringDay)
    }

    fun hasUnsavedChanges(): Boolean {
        return formMode == FormMode.CREATE && (amount.get()!!.isNotEmpty() || frequency.get()!!.isNotEmpty()
                || fromAccount.get()!!.isNotEmpty() && toAccount.get()!!.isNotEmpty())
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

    private fun getCardInfo(debitCardInfo: DebitCardInfo) : CardInfoModel? {
        BrandingInfoRepo.cards?.let { brandingCardsList ->
            for (brandingCard in brandingCardsList) {
                if (debitCardInfo.cardType == brandingCard.type) {
                    return CardInfoModel(cardId = debitCardInfo.debitCardId,
                            name = brandingCard.name,
                            lastFour = debitCardInfo.lastFour,
                            balance = BigDecimal(debitCardInfo.currentBalance))
                }
            }
        }
        return null
    }

    private fun hasFrequencySelected(): Boolean {
        val frequencyType = frequency.get()!!
        return frequencyType == FREQUENCY_ONETIME
                || (frequencyType == FREQUENCY_WEEKLY || frequencyType == FREQUENCY_EVERY_OTHER_WEEK && dayOfWeek.get()!!.isNotEmpty())
                || (frequencyType == FREQUENCY_MONTHLY && date1.get()!!.isNotEmpty())
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

//    var selectedSource: AccountFundSourceModel? = null
    lateinit var fundSourceMediator: HashMap<String, AccountFundSourceModel>
    fun setFundSourceListMediator(mediatorHashmap: HashMap<String, AccountFundSourceModel>) {
        fundSourceMediator = mediatorHashmap
//        fromAccountObservable.value?.let { fundSourceList ->
//            selectedSource = fundSourceList[index]
//        }
    }

//    var selectedCard: CardInfoModel? = null
    lateinit var fundDestinationMediator: HashMap<String, CardInfoModel>
    fun setSelectedCard(mediatorHashmap: HashMap<String, CardInfoModel>) {
        fundDestinationMediator = mediatorHashmap
//        toAccountObservable.value?.let { cardList ->
//            selectedCard = cardList[index]
//        }
    }

    private companion object {
        private const val DAYS_IN_A_WEEK = 7

        const val FREQUENCY_ONETIME = "One-time"
        const val FREQUENCY_WEEKLY = "Once a week"
        const val FREQUENCY_EVERY_OTHER_WEEK = "Every other week"
        const val FREQUENCY_MONTHLY = "Once a month"
    }
}
class CreateEditTransferViewModelFactory(private val scheduleLoadId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreateEditTransferViewModel(scheduleLoadId) as T
    }
}

enum class FundSourceType {
    DEBIT_CREDIT_CARD,
    ACH_ACCOUNT
}
// this enum matches EXACTLY the ScheduledLoadInfo frequency strings that we get from backend.
// we're doing this to prevent errors passing frequencies around.
enum class ScheduleLoadFrequencyType {
    ONCE,
    WEEKLY,
    EVERY_OTHER_WEEK,
    MONTHLY
}
@Parcelize
data class TransferFundsModel(val fromId: Long, val toId: Long, val amount: BigDecimal,
                         val frequency: ScheduleLoadFrequencyType, val scheduleDate: DateTime? = null,
                         val fundSourceType: FundSourceType): Parcelable