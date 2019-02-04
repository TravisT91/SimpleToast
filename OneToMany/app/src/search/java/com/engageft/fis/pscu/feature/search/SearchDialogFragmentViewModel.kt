package com.engageft.fis.pscu.feature.search

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.aac.SingleLiveEvent
import com.engageft.engagekit.repository.transaction.TransactionRepository
import com.engageft.engagekit.repository.transaction.toTransaction
import com.engageft.engagekit.repository.transaction.vo.Transaction
import com.engageft.engagekit.rest.request.TransactionsSearchRequest
import com.engageft.fis.pscu.feature.BaseEngageViewModel
import com.ob.ws.dom.TransactionsResponse
import io.reactivex.schedulers.Schedulers

/**
 * SearchDialogFragmentViewModel
 * <p>
 * ViewModel to manage transaction searches
 * </p>
 * Created by kurteous on 1/6/19.
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class SearchDialogFragmentViewModel : BaseEngageViewModel() {

    val searchTransactions: MutableLiveData<List<Transaction>> = MutableLiveData()
    val searchErrorObservable: SingleLiveEvent<SearchStringStatus> = SingleLiveEvent()

    fun searchTransactions(searchString: String) {
        if (searchString.length < TRANSACTION_SEARCH_MINIMUM_CHARS) {
            searchErrorObservable.value = SearchStringStatus.SEARCH_STRING_NOT_MINIMUM_LENGTH
        } else {
            searchErrorObservable.value = SearchStringStatus.VALID
            showProgressOverlayDelayed()
            val request = TransactionsSearchRequest(searchString)
            compositeDisposable.add(
                    EngageService.getInstance().engageApiInterface.postSearchTransactions(request.fieldMap)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.computation())
                            .subscribe({ response ->
                                if (response.isSuccess && response is TransactionsResponse) {
                                    if (response.transactionList != null && !response.transactionList.isEmpty()) {
                                        val transactionList = mutableListOf<Transaction>()
                                        for (transactionInfo in response.transactionList) {
                                            transactionList.add(transactionInfo.toTransaction(TransactionRepository.TransactionRepoType.ALL_ACTIVITY))
                                        }
                                        searchTransactions.postValue(transactionList)
                                        dismissProgressOverlay()
                                    } else {
                                        // transactions list was null or empty
                                        searchTransactions.postValue(listOf())
                                        dismissProgressOverlay()
                                    }
                                } else {
                                    handleUnexpectedErrorResponse(response)
                                    dismissProgressOverlay()
                                }
                            }) { e ->
                                handleThrowable(e)
                                dismissProgressOverlay()
                            }
            )
        }
    }

    companion object {
        private const val TRANSACTION_SEARCH_MINIMUM_CHARS = 2
    }

    enum class SearchStringStatus {
        SEARCH_STRING_NOT_MINIMUM_LENGTH,
        VALID
    }
}