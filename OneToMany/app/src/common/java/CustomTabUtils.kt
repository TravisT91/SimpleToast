import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * ChromeCustomTabUtils
 * </p>
 * Utility class for using Chrome Custom Tabs
 * </p>
 * Created by Travis Tkachuk 1/25/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */

fun showChromeCustomTab(context: Context, activity : Activity, url: String){
    CustomTabsIntent.Builder().apply {
        setToolbarColor(Palette.primaryColor)
        setStartAnimations(context, R.anim.nav_enter_anim, R.anim.nav_exit_anim)
        setExitAnimations(context, R.anim.nav_pop_enter_anim, R.anim.nav_pop_exit_anim)
        setShowTitle(true)
    }.build().launchUrl(activity, Uri.parse(url))
}
