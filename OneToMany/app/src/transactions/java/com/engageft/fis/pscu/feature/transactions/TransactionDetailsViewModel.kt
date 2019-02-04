package com.engageft.fis.pscu.feature.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.repository.transaction.TransactionRepository
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.engagekit.rest.request.TransactionSetOffBudgetRequest
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.transactions.utils.TransactionId
import com.ob.ws.dom.BasicResponse
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils
import utilGen1.StringUtils.formatCurrencyStringWithFractionDigits
import kotlin.math.absoluteValue

class TransactionDetailsViewModel(transactionId: TransactionId) : BaseEngageViewModel() {

    private val storageManager = EngageService.getInstance().storageManager
    private val accountId = storageManager.currentAccountInfo.accountId
    private val cardId = storageManager.currentCard.debitCardId

    private var originalCategory: CharSequence? = null
    private var originalNotes: String? = null
    private var originalIsOffBudget: Boolean? = null

    val repoLiveData: LiveData<Transaction> = TransactionRepository.getTransaction(
            accountId = accountId.toString(),
            cardId = cardId.toString(),
            transactionId = transactionId.id)

    val transaction = MutableLiveData<Transaction>()
    val amount = MutableLiveData<String>().apply { value = "" }
    val txDate = MutableLiveData<String>().apply { value = "" }
    val txStore = MutableLiveData<String>().apply { value = "" }
    val txCategory = MutableLiveData<CharSequence>().apply {
        value = ""
    }
    val txCategoryDisplayString = MutableLiveData<String>().apply {
        value = ""
    }
    val txNotes = MutableLiveData<String>().apply { value = "" }
    val isOffBudget = MutableLiveData<Boolean>().apply { value = false }

    val hasChanges = MutableLiveData<Boolean>().apply { value = false }

    fun setTransaction(transaction: Transaction) {
        transaction.let {

            val amount = formatCurrencyStringWithFractionDigits(
                    it.amount.toFloat().absoluteValue, true)
            val date = DisplayDateTimeUtils.getMediumFormatted(it.date)
            val store = StringUtils.removeRedundantWhitespace(transaction.store)

            this.amount.value = amount
            this.txDate.value = date
            this.txStore.value = store

            this.txCategory.value = it.subCategory

            this.txNotes.value = it.note
            this.isOffBudget.value = it.offBudget

            originalCategory = it.subCategory
            originalNotes = it.note ?: ""
            originalIsOffBudget = it.offBudget

            this.transaction.postValue(it)
        }
    }

    fun checkForChanges(): Boolean {
        transaction.value?.let {

            val categoryDidNotChange = (txCategory.value == originalCategory)
            val offBudgetDidNotChange = (isOffBudget.value == originalIsOffBudget)
            val notesDidNotChange = (txNotes.value == originalNotes)
            val hasChanges =
                    ( categoryDidNotChange
                            && offBudgetDidNotChange
                            && notesDidNotChange)
                    .not()

            this.hasChanges.postValue(hasChanges)

            return hasChanges
        }
        return false
    }

    fun saveChanges() {
        transaction.value?.let { transaction ->
            val changesIterable = ArrayList<ObservableSource<BasicResponse>>()

            addCategoryUpdateIfChanged(changesIterable, transaction)
            addEmailUpdateIfChanged(changesIterable, transaction)
            addBudgetUpdateIfChanged(changesIterable, transaction)

            showProgressOverlayDelayed()
            Observable.mergeDelayError(changesIterable)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {

                            },

                            {
                                dismissProgressOverlay()
                                handleThrowable(it)
                            },

                            {
                                //default values should have changed if not this means something
                                //was unsuccessful
                                if (checkForChanges()) {
                                    handleUnexpectedErrorResponse(
                                            BasicResponse(false, ""))
                                }
                                dismissProgressOverlay()
                            }
                    )
        }
    }

    private fun addCategoryUpdateIfChanged
            (changesIterable: ArrayList<ObservableSource<BasicResponse>>,
             transaction: Transaction) {

        if (txCategory.value != originalCategory) {
            txCategory.value?.let {
                val engageService = EngageService.getInstance()
                val sm = engageService.storageManager
                val budgetCategory = sm.getBudgetCategoryForName(it.toString())
                changesIterable.add(
                        engageService.transactionChangeCategoryObservable(
                                transaction.transactionId,
                                budgetCategory.name,
                                true)
                                .doOnNext { response ->
                                    if (response.isSuccess) {
                                        originalCategory = txCategory.value
                                        TransactionRepository.updateTransactionCategory(transaction, budgetCategory.parentCategory.name, budgetCategory.name)
                                    }
                                }
                )
            }
        }
    }

    private fun addEmailUpdateIfChanged(
            changesIterable: ArrayList<ObservableSource<BasicResponse>>,
            transaction: Transaction) {
        if (txNotes.value != originalNotes) {
            txNotes.value?.let {
                val engageService = EngageService.getInstance()
                changesIterable.add(
                        engageService
                                .transactionUpdateNoteObservable(transaction.transactionId, it)
                                .doOnNext { response ->
                                    if (response.isSuccess) {
                                        originalNotes = txNotes.value
                                        TransactionRepository.updateTransactionNote(transaction, txNotes.value)
                                    }
                                }
                )
            }
        }
    }

    private fun addBudgetUpdateIfChanged(
            changesIterable: ArrayList<ObservableSource<BasicResponse>>,
            transaction: Transaction) {

        if (isOffBudget.value != originalIsOffBudget) {
            isOffBudget.value?.let {
                val engageService = EngageService.getInstance()
                val updateBudgetRequest = TransactionSetOffBudgetRequest(
                        transaction.transactionId,
                        it).fieldMap

                changesIterable.add(
                        engageService
                                .engageApiInterface
                                .postTransactionSetOffBudget(updateBudgetRequest)
                                .doOnNext { response ->
                                    if (response.isSuccess) {
                                        originalIsOffBudget = isOffBudget.value
                                    }
                                }
                )
            }
        }
    }


}

class TransactionViewModelFactory(private val transactionId: TransactionId)
    : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionDetailsViewModel(transactionId) as T
    }
}

