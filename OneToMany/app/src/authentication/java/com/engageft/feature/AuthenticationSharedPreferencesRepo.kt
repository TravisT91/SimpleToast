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
}