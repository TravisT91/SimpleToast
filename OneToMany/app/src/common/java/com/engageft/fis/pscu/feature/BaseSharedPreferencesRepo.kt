package com.engageft.fis.pscu.feature

import android.content.Context
import android.content.SharedPreferences
import com.engageft.fis.pscu.OneToManyApplication

/**
 * BaseSharedPreferencesRepo
 * <p>
 * Base class that all SharedPreference repositories or helper classes should inherit from. This
 * base object ensures that all these preferences are registered with the SharedPreferenceReferenceRepo
 * to enable convenience method for clearing all app shared preferences.
 * </p>
 * Created by joeyhutchins on 10/5/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
abstract class BaseSharedPreferencesRepo {
    abstract val sharedPreferencesKey: String
    protected val sharedPreferences: SharedPreferences by lazy {
        SharedPreferenceReferenceRepo.registerSharedPreferenceKey(sharedPreferencesKey)
        OneToManyApplication.sInstance.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
    }
}