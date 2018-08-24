package com.engageft.showcase

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.engageft.apptoolbox.LotusActivity

class ShowcaseActivity : LotusActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun instantiateNavHostFragment(): NavHostFragment {
        return NavHostFragment.create(R.navigation.nav_graph)
    }
}