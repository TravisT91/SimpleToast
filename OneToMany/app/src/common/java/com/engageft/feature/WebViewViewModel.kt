//package com.engageft.feature
//
//import android.os.AsyncTask
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.Observer
//import io.reactivex.Observable
//import org.jsoup.Jsoup
//import java.io.IOException
//import java.net.MalformedURLException
//import java.net.URL
//
//class WebViewViewModel: BaseEngageViewModel() {
//    val loadDataWithBaseUrlObservable = MutableLiveData<String>()
//
//    fun printDownload(initialUrl: String) {
//        compositeDisposable.add(
//
//        )
//    }
//
//    private fun extractURL(initialUrl: String): Observable<String>? {
//        var result: Observable<String>
//        try {
//            val doc = Jsoup.connect(initialUrl).get()
//            doc.select("*.hidden-print").remove()
//            doc.select("*.hidden-embedded").remove()
//            // proxy.js does a redirect (to protocol://host:0) and the :0 port at the end causes the request to timeout.
//            // This is only a problem when using loadDataWithBaseUrl, it seems.
//            val scripts = doc.select("script")
//            for (element in scripts) {
//                if (element.toString().contains("proxy.js")) {
//                    element.remove()
//                }
//            }
//            return "test"
////            return Observable(doc.toString().replace("<h1", "<h3").replace("<h2", "<h3")) // h1 and h2 styles render huge on the screen
//        } catch (e: IOException) {
//            //TODO
//
////                loadFailure(getString(R.string.ERROR_GENERIC))
//            return null
//        }
//    }
//
//
//}