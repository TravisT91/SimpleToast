# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class com.heapanalytics.android.internal.HeapInternal {
    public static void captureClick(...);
}
-keep public class com.heapanalytics.android.internal.HeapImpl {
    private static void unsafeInit(...);
    private static void markInstrumentorRan(...);
}
-keep public class **.BuildConfig {
    *;
}
-keepnames public class android.support.design.widget.TabLayout** {
    *;
}

# MoEngage
-dontwarn com.google.android.gms.location.**
-dontwarn com.google.android.gms.gcm.**
-dontwarn com.google.android.gms.iid.**

-keep class com.google.android.gms.gcm.** { *; }
-keep class com.google.android.gms.iid.** { *; }
-keep class com.google.android.gms.location.** { *; }

# not in MoEngage developer docs, but won't build release without this.
-dontwarn com.google.firebase.**

# only when using FCM
-keep class com.moengage.firebase.MoEngaeFireBaseMessagingService
-keep class com.moengage.firebase.MoEngageFireBaseInstanceIdService
-dontwarn com.moengage.firebase.MoEngaeFireBaseMessagingService
-dontwarn com.moengage.firebase.MoEngageFireBaseInstanceIdService

-keep class com.moe.pushlibrary.activities.** { *; }
-keep class com.moe.pushlibrary.MoEHelper
-keep class com.moengage.locationlibrary.GeofenceIntentService
-keep class com.moe.pushlibrary.InstallReceiver
-keep class com.moe.pushlibrary.providers.MoEProvider
-keep class com.moe.pushlibrary.models.** { *;}
-keep class com.moengage.core.GeoTask
-keep class com.moengage.location.GeoManager
-keep class com.moengage.inapp.InAppManager
-keep class com.moengage.push.PushManager
-keep class com.moengage.inapp.InAppController
-keep class com.moe.pushlibrary.AppUpdateReceiver
-keep class com.moengage.core.MoEAlarmReceiver
-keep class com.moengage.core.MoEngage

# Push
-keep class com.moengage.pushbase.activities.PushTracker
-keep class com.moengage.pushbase.activities.SnoozeTracker
-keep class com.moengage.pushbase.push.MoEPushWorker
-keep class com.moe.pushlibrary.MoEWorker

# Device triggers
-keep class com.moengage.addon.trigger.DTHandlerImpl
-keep class com.moengage.core.MoEDTManager
-keep class com.moengage.core.MoEDTManager.DTHandler
# Push Amplification
-keep class com.moengage.addon.messaging.MessagingHandlerImpl
-keep class com.moengage.push.MoEMessagingManager

-dontwarn com.moengage.location.GeoManager
-dontwarn com.moengage.core.GeoTask
-dontwarn com.moengage.receiver.*
-dontwarn com.moengage.worker.*
-dontwarn com.moengage.inapp.ViewEngine

-keep class com.delight.**  { *; }
