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

class GoalSingleTransferViewModel(val goalId: Long): BaseEngageViewModel() {
    enum class ButtonState {
        SHOW,
        HIDE
    }
    enum class TransferType {
        GOAL,
        SPENDING_BALANCE
    }
    enum class AmountErrorState {
        EMPTY,
        VALID,
        EXCEEDS_REMAINING_GOAL,
        EXCEEDS_REMAINING_BALANCE,
    }
    enum class TransferState {
        GOAL_COMPLETED,
        GOAL_INSUFFICIENT_FUNDS,
        ACCOUNT_INSUFFICIENT_FUNDS,
        INSUFFICIENT_FUNDS,
        DEFAULT
    }
    enum class Event {
        ON_TEXT_CHANGED,
        ON_FOCUS_LOST,
        ENFORCED_VALIDATION
    }

    val amountValidationStateObservable = MutableLiveData<AmountErrorState>()
    val nextButtonStateObservable = MutableLiveData<ButtonState>()
    val selectionOptionsListObservable = MutableLiveData<ArrayList<CharSequence>>()
    val selectionEnableObservable = MutableLiveData<Boolean>()

    val from = ObservableField("")
    val to = ObservableField("")
    val amount = ObservableField("")

    var transferAmount: BigDecimal = BigDecimal.ZERO
    var fromSelectionType: TransferType? = null

    private var currentTransferState: TransferState = TransferState.DEFAULT
    private val accountsList = mutableListOf<SelectionOptions>()
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

    init {
        nextButtonStateObservable.value = ButtonState.HIDE
        amountValidationStateObservable.value = AmountErrorState.EMPTY

        from.addOnPropertyChangedCallback(fromOnPropertyChangedCallback)
        to.addOnPropertyChangedCallback(toOnPropertyChangedCallback)
        amount.addOnPropertyChangedCallback(amountOnPropertyChangedCallback)

        initData()
    }

    fun hasUnsavedChanges(): Boolean {
        val hasPrePopulatedData = currentTransferState != TransferState.DEFAULT

        return ((!hasPrePopulatedData && (to.get()!!.isNotEmpty() || from.get()!!.isNotEmpty())) || amount.get()!!.isNotEmpty())
                || (hasPrePopulatedData && amount.get()!!.isNotEmpty())
    }

    fun validateAmount(event: Event) {
        if (amount.get()!!.isNotEmpty()) {
            val transferAmount = getNonFormattedAmount(amount.get()!!)

            var shouldValid = false
            if (amountValidationStateObservable.value == AmountErrorState.EXCEEDS_REMAINING_BALANCE
                    || amountValidationStateObservable.value == AmountErrorState.EXCEEDS_REMAINING_GOAL) {
                shouldValid = true
            }

            when (event) {
                Event.ON_TEXT_CHANGED -> {
                    if (shouldValid) {
                        validateAmountBasedOnTransferType(transferAmount)
                    }
                }
                Event.ON_FOCUS_LOST -> {
                    if (!shouldValid) {
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
                EngageService.getInstance().goalsObservable(debitCardInfo, true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            dismissProgressOverlay()
                            if (response.isSuccess && response is GoalsResponse) {
                                GoalsResponseUtils.getGoalInfoWithId(response, goalId)?.let { goalInfo ->
                                    this.goalInfo = goalInfo
                                    val nameAndAmountList = ArrayList<CharSequence>()

                                    val goalNameAndBalance = String.format(SINGLE_TRANSFER_ACCOUNT_FORMAT,
                                            goalInfo.name,
                                            StringUtils.formatCurrencyStringWithFractionDigits(goalInfo.fundAmount.toFloat(), true))

                                    val accountAndBalance = String.format(
                                            SINGLE_TRANSFER_ACCOUNT_FORMAT,
                                            AVAILABLE_BALANCE,
                                            StringUtils.formatCurrencyStringWithFractionDigits(debitCardInfo.currentBalance, true))

                                    accountsList.add(SelectionOptions(TransferType.GOAL, goalNameAndBalance))
                                    accountsList.add(SelectionOptions(TransferType.SPENDING_BALANCE, accountAndBalance))
                                    nameAndAmountList.add(goalNameAndBalance)
                                    nameAndAmountList.add(accountAndBalance)

                                    determineState()

                                    selectionOptionsListObservable.value = nameAndAmountList
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

    private fun determineState() {
        val availableBalance = BigDecimal(debitCardInfo.currentBalance)

        currentTransferState = when {
            goalInfo.isAchieved -> TransferState.GOAL_COMPLETED
            goalInfo.fundAmount.isZero() && availableBalance.isZero() -> TransferState.INSUFFICIENT_FUNDS
            goalInfo.fundAmount.isZero() -> TransferState.GOAL_INSUFFICIENT_FUNDS
            availableBalance.isZero() -> TransferState.ACCOUNT_INSUFFICIENT_FUNDS
            else -> TransferState.DEFAULT
        }

        when (currentTransferState) {
            TransferState.GOAL_COMPLETED -> populatePredeterminedTransfer(TransferType.GOAL)
            TransferState.GOAL_INSUFFICIENT_FUNDS -> populatePredeterminedTransfer(TransferType.SPENDING_BALANCE)
            TransferState.ACCOUNT_INSUFFICIENT_FUNDS -> populatePredeterminedTransfer(TransferType.GOAL)
            TransferState.INSUFFICIENT_FUNDS -> dialogInfoObservable.value = DialogInfo(dialogType = DialogInfo.DialogType.OTHER)
            else -> {} // left intentionally blank
        }
    }

    private fun populatePredeterminedTransfer(transferFrom: TransferType) {
        selectionEnableObservable.value = false
        fromSelectionType = transferFrom

        accountsList.find {
            it.optionType == transferFrom
        }?.let { from.set(it.nameAndBalance) }

        accountsList.find {
            it.optionType != transferFrom
        }?.let { to.set(it.nameAndBalance) }
    }

    private fun populateToField(from: String) {
        // only populate if it's in default state
        if (currentTransferState == TransferState.DEFAULT) {
            accountsList.find {
                from != it.nameAndBalance
            }?.let {
                to.set(it.nameAndBalance)
                fromSelectionType = if (it.optionType != TransferType.GOAL) {
                    TransferType.GOAL
                } else {
                    TransferType.SPENDING_BALANCE
                }
            }
        }
    }

    private fun isAmountValid(amount: BigDecimal): Boolean {
        val goalFundAmountRemaining = goalInfo.amount - goalInfo.fundAmount
        return when (fromSelectionType) {
            TransferType.GOAL -> amount.isLessThanOrEqualTo(goalInfo.fundAmount) && !amount.isZero()
            TransferType.SPENDING_BALANCE -> amount.isLessThanOrEqualTo(BigDecimal(debitCardInfo.currentBalance))
                    && !amount.isGreaterThan(goalFundAmountRemaining) && !amount.isZero()
            else -> false
        }
    }

    private fun validateAmountBasedOnTransferType(transferAmount: BigDecimal) {
        when (fromSelectionType) {
            TransferType.GOAL -> {
                if (transferAmount.isGreaterThan(goalInfo.fundAmount)) {
                    amountValidationStateObservable.value = AmountErrorState.EXCEEDS_REMAINING_GOAL
                } else {
                    amountValidationStateObservable.value = AmountErrorState.VALID
                }
            }
            TransferType.SPENDING_BALANCE -> {
                val goalFundAmountRemaining = goalInfo.amount - goalInfo.fundAmount
                val balance = BigDecimal(debitCardInfo.currentBalance)
                if (transferAmount.isGreaterThan(balance)) {
                    amountValidationStateObservable.value = AmountErrorState.EXCEEDS_REMAINING_BALANCE
                } else if (!transferAmount.isGreaterThan(balance) && transferAmount.isGreaterThan(goalFundAmountRemaining)) {
                    amountValidationStateObservable.value = AmountErrorState.EXCEEDS_REMAINING_GOAL
                } else {
                    amountValidationStateObservable.value = AmountErrorState.VALID
                }
            }
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

    private fun getNonFormattedAmount(amount: String): BigDecimal {
        return if (amount.isEmpty()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(CurrencyUtils.getNonFormattedDecimalAmountString(EngageAppConfig.currencyCode, amount))
        }
    }

    data class SelectionOptions(val optionType: GoalSingleTransferViewModel.TransferType, val nameAndBalance: String)
}

class GoalSingleTransferViewModelFactory(private val goalId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalSingleTransferViewModel(goalId) as T
    }
}