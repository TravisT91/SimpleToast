package com.engageft.apptoolbox.view

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.MailTo
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import com.engageft.app.R
import com.engageft.app.feature.base.BaseActivity
import com.engageft.app.feature.base.BaseFragment
import com.engageft.app.feature.base.BaseToolbarActivity
import com.engageft.app.feature.dialog.NoYesDialogFragment
import com.engageft.app.feature.webview.util.WebPrinter
import com.engageft.app.util.ColorUtils
import com.engageft.app.util.MenuTint
import com.engageft.app.view.AgreementsWebView
import com.engageft.apptoolbox.BaseFragment
import com.engageft.engagekit.rest.exception.NoConnectivityException

import org.greenrobot.eventbus.EventBus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.UnknownHostException
import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

/**
 * TODO: CLASS NAME
 *
 * TODO: CLASS DESCRIPTION
 *
 * Created by Kurt Mueller on 2/20/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
class WebViewFragment : BaseFragment() {

    // Using custom WebView, even though only need its functionality when showing agreements.
//    private var webView: AgreementsWebView? = null

    private var title: String? = null
    private var initialUrl: String? = null

    private var baseUrl: String? = null
    private var urlsToMerge: List<String>? = null
    private var titlesToMerge: List<String>? = null

    private var forPrint: Boolean = false
    private var showPdfImmediately: Boolean = false

    private var listener: OnWebViewFragmentListener? = null

//    private var onBottomReachedListener: AgreementsWebView.OnBottomReachedListener? = null
    private var minDistance: Int = 0

//    fun setOnBottomReachedListener(listener: AgreementsWebView.OnBottomReachedListener, minDistance: Int) {
//        onBottomReachedListener = listener
//        this.minDistance = minDistance
//        if (webView != null) {
//            webView!!.setOnBottomReachedListener(listener, minDistance)
//        }
//    }

    fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE)
            initialUrl = getArguments().getString(ARG_INITIAL_URL)
            baseUrl = getArguments().getString(ARG_BASE_URL)
            urlsToMerge = getArguments().getStringArrayList(ARG_URLS_TO_MERGE)
            titlesToMerge = getArguments().getStringArrayList(ARG_TITLES_TO_MERGE)
            forPrint = getArguments().getBoolean(ARG_FOR_PRINT, false)
            showPdfImmediately = getArguments().getBoolean(ARG_SHOW_PDF_IMMEDIATELY, false)
        }

        setHasOptionsMenu(forPrint && !showPdfImmediately)
    }

    fun onCreateView(inflater: LayoutInflater,container: ViewGroup, savedInstanceState: Bundle): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.web_view_fragment, container, false)
        webView = view.findViewById(R.id.web_view)
        webView!!.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.themeBackground))

        webView!!.getSettings().setJavaScriptEnabled(true)

        if (showPdfImmediately) {
            webView!!.setVisibility(View.GONE)
        }

//        if (onBottomReachedListener != null) {
//            webView!!.setOnBottomReachedListener(onBottomReachedListener, minDistance)
//        }

        // This approach will work fine for simple pages, such as showing static terms. May not be accurate
        // for iFrames, etc.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    removeHeadersAndFooter()
                    removeLeftRightPadding()
                    hideProgressOverlay()
                    loadSuccess()
                    if (forPrint && showPdfImmediately) {
                        exportPdfIntent()
                    }
                }

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    hideProgressOverlay()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        loadFailure(error.description.toString())
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return if (handleUrl(view, request.url.toString())) {
                        true
                    } else {
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }

                /**
                 * This is the deprecated override that some old phones still seem to use. Overriding
                 * to maintain compatibility.
                 */
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return if (handleUrl(view, url)) {
                        true
                    } else {
                        super.shouldOverrideUrlLoading(view, url)
                    }
                }
            })
        } else {
            webView!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    removeHeadersAndFooter()
                    removeLeftRightPadding()
                    hideProgressOverlay()
                    loadSuccess()
                    if (forPrint && showPdfImmediately) {
                        exportPdfIntent()
                    }
                }

                override fun onReceivedError(view: WebView, errorCode: Int,
                                             description: String, failingUrl: String) {
                    hideProgressOverlay()
                    loadFailure(description)
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return if (handleUrl(view, request.url.toString())) {
                        true
                    } else {
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }

                /**
                 * This is the deprecated override that some old phones still seem to use. Overriding
                 * to maintain compatibility.
                 */
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return if (handleUrl(view, url)) {
                        true
                    } else {
                        super.shouldOverrideUrlLoading(view, url)
                    }
                }
            })
        }

        if (!TextUtils.isEmpty(initialUrl)) {
            if (getActivity() is BaseActivity && !(getActivity() as BaseActivity).isNetworkAvailable()) {
                loadFailure(getString(R.string.ALERT_NO_INTERNET))
            } else {
                showProgressOverlay()
                if (forPrint) {
                    PrintDownloadTask().execute()
                } else {
                    webView!!.loadUrl(initialUrl)
                }
            }
        } else if (urlsToMerge != null && !urlsToMerge!!.isEmpty()) {
            if (getActivity() is BaseActivity && !(getActivity() as BaseActivity).isNetworkAvailable()) {
                loadFailure(getString(R.string.ALERT_NO_INTERNET))
            } else {
                // fetch and merge HTML from multiple URLs
                showProgressOverlay()
                val observables = ArrayList<Observable<String>>()
                for (url in urlsToMerge!!) {
                    observables.add(htmlObservable(url))
                }
                val mergeObservable = Observable.zip(observables, { objects ->
                    var mergeHtml: String? = null
                    var linkHtml: String? = null
                    var idTag: String

                    // load custom css for combined terms from file assets/terms.css (should be created for every app flavor, but will still work without it)
                    var termsCssFormat = ""
                    var termsCss: String? = null
                    var inputStream: InputStream? = null
                    try {
                        inputStream = getActivity().getAssets().open("terms.css.format")
                        val buffer = ByteArray(inputStream!!.available())
                        inputStream!!.read(buffer)
                        termsCssFormat = String(buffer)
                        val headerBackgroundColor = ColorUtils.getRGBStringForWebViewCss(ContextCompat.getColor(getContext(), R.color.themeBackground))
                        val bodyTextColor = ColorUtils.getRGBStringForWebViewCss(ContextCompat.getColor(getContext(), R.color.themeDefaultTextDark))
                        val bodyBackground = ColorUtils.getRGBStringForWebViewCss(ContextCompat.getColor(getContext(), R.color.white))
                        val linkColor = ColorUtils.getRGBStringForWebViewCss(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        termsCss = String.format(termsCssFormat, headerBackgroundColor, bodyTextColor, bodyBackground, linkColor)
                    } catch (e: Exception) {
                        Log.d(TAG, "error $e")
                    } finally {
                        inputStream?.close()
                    }

                    for (i in 0 until objects.length) {
                        idTag = String.format("tag%d", i)
                        val htmlString = objects[i]
                        // get just <body> contents, to combine inside new document
                        val doc = Jsoup.parse(htmlString as String)
                        val bodyString = doc.body().html()
                        if (mergeHtml != null) {
                            mergeHtml = String.format("%s <hr/><div id='%s'style='text-align:center'><a href='#top'>%s</a></div> %s", mergeHtml, idTag, getString(R.string.WEBVIEW_LINK_RETURN_TO_TOP_OF_PAGE), bodyString)
                            linkHtml = if (objects.length > 1) String.format("%s | <a href='#%s'>%s</a>", linkHtml, idTag, titlesToMerge!![i]) else ""
                        } else {
                            mergeHtml = String.format("<div id='%s'/> %s", idTag, bodyString)
                            linkHtml = if (objects.length > 1) String.format("<a href='#%s'>%s</a>", idTag, titlesToMerge!![i]) else ""
                        }
                    }

                    val fullHtml = String.format("<!DOCTYPE html><html><head><meta charset='utf-8'><style type='text/css'>%s</style><title>Combined Pages</title></head><body><div id='top' class='header-links'>%s</div><div class='content'> %s</div></body></html>", termsCss, linkHtml, mergeHtml)

                    fullHtml
                })

                compositeDisposable.add(
                        mergeObservable
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        { fullHtml ->
                                            hideProgressOverlay()
                                            if (!TextUtils.isEmpty(baseUrl)) {
                                                webView!!.loadDataWithBaseURL(baseUrl, fullHtml, null, null, null)
                                            } else {
                                                webView!!.loadData(fullHtml, null, null)
                                            }
                                        }, { e ->
                                    hideProgressOverlay()
                                    loadFailure(if (e is NoConnectivityException || e is UnknownHostException)
                                        getString(R.string.ALERT_NO_INTERNET)
                                    else
                                        e.getMessage())
                                }
                                )
                )
            }
        }

        return view
    }

    fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.general_options_menu_share, menu)
    }

    fun onPrepareOptionsMenu(menu: Menu) {
        val outTypedValue = TypedValue()
        getContext().getTheme().resolveAttribute(R.attr.actionModeShareDrawable, outTypedValue, true)
        val drawable = ContextCompat.getDrawable(getContext(), outTypedValue.resourceId)

        val item = menu.findItem(R.id.menu_item_share)
        item.setIcon(drawable)

        MenuTint.colorIcons(getActivity(), menu, ContextCompat.getColor(getContext(), R.color.themeNavigationTint))
        super.onPrepareOptionsMenu(menu)
    }


    fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId


        if (id == R.id.menu_item_share) {
            exportPdfIntent()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun scrollToBottom() {
        webView!!.scrollToBottom()
    }

    private fun handleUrl(view: WebView, url: String): Boolean {
        var handled = false
        if (url.startsWith("mailto:")) {
            val mt = MailTo.parse(url)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(mt.to))

            val manager = getActivity().getPackageManager()
            val infos = manager.queryIntentActivities(i, 0)
            if (infos.size > 0) {
                startActivity(i)
            } else {
                showErrorDialog(getString(R.string.WEBVIEW_NO_EMAIL_CLIENT_FOUND))
            }

            view.reload()
            handled = true
        }
        return handled
    }

    internal inner class PrintDownloadTask : AsyncTask<Void, String, String>() {

        override fun doInBackground(vararg params: Void): String? {
            try {
                val doc = Jsoup.connect(initialUrl).get()
                doc.select("*.hidden-print").remove()
                doc.select("*.hidden-embedded").remove()
                // proxy.js does a redirect (to protocol://host:0) and the :0 port at the end causes the request to timeout.
                // This is only a problem when using loadDataWithBaseUrl, it seems.
                val scripts = doc.select("script")
                for (element in scripts) {
                    if (element.toString().contains("proxy.js")) {
                        element.remove()
                    }
                }
                return doc.toString().replace("<h1", "<h3").replace("<h2", "<h3") // h1 and h2 styles render huge on the screen
            } catch (e: IOException) {
                loadFailure(getString(R.string.ERROR_GENERIC))
                return null
            }

        }

        override fun onPostExecute(result: String) {
            try {
                val url = URL(initialUrl)
                val protocolAndHost = url.protocol + "://" + url.host
                webView!!.loadDataWithBaseURL(protocolAndHost, result, "text/html", "utf-8", null)
            } catch (e: MalformedURLException) {
                loadFailure(getString(R.string.ERROR_GENERIC))
            }

        }
    }

    fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as OnWebViewFragmentListener
        } catch (e: ClassCastException) {
            // okay if no listener here. Will use EventBus instead. See loadSuccess() and loadFailure(String errorMessage).
            // Because this fragment is used both in an Activity fragment container and in a fragment's child fragment container,
            // support both listener and EventBus. Otherwise for child fragment, activity would have to implement listener
            // interface and then find the parent fragment and send messages to it. EventBus is cleaner in this case.
        }

    }

    fun onResume() {
        super.onResume()

        if (!TextUtils.isEmpty(title)) {
            setTitle(title)
        } else {
            clearTitleAndSubtitle()
        }
    }

    fun goBack(): Boolean {
        if (webView != null && webView!!.canGoBack()) {
            webView!!.goBack()

            return true
        }

        return false
    }

    private fun htmlObservable(url: String): Observable<String> {
        return Observable.defer({
            try {
                return@Observable.defer Observable . just getHtml(url)
            } catch (e: IOException) {
                return@Observable.defer Observable . error e
            }
        })
    }

    @Throws(IOException::class)
    private fun getHtml(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            throw IOException("Non-200 response code getting: $urlString")
        } else {
            val output = StringBuilder()
            val br = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String
            while ((line = br.readLine()) != null) {
                output.append(line)
            }
            br.close()
            return output.toString()
        }
    }

    private fun removeHeadersAndFooter() {
        webView!!.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-header')[0].style.display = \"none\";"
                + "})()")
        webView!!.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-footer')[0].style.display = \"none\";"
                + "})()")
        webView!!.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('header')[0].style.display = \"none\";"
                + "})()")
    }

    private fun removeLeftRightPadding() {
        webView!!.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-raised')[0].style.margin = \"0\";"
                + "})()")
    }

    private fun loadSuccess() {
        if (listener != null) {
            listener!!.onWebViewLoadSuccess()
        }

        EventBus.getDefault().post(WebViewLoadEvent())
    }

    private fun loadFailure(errorMessage: String) {
        if (listener != null) {
            listener!!.onWebViewLoadFailure(errorMessage)
        }

        EventBus.getDefault().post(WebViewLoadEvent(errorMessage))
    }

    fun exportPdfIntent() {
        WebPrinter(getContext()).createPdfPrint(webView, title!!.replace(" ", "_"), object : DisposableObserver<File>() {
            fun onNext(file: File) {
                var fileUri: Uri? = null
                try {
                    fileUri = FileProvider.getUriForFile(
                            getContext(),
                            getContext().getApplicationContext().getPackageName() + ".file_provider",
                            file)
                    Log.d(TAG, "onImageClick() filePath: " + fileUri!!.path!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (fileUri != null) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    // Grant temporary read permission to the content URI
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(fileUri, "application/pdf")
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(intent, 0)
                    } else {
                        openPlayStoreForPdfApps()
                    }
                }
            }

            fun onError(e: Throwable) {
                handleThrowable(e)
            }

            fun onComplete() {}
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && showPdfImmediately) {
            (getActivity() as BaseToolbarActivity).popFragment()
        }
    }

    private fun openPlayStoreForPdfApps() {
        val openStoreDialogFragment = NoYesDialogFragment.newInstance(getContext(),
                getString(R.string.NO_PDF_APP_FOUND_TITLE),
                getString(R.string.NO_PDF_APP_FOUND_MESSAGE),
                OPEN_PLAY_STORE_DIALOG_TAG)
        openStoreDialogFragment.show(getChildFragmentManager(), OPEN_PLAY_STORE_DIALOG_TAG)
    }

    fun onDialogFragmentPositiveButtonClicked(dialog: Dialog, tag: String) {
        if (OPEN_PLAY_STORE_DIALOG_TAG == tag) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pdf&c=apps")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pdf&c=apps")))
            }

        } else {
            super.onDialogFragmentPositiveButtonClicked(dialog, tag)
        }
    }

    interface OnWebViewFragmentListener {
        fun onWebViewLoadSuccess()
        fun onWebViewLoadFailure(message: String)
    }

    companion object {

        val TAG = "WebViewFragment"

        private val OPEN_PLAY_STORE_DIALOG_TAG = "OPEN_PLAY_STORE_DIALOG_TAG"

        protected val ARG_TITLE = "ARG_TITLE"
        protected val ARG_INITIAL_URL = "ARG_INITIAL_URL"
        private val ARG_BASE_URL = "ARG_BASE_URL"
        private val ARG_URLS_TO_MERGE = "ARG_URLS_TO_MERGE"
        private val ARG_TITLES_TO_MERGE = "ARG_TITLES_TO_MERGE"
        protected val ARG_FOR_PRINT = "ARG_FOR_PRINT"
        private val ARG_SHOW_PDF_IMMEDIATELY = "ARG_SHOW_PDF_IMMEDIATELY"

        fun newInstance(title: String, initialUrl: String): WebViewFragment {
            return WebViewFragment.newInstance(title, initialUrl, false, false)
        }

        fun newInstance(title: String, initialUrl: String, forPrint: Boolean, showPdfImmediately: Boolean): WebViewFragment {
            val webViewFragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_INITIAL_URL, initialUrl)
            args.putBoolean(ARG_FOR_PRINT, forPrint)
            args.putBoolean(ARG_SHOW_PDF_IMMEDIATELY, showPdfImmediately)
            webViewFragment.setArguments(args)

            return webViewFragment
        }

        fun newInstance(title: String, baseUrl: String, urlsToMerge: ArrayList<String>, titlesToMerge: ArrayList<String>): WebViewFragment {
            val webViewFragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_BASE_URL, baseUrl)
            args.putStringArrayList(ARG_URLS_TO_MERGE, urlsToMerge)
            args.putStringArrayList(ARG_TITLES_TO_MERGE, titlesToMerge)
            webViewFragment.setArguments(args)

            return webViewFragment
        }

        fun getBundle(title: String, initialUrl: String, forPrint: Boolean, showPdfImmediately: Boolean): Bundle {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_INITIAL_URL, initialUrl)
            args.putBoolean(ARG_FOR_PRINT, forPrint)
            args.putBoolean(ARG_SHOW_PDF_IMMEDIATELY, showPdfImmediately)
            return args
        }
    }
}
