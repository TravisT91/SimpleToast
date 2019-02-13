package com.engageft.feature.goals

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.apptoolbox.util.CurrencyUtils
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.utils.GoalsResponseUtils
import com.engageft.engagekit.utils.LoginResponseUtils
import com.engageft.feature.budgets.extension.isGreaterThan
import com.engageft.feature.budgets.extension.isLessThanOrEqualTo
import com.engageft.feature.budgets.extension.isZero
import com.engageft.feature.goals.utils.GoalConstants.SINGLE_TRANSFER_ACCOUNT_FORMAT
import com.engageft.feature.goals.utils.GoalConstants.AVAILABLE_BALANCE
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.DialogInfo
import com.ob.ws.dom.GoalsResponse
import com.ob.ws.dom.LoginResponse
import com.ob.ws.dom.utility.DebitCardInfo
import com.ob.ws.dom.utility.GoalInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import utilGen1.StringUtils
import java.lang.IllegalStateException
import java.math.BigDecimal

//todo: disable to and from fields when necessary
class GoalSingleTransferViewModel(val goalId: Long): BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }

    //todo put it outside of the class. rename TransferToType?
    enum class TransferType {
        GOAL, SPENDING_BALANCE
    }
    enum class AmountErrorState {
        EMPTY, VALID, EXCEEDS,
    }
    val amountValidationStateObservable = MutableLiveData<AmountErrorState>()

    enum class Event {
        ON_TEXT_CHANGED, ON_FOCUS_LOST, ENFORCED_VALIDATION
    }

    val nextButtonStateObservable = MutableLiveData<ButtonState>()
    val selectionOptionsListObservable = MutableLiveData<List<AccountSelectionOptions>>()
    val fromEnableObservable = MutableLiveData<Boolean>()
    private val accountsList = mutableListOf<AccountSelectionOptions>()
    var fromSelectionType: TransferType? = null

    val from = ObservableField("")
    val to = ObservableField("")
    val amount = ObservableField("")
    var transferAmount: BigDecimal = BigDecimal.ZERO

    private lateinit var goalInfo: GoalInfo
    private lateinit var debitCardInfo: DebitCardInfo

    private val fromOnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            populateToField(from.get()!!)
            validateAmount(Event.ENFORCED_VALIDATION)
            validateForm()
        }
    }

    private val toOnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateAmount(Event.ENFORCED_VALIDATION)
            validateForm()
        }
    }

    private val amountOnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            validateAmount(Event.ON_TEXT_CHANGED)
            validateForm()
        }
    }

    private fun getNonFormattedAmount(amount: String): BigDecimal {
        return if (amount.isEmpty()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, amount))
        }
    }

    init {
        nextButtonStateObservable.value = ButtonState.HIDE
        amountValidationStateObservable.value = AmountErrorState.EMPTY

        from.addOnPropertyChangedCallback(fromOnPropertyChangedCallback)
        to.addOnPropertyChangedCallback(toOnPropertyChangedCallback)
        amount.addOnPropertyChangedCallback(amountOnPropertyChangedCallback)

        initData()
    }

    fun validateAmount(event: Event) {
        fromSelectionType?.let {
            if (amount.get()!!.isNotEmpty()) {
                val transferAmount = getNonFormattedAmount(amount.get()!!)

                var isInvalid = false
                if (amountValidationStateObservable.value == AmountErrorState.EXCEEDS) {
                    isInvalid = true
                }

                when (event) {
                    Event.ON_TEXT_CHANGED -> {
                        if (isInvalid) {
                            validateAmountBasedOnTransferType(transferAmount)
                        }
                    }
                    Event.ON_FOCUS_LOST -> {
                        if (!isInvalid) {
                            validateAmountBasedOnTransferType(transferAmount)
                        }
                    }
                    Event.ENFORCED_VALIDATION -> {
                        validateAmountBasedOnTransferType(transferAmount)
                    }
                }
            } else {
                amountValidationStateObservable.value = AmountErrorState.EMPTY
            }
        }
    }

    private fun isAmountValid(amount: BigDecimal): Boolean {
        var availableAmount = BigDecimal.ZERO

        when (fromSelectionType) {
            TransferType.GOAL ->  availableAmount = goalInfo.fundAmount
            TransferType.SPENDING_BALANCE -> availableAmount = BigDecimal(debitCardInfo.currentBalance)
        }

        return amount.isLessThanOrEqualTo(availableAmount) && !amount.isZero()
    }

    private fun validateAmountBasedOnTransferType(transferAmount: BigDecimal) {
        when (fromSelectionType) {
            TransferType.GOAL ->  validateAmountAndNotifyObserver(goalInfo.fundAmount, transferAmount)
            TransferType.SPENDING_BALANCE -> validateAmountAndNotifyObserver(BigDecimal(debitCardInfo.currentBalance), transferAmount)
        }
    }

    private fun validateAmountAndNotifyObserver(availableAmount: BigDecimal, transferAmount: BigDecimal) {
        if (transferAmount.isGreaterThan(availableAmount)) {
            amountValidationStateObservable.value = AmountErrorState.EXCEEDS
        } else if (transferAmount.isLessThanOrEqualTo(availableAmount)) {
            amountValidationStateObservable.value = AmountErrorState.VALID
        }
    }

    private fun validateForm() {
        if (from.get()!!.isNotEmpty() && to.get()!!.isNotEmpty() && isAmountValid(getNonFormattedAmount(amount.get()!!))) {
            transferAmount = getNonFormattedAmount(amount.get()!!)
            nextButtonStateObservable.value = ButtonState.SHOW
        } else {
            nextButtonStateObservable.value = ButtonState.HIDE
        }
    }

    private fun populateToField(from: String) {
        accountsList.find {
            from != it.accountNameAndBalance
        }?.let {
            to.set(it.accountNameAndBalance)
            fromSelectionType = if (it.optionType != TransferType.GOAL) {
                TransferType.GOAL
            } else {
                TransferType.SPENDING_BALANCE
            }
        }
    }

    private fun initData() {
        showProgressOverlayDelayed()
        compositeDisposable.add(EngageService.getInstance().loginResponseAsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({ response ->
                    if (response.isSuccess && response is LoginResponse) {
                        // don't dismiss progressOverlay yet
                        val accountInfo = LoginResponseUtils.getCurrentAccountInfo(response)
                        debitCardInfo = accountInfo.debitCardInfo
                        val accountTypeAndBalance = String.format(
                                SINGLE_TRANSFER_ACCOUNT_FORMAT,
                                AVAILABLE_BALANCE,
                                StringUtils.formatCurrencyStringWithFractionDigits(debitCardInfo.currentBalance, true))
                        accountsList.add(AccountSelectionOptions(TransferType.SPENDING_BALANCE, accountTypeAndBalance))
                        getGoal(goalId, debitCardInfo)
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

    private fun getGoal(goalId: Long, debitCardInfo: DebitCardInfo) {
        compositeDisposable.add(
                EngageService.getInstance().goalsObservable(debitCardInfo, false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is GoalsResponse) {
                                GoalsResponseUtils.getGoalInfoWithId(response, goalId)?.let { goalInfo ->
                                    this.goalInfo = goalInfo
                                    val goalNameAndBalance = String.format(SINGLE_TRANSFER_ACCOUNT_FORMAT,
                                            goalInfo.name,
                                            StringUtils.formatCurrencyStringWithFractionDigits(goalInfo.fundAmount.toFloat(), true))
                                    accountsList.add(AccountSelectionOptions(TransferType.GOAL, goalNameAndBalance))
                                    determineState()
                                    selectionOptionsListObservable.value = accountsList
                                } ?: run {
                                    throw IllegalStateException("Goal not found")
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
    enum class TransfrerState {
        GOAL_COMPLETED,
        GOAL_CLOSED,

    }

    private fun determineState() {
        val availableBalance = BigDecimal(debitCardInfo.currentBalance)
        // if goal is achieved there's only one option: to transfer back to balance.
        // OR if the spending balance is zero
        if ((goalInfo.isAchieved || availableBalance.isZero())) {
            fromEnableObservable.value = false
            fromSelectionType = TransferType.GOAL

            accountsList.find {
                it.optionType == TransferType.GOAL
            }?.let { from.set(it.accountNameAndBalance) }

            accountsList.find {
                it.optionType == TransferType.SPENDING_BALANCE
            }?.let { to.set(it.accountNameAndBalance) }
        } else {
            if (goalInfo.fundAmount.isZero() && !availableBalance.isZero()) {
                fromEnableObservable.value = false
                fromSelectionType = TransferType.SPENDING_BALANCE

                accountsList.find {
                    it.optionType == TransferType.SPENDING_BALANCE
                }?.let { from.set(it.accountNameAndBalance) }

                accountsList.find {
                    it.optionType == TransferType.GOAL
                }?.let { to.set(it.accountNameAndBalance) }

            } else if (goalInfo.fundAmount.isZero() && availableBalance.isZero()) {
                // todo observer it
                dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.OTHER)
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return to.get()!!.isNotEmpty() || from.get()!!.isNotEmpty() || amount.get()!!.isNotEmpty()
    }

    data class AccountSelectionOptions(val optionType: GoalSingleTransferViewModel.TransferType, val accountNameAndBalance: String)
}

data class goalSingleTransferModel(val transferType: GoalSingleTransferViewModel.TransferType,
                                   val amount: BigDecimal,
                                   val goalId: Long)

const val ONE_TIME_TRANSFER_MIN = 0.01
const val ONE_TIME_TRANSFER_MAX = 1000000.00

//todo make this to return a generic viewModel?
class GoalSingleTransferViewModelFactory(private val goalId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalSingleTransferViewModel(goalId) as T
    }
}