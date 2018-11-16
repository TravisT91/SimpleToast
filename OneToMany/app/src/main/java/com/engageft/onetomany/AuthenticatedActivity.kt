package com.engageft.onetomany


import android.app.Activity
import android.content.Intent
import android.util.Log
import com.engageft.apptoolbox.LotusActivity
import com.engageft.apptoolbox.LotusActivityConfig
import com.engageft.feature.WebViewFragment

class AuthenticatedActivity : LotusActivity() {
    private val lotusActivityConfig = object : LotusActivityConfig() {
        override val navigationMenuResourceId = R.menu.menu_navigation
        override val navigationGraphResourceId = R.navigation.navigation_authenticated
    }

    override fun getLotusActivityConfig(): LotusActivityConfig {
        return lotusActivityConfig
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Log.e("AuthenticatedActivity", "requestCode $requestCode")
//        if (resultCode == Activity.RESULT_OK) {
//
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }
}
