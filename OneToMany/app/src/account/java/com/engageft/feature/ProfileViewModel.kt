package com.engageft.feature

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable

/**
 * ChangeSecurityQuestionsViewModel
 * <p>
 * ViewModel for change security questions.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ProfileViewModel : BaseEngageViewModel() {
    enum class ProfileNavigation {
        NONE,
        CHANGE_SUCCESSFUL,
        CREATE_SUCCESSFUL
    }

    private val compositeDisposable = CompositeDisposable()
    val navigationObservable = MutableLiveData<ProfileNavigation>()

    val legalName : ObservableField<String> = ObservableField("")
    val emailAddress : ObservableField<String> = ObservableField("")
    val phoneNumber : ObservableField<String> = ObservableField("")
    val streetAddress : ObservableField<String> = ObservableField("")
    val aptSuite : ObservableField<String> = ObservableField("")
    val city : ObservableField<String> = ObservableField("")
    val state : ObservableField<String> = ObservableField("")
    val zip : ObservableField<String> = ObservableField("")

    private lateinit var questionsList: List<String>

    init {
//        progressOverlayShownObservable.value = true
//        modeObservable.value = ChangeSecurityQuestionsMode.FETCHING
//        legalName : ObservableField<String>.value = null
//        questions2List.value = null
//
//        answer1.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
//            override fun onPropertyChanged(observable: Observable?, field: Int) {
//                validateSaveButtonState()
//            }
//        })
//        answer2.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
//            override fun onPropertyChanged(observable: Observable?, field: Int) {
//                validateSaveButtonState()
//            }
//        })
//        loadSecurityQuestionState()
//        question1.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
//            override fun onPropertyChanged(observable: Observable?, field: Int) {
//                invalidateDisplayLists()
//            }
//        })
//        question2.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
//            override fun onPropertyChanged(observable: Observable?, field: Int) {
//                invalidateDisplayLists()
//            }
//        })
    }
}