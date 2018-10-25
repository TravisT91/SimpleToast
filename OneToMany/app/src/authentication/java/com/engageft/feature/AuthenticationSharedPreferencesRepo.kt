package com.engageft.feature

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 10/24/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
object AuthenticationSharedPreferencesRepo : BaseSharedPreferencesRepo() {
    private const val USING_DEMO_SERVER = "USING_DEMO_SERVER"
    private const val USING_DEMO_SERVER_DEF = false
    private const val DEMO_SAVED_USERNAME = "DEMO_SAVED_USERNAME"
    private const val DEMO_SAVED_USERNAME_DEF = ""
    private const val IS_FIRST_USE = "IS_FIRST_USE"
    private const val IS_FIRST_USE_DEF = true
    private const val SAVED_USERNAME = "SAVED_USERNAME"
    private const val SAVED_USERNAME_DEF = ""

    override val sharedPreferencesKey: String = "AuthenticationSharedPreferencesRepo"

    fun isUsingDemoServer(): Boolean {
        return sharedPreferences.getBoolean(USING_DEMO_SERVER, USING_DEMO_SERVER_DEF)
    }

    fun applyUsingDemoServer(usingDemoServer: Boolean) {
        sharedPreferences.edit().putBoolean(USING_DEMO_SERVER, usingDemoServer).apply()
    }

    fun getDemoSavedUsername(): String {
        return sharedPreferences.getString(DEMO_SAVED_USERNAME, DEMO_SAVED_USERNAME_DEF)!!
    }

    fun applyDemoSavedUsername(username: String) {
        sharedPreferences.edit().putString(DEMO_SAVED_USERNAME, username).apply()
    }

    fun isFirstUse(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_USE, IS_FIRST_USE_DEF)
    }

    fun applyFirstUse() {
        sharedPreferences.edit().putBoolean(IS_FIRST_USE, false).apply()
    }

    fun getSavedUsername(): String {
        return sharedPreferences.getString(SAVED_USERNAME, SAVED_USERNAME_DEF)!!
    }

    fun applySavedUsername(username: String) {
        sharedPreferences.edit().putString(SAVED_USERNAME, username).apply()
    }
}