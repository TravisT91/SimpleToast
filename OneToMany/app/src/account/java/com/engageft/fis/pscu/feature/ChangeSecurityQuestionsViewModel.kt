package com.engageft.fis.pscu.feature

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.EngageService
import com.engageft.engagekit.rest.request.AuthenticatedRequest
import com.engageft.engagekit.rest.request.SetSecurityQuestionsRequest
import com.ob.ws.dom.SecurityQuestionsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ChangeSecurityQuestionsViewModel
 * <p>
 * ViewModel for change security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangeSecurityQuestionsViewModel : BaseEngageViewModel() {
    enum class ChangeSecurityQuestionsNavigation {
        NONE,
        CHANGE_SUCCESSFUL,
        CREATE_SUCCESSFUL
    }

    enum class ChangeSecurityQuestionsMode {
        FETCHING, CHANGE, CREATE // CREATE is for first time, CHANGE is if questions were already set.
    }

    private val compositeDisposable = CompositeDisposable()
    val navigationObservable = MutableLiveData<ChangeSecurityQuestionsNavigation>()
    val modeObservable = MutableLiveData<ChangeSecurityQuestionsMode>()
    val questions1List = MutableLiveData<List<String>>()
    val questions2List = MutableLiveData<List<String>>()
    val question1 : ObservableField<String> = ObservableField("")
    val question2 : ObservableField<String> = ObservableField("")
    val answer1 : ObservableField<String> = ObservableField("")
    val answer2 : ObservableField<String> = ObservableField("")
    val saveEnabled: ObservableField<Boolean> = ObservableField(false)

    private lateinit var questionsList: List<String>

    init {
        progressOverlayShownObservable.value = true
        modeObservable.value = ChangeSecurityQuestionsMode.FETCHING
        questions1List.value = null
        questions2List.value = null

        answer1.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateSaveButtonState()
            }
        })
        answer2.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                validateSaveButtonState()
            }
        })
        loadSecurityQuestionState()
        question1.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                invalidateDisplayLists()
            }
        })
        question2.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(observable: Observable?, field: Int) {
                invalidateDisplayLists()
            }
        })
    }

    /**
     * Only possible when this has been set enabled.
     */
    fun onSaveClicked() {
        progressOverlayShownObservable.value = true


        val questionsAndAnswers = ArrayList<androidx.core.util.Pair<String, String>>()
        questionsAndAnswers.add(androidx.core.util.Pair(question1.get()!!, answer1.get()!!.trim { it <= ' ' }))
        questionsAndAnswers.add(androidx.core.util.Pair(question2.get()!!, answer2.get()!!.trim { it <= ' ' }))
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postSetSecurityQuestions(
                        SetSecurityQuestionsRequest(EngageService.getInstance().authManager.authToken,
                                questionsAndAnswers.toMutableList()).fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess) {
                                when (modeObservable.value) {
                                    ChangeSecurityQuestionsMode.CHANGE -> navigationObservable.value = ChangeSecurityQuestionsNavigation.CHANGE_SUCCESSFUL
                                    ChangeSecurityQuestionsMode.CREATE -> navigationObservable.value = ChangeSecurityQuestionsNavigation.CREATE_SUCCESSFUL
                                }
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }) { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        }
        )
    }

    private fun validateSaveButtonState() {
        saveEnabled.set( answer1.get()!!.isNotBlank() && answer2.get()!!.isNotBlank() )
    }

    private fun loadSecurityQuestionState() {
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postHasSecurityQuestions(AuthenticatedRequest(EngageService.getInstance().authManager.authToken).fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            if (response.isSuccess) {
                                if (response.message == "true") {
                                    // The user HAS security questions:
                                    modeObservable.value = ChangeSecurityQuestionsMode.CHANGE
                                } else {
                                    modeObservable.value = ChangeSecurityQuestionsMode.CREATE
                                }
                                loadStandardSecurityQuestions()
                            } else {
                                progressOverlayShownObservable.value = false
                                handleUnexpectedErrorResponse(response)
                            }

                        }) { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        }
        )
    }

    private fun loadStandardSecurityQuestions() {
        compositeDisposable.add(
                EngageService.getInstance().engageApiInterface.postStandardSecurityQuestions(AuthenticatedRequest(EngageService.getInstance().authManager.authToken).fieldMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            progressOverlayShownObservable.value = false
                            if (response.isSuccess && response is SecurityQuestionsResponse) {
                                questionsList = response.questions
                                invalidateDisplayLists()
                            } else {
                                handleUnexpectedErrorResponse(response)
                            }
                        }) { e ->
                            progressOverlayShownObservable.value = false
                            handleThrowable(e)
                        }
        )
    }

    private fun invalidateDisplayLists() {
        val currentQuestion1Selection = question1.get()!!
        val currentQuestion2Selection = question2.get()!!

        val filteredListFor1 = ArrayList(questionsList)
        filteredListFor1.remove(currentQuestion2Selection)

        val filteredListFor2 = ArrayList(questionsList)
        filteredListFor2.remove(currentQuestion1Selection)

        questions1List.value = filteredListFor1
        questions2List.value = filteredListFor2
    }
}