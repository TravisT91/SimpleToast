package com.engageft.fis.pscu.feature.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.repository.transaction.TransactionRepository
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.engageft.fis.pscu.feature.transactions.utils.TransactionId
import utilGen1.DisplayDateTimeUtils
import utilGen1.StringUtils
import utilGen1.StringUtils.formatCurrencyStringWithFractionDigits
import kotlin.math.absoluteValue

class TransactionDetailsViewModel(transactionId: TransactionId) : BaseEngageViewModel() {

    private val storageManager = EngageService.getInstance().storageManager
    private val accountId  = storageManager.currentAccountInfo.accountId
    private val cardId = storageManager.currentCard.debitCardId

    private var origionalCategory :CharSequence? = null
    private var origionalNotes : String? = null
    private var origionalIsOffBudget : Boolean? = null

    val repoLiveData: LiveData<Transaction> =  TransactionRepository.getTransaction(
            repoType = TransactionRepository.TransactionRepoType.ALL_ACTIVITY,
            accountId = accountId.toString(),
            cardId = cardId.toString(),
            transactionId = transactionId.id)

    val transaction = MutableLiveData<Transaction>()
    val amount = MutableLiveData<String>().apply { value = "" }
    val txDate = MutableLiveData<String>().apply { value = "" }
    val txStore = MutableLiveData<String>().apply { value = "" }
    val txCategory = MutableLiveData<CharSequence>().apply { value = "" }
    val txNotes = MutableLiveData<String>().apply { value = "" }
    val isOffBudget = MutableLiveData<Boolean>().apply { value = false }

    val hasChanges = MutableLiveData<Boolean>().apply { value = false }

    fun setTransaction(transaction: Transaction){
        transaction.let {

            val amount = formatCurrencyStringWithFractionDigits(
                    it.amount.toFloat().absoluteValue, true)
            val date = DisplayDateTimeUtils.getMediumFormatted(it.date)
            val store = StringUtils.removeRedundantWhitespace(transaction.store)

            this.transaction.postValue(it)
            this.amount.postValue(amount)
            this.txDate.postValue(date)
            this.txStore.postValue(store)
            this.txCategory.postValue(it.category)
            this.txNotes.postValue(it.note)
            this.isOffBudget.postValue(it.offBudget)

            origionalCategory = it.category ?: ""
            origionalNotes = it.note ?: ""
            origionalIsOffBudget = it.offBudget

            this.transaction.postValue(it)
        }
    }

    fun checkForChanges() {
        transaction.value?.let{

            val categoryDidNotChange = (txCategory.value == origionalCategory)
            val offBudgetDidNotChange = (isOffBudget.value == origionalIsOffBudget)
            val notesDidNotChange = (txNotes.value == origionalNotes)
            val hasChanges = (categoryDidNotChange && offBudgetDidNotChange && notesDidNotChange).not()

             this.hasChanges.postValue(hasChanges)
        }
    }
}

class TransactionViewModelFactory(private val transactionId: TransactionId)
    : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionDetailsViewModel(transactionId) as T
    }
}
