package com.engageft.fis.pscu.feature

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.MailTo
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.WebPrinter
import com.engageft.apptoolbox.util.MenuTint
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ScrollToBottomWebView
import com.engageft.fis.pscu.R
import io.reactivex.observers.DisposableObserver
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * WebViewFragment
 *
 * WebView baseFragmentIm that displays a webView, and shows webView in PDF format.
 *
 * Created by Kurt Mueller on 2/20/17.
 * Converted to Kotlin, imported partially by Atia Hashimi on 11/16/18.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
class WebViewFragment : BaseEngagePageFragment() {

    // Using custom WebView, even though only need its functionality when showing agreements.
    private lateinit var webView: ScrollToBottomWebView
    private var title: String? = null
    private var initialUrl: String? = null

    private var baseUrl: String? = null
    private var urlsToMerge: List<String>? = null
    private var titlesToMerge: List<String>? = null

    private var forPrint: Boolean = false
    private var showPdfImmediately: Boolean = false

    private var listener: OnWebViewFragmentListener? = null

    private val dialogInfoListener = object: InformationDialogFragment.InformationDialogFragmentListener {
        override fun onDialogFragmentNegativeButtonClicked() {}

        override fun onDialogFragmentPositiveButtonClicked() {
            findNavController().popBackStack()
        }

        override fun onDialogCancelled() {
            findNavController().popBackStack()
        }
    }

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            title = args.getString(ARG_TITLE)
            initialUrl = args.getString(ARG_INITIAL_URL)
            baseUrl = args.getString(ARG_BASE_URL)
            urlsToMerge = args.getStringArrayList(ARG_URLS_TO_MERGE)
            titlesToMerge = args.getStringArrayList(ARG_TITLES_TO_MERGE)
            forPrint = args.getBoolean(ARG_FOR_PRINT, false)
            showPdfImmediately = args.getBoolean(ARG_SHOW_PDF_IMMEDIATELY, false)
        }

        setHasOptionsMenu(forPrint && !showPdfImmediately)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        webView = view.findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true

        if (showPdfImmediately) {
            webView.visibility = View.INVISIBLE
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                removeHeadersAndFooter()
                removeLeftRightPadding()
                fragmentDelegate.dismissProgressOverlay()
                loadSuccess()
                if (forPrint && showPdfImmediately) {
                    exportPdfIntent()
                }
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                fragmentDelegate.dismissProgressOverlay()
                loadFailure(error?.description.toString())
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onReceivedError(view: WebView, errorCode: Int,
                                         description: String, failingUrl: String) {
                fragmentDelegate.dismissProgressOverlay()
                loadFailure(description)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return if (handleUrl(view, request.url.toString())) {
                    true
                } else {
                    super.shouldOverrideUrlLoading(view, request)
                }
            }
        }

        if (!initialUrl.isNullOrEmpty()) {
            if (!isNetworkAvailable()) {
                fragmentDelegate.showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!,
                        message = getString(R.string.alert_error_message_no_internet_connection),
                        listener = dialogInfoListener))
            } else {
                fragmentDelegate.showProgressOverlay()

                if (forPrint) {
                    Thread(Runnable {
                        val result = extractOutHeadersAndProxyJS()

                        activity?.let { currentActivity ->
                            if (!currentActivity.isFinishing) {
                                currentActivity.runOnUiThread {
                                    result?.let { url ->
                                        loadWebView(url)
                                    } ?: run {
                                        fragmentDelegate.showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!, listener = dialogInfoListener))
                                    }
                                }
                            }
                        }
                    }).start()
                } else {
                    webView.loadUrl(initialUrl)
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var connected = false
        if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected) {
            connected = true
        }
        return connected
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.general_options_menu_share, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val outTypedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.actionModeShareDrawable, outTypedValue, true)
        val drawable = ContextCompat.getDrawable(context!!, outTypedValue.resourceId)

        val item = menu.findItem(R.id.menu_item_share)
        item.icon = drawable
        MenuTint.colorIcons(activity, menu, ContextCompat.getColor(context!!, R.color.menuTint))
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_item_share) {
            exportPdfIntent()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleUrl(view: WebView, url: String): Boolean {
        var handled = false
        if (url.startsWith("mailto:")) {
            val mt = MailTo.parse(url)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(mt.to))

            val manager = activity!!.packageManager
            val infos = manager.queryIntentActivities(i, 0)
            if (infos.size > 0) {
                startActivity(i)
            } else {
                fragmentDelegate.showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!, message = getString(R.string.WEBVIEW_NO_EMAIL_CLIENT_FOUND)))
            }

            view.reload()
            handled = true
        }
        return handled
    }

    private fun removeHeadersAndFooter() {
        webView.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-header')[0].style.display = \"none\";"
                + "})()")
        webView.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-footer')[0].style.display = \"none\";"
                + "})()")
        webView.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('header')[0].style.display = \"none\";"
                + "})()")
    }

    private fun removeLeftRightPadding() {
        webView.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-raised')[0].style.margin = \"0\";"
                + "})()")
    }

    private fun loadSuccess() {
        if (listener != null) {
            listener!!.onWebViewLoadSuccess()
        }
    }

    private fun loadFailure(errorMessage: String) {
        if (listener != null) {
            listener!!.onWebViewLoadFailure(errorMessage)
        }
    }

    //TODO(aHashimi): Is there a way to intercept/override navigatioUp for custom handling? https://engageft.atlassian.net/browse/SHOW-441
//    fun goBack(): Boolean {
//        if (webView != null && webView!!.canGoBack()) {
//            webView!!.goBack()
//
//            return true
//        }
//        return false
//    }

    fun exportPdfIntent() {
        // if we are showing PDF immediately, don't append .PDF or underscores to the file and rename
        // underscores because the filename is used as the title of the screen when the pdf file is opened in activity
        if (!showPdfImmediately) {
            title = title!!.replace(" ", "_")
        }
        WebPrinter(context!!).createPdfPrint(webView, title!!, !showPdfImmediately, object : DisposableObserver<File>() {
            override fun onNext(file: File) {
                var fileUri: Uri? = null
                try {
                    fileUri = FileProvider.getUriForFile(
                            context!!,
                            context!!.applicationContext.packageName + ".file_provider",
                            file)
                } catch (e: Exception) {
                    engageFragmentDelegate.handleGenericThrowable(e)
                }

                if (fileUri != null) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    // Grant temporary read permission to the content URI
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(fileUri, "application/pdf")
                    intent.resolveActivity(context!!.packageManager)?.let {
                        startActivityForResult(intent, 0)
                    } ?: run {
                        openPlayStoreForPdfApps()
                    }
                }
            }

            override fun onError(e: Throwable) {
                engageFragmentDelegate.handleGenericThrowable(e)
                fragmentDelegate.showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!, listener = dialogInfoListener))
            }

            override fun onComplete() {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            if (requestCode == 0 && showPdfImmediately) {
                findNavController().popBackStack()
            }
        }
    }

    private fun openPlayStoreForPdfApps() {
        val listener = object: InformationDialogFragment.InformationDialogFragmentListener {
            override fun onDialogFragmentNegativeButtonClicked() {}

            override fun onDialogFragmentPositiveButtonClicked() {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pdf&c=apps")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pdf&c=apps")))
                }
            }

            override fun onDialogCancelled() {}
        }
        fragmentDelegate.showDialog(infoDialogYesNoNewInstance(context!!, title = getString(R.string.NO_PDF_APP_FOUND_TITLE),
                message = getString(R.string.NO_PDF_APP_FOUND_MESSAGE), listener = listener))
    }

    // TODO(aHashimi): can we change this to LiveData's?
    interface OnWebViewFragmentListener {
        fun onWebViewLoadSuccess()
        fun onWebViewLoadFailure(message: String)
    }

    companion object {

        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_INITIAL_URL = "ARG_INITIAL_URL"
        private const val ARG_BASE_URL = "ARG_BASE_URL"
        private const val ARG_URLS_TO_MERGE = "ARG_URLS_TO_MERGE"
        private const val ARG_TITLES_TO_MERGE = "ARG_TITLES_TO_MERGE"
        private const val ARG_FOR_PRINT = "ARG_FOR_PRINT"
        private const val ARG_SHOW_PDF_IMMEDIATELY = "ARG_SHOW_PDF_IMMEDIATELY"

        fun newInstance(title: String, initialUrl: String): WebViewFragment {
            return newInstance(title, initialUrl, false, false)
        }

        fun newInstance(title: String, initialUrl: String, forPrint: Boolean, showPdfImmediately: Boolean): WebViewFragment {
            val webViewFragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_INITIAL_URL, initialUrl)
            args.putBoolean(ARG_FOR_PRINT, forPrint)
            args.putBoolean(ARG_SHOW_PDF_IMMEDIATELY, showPdfImmediately)
            webViewFragment.arguments = args

            return webViewFragment
        }

        fun newInstance(title: String, baseUrl: String, urlsToMerge: ArrayList<String>, titlesToMerge: ArrayList<String>): WebViewFragment {
            val webViewFragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_BASE_URL, baseUrl)
            args.putStringArrayList(ARG_URLS_TO_MERGE, urlsToMerge)
            args.putStringArrayList(ARG_TITLES_TO_MERGE, titlesToMerge)
            webViewFragment.arguments = args

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

    private fun extractOutHeadersAndProxyJS() : String? {
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
            // h1 and h2 styles render huge on the screen
            return doc.toString().replace("<h1", "<h3").replace("<h2", "<h3")
        } catch (e: IOException) {
            engageFragmentDelegate.handleGenericThrowable(e)
            fragmentDelegate.showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!))
            return null
        }
    }

    private fun loadWebView(result: String) {
        try {
            val url = URL(initialUrl)
            val protocolAndHost = url.protocol + "://" + url.host
            webView.loadDataWithBaseURL(protocolAndHost, result, "text/html", "utf-8", null)
        } catch (e: MalformedURLException) {
            engageFragmentDelegate.handleGenericThrowable(e)
            fragmentDelegate.showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!))
        }
    }
}
