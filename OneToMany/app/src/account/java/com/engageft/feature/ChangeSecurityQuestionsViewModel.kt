package com.engageft.feature

import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 11/12/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class ChangeSecurityQuestionsViewModel : BaseEngageViewModel() {
    enum class ChangeSecurityQuestionsNavigation {
        NONE,
        FINISH
    }

    enum class ChangeSecurityQuestionsMode {
        FETCHING, CHANGE, SET // SET is for first time, CHANGE is if questions were already set.
    }

    val navigationObservable = MutableLiveData<ChangeSecurityQuestionsNavigation>()
    val modeObservable = MutableLiveData<ChangeSecurityQuestionsMode>()
    val questions1List = MutableLiveData<List<String>>()
    val questions2List = MutableLiveData<List<String>>()
    val answer1 : ObservableField<String> = ObservableField("")
    val answer2 : ObservableField<String> = ObservableField("")
    val saveEnabled: ObservableField<Boolean> = ObservableField(false)

    init {
        //progressOverlayShownObservable.value = true
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
    }

    /**
     * Only possible when this has been set enabled.
     */
    fun onSaveClicked(v: View) {

    }

    private fun validateSaveButtonState() {
        saveEnabled.set( answer1.get()!!.isNotBlank() && answer2.get()!!.isNotBlank() )
    }
}