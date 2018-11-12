package com.engageft.feature

/**
 * WelcomeSharedPreferencesRepo
 * <p>
 * SharedPreferences utility repo object for the Welcome feature. This class stores KEYS and DEFAULT
 * values for storing/retrieving Android shared preferences related to this feature. This class provides
 * convenience methods to get and set values.
 * </p>
 * Created by joeyhutchins on 10/5/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object WelcomeSharedPreferencesRepo : BaseSharedPreferencesRepo() {
    private const val HAS_SEEN_GET_STARTED_KEY = "HAS_SEEN_GET_STARTED_KEY"
    private const val HAS_SEEN_GET_STARTED_DEF = false

    override val sharedPreferencesKey = "WelcomeSharedPreferencesRepo"

    fun hasSeenGetStarted(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_GET_STARTED_KEY, HAS_SEEN_GET_STARTED_DEF)
    }

    fun applyHasSeenGetStarted(hasSeenGetStarted: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_SEEN_GET_STARTED_KEY, hasSeenGetStarted).apply()
    }
}