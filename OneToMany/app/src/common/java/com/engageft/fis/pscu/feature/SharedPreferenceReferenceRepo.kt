package com.engageft.fis.pscu.feature

import android.content.Context
import android.content.SharedPreferences
import com.engageft.fis.pscu.OneToManyApplication

/**
 * SharedPreferenceReferenceRepo
 * <p>
 * A Repo that concatenates references to every since SharedPreference file created by this application.
 * In order to do so, it relies of BaseSharedPreferencesRepo object to register itself when its
 * preferences are first accessed. Any SharedPreferences that are created without inheriting from
 * BaseSharedPreferencesRepo will not be tracked by this object, and therefore calling clear() method
 * will not clear those SharedPreferences.
 * </p>
 * Created by joeyhutchins on 10/5/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object SharedPreferenceReferenceRepo {
    private const val SHARED_PREFERENCE_REFERENCE_KEY = "SHARED_PREFERENCE_REFERENCE_KEY"
    val sharedPreferenceRefences: SharedPreferences = OneToManyApplication.sInstance.getSharedPreferences(SHARED_PREFERENCE_REFERENCE_KEY, Context.MODE_PRIVATE)

    fun registerSharedPreferenceKey(key: String) {
        val keys = sharedPreferenceRefences.getStringSet(SHARED_PREFERENCE_REFERENCE_KEY, HashSet<String>())!!
        keys.add(key)
        sharedPreferenceRefences.edit().putStringSet(SHARED_PREFERENCE_REFERENCE_KEY, keys).apply()
    }

    fun clearAllSharedPreferences() {
        val keys = sharedPreferenceRefences.getStringSet(SHARED_PREFERENCE_REFERENCE_KEY, HashSet<String>())!!
        for (key: String in keys) {
            val sharedPreferences = OneToManyApplication.sInstance.getSharedPreferences(key, Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        }
        sharedPreferenceRefences.edit().clear().apply()
    }
}