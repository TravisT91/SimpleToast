package com.engageft.feature

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.MenuInflater
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController

import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.apptoolbox.view.ScrollToBottomWebView
import com.engageft.onetomany.R
import io.reactivex.observers.DisposableObserver

import org.jsoup.Jsoup

import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList


/**
 * TODO: CLASS NAME
 *
 * TODO: CLASS DESCRIPTION
 *
 * Created by Kurt Mueller on 2/20/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
class WebViewFragment : LotusFullScreenFragment() {

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
        // todo set color is possible thru Paris
//        webView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.themeBackground))

        webView.settings.javaScriptEnabled = true

        if (showPdfImmediately) {
            webView.visibility = View.GONE
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                removeHeadersAndFooter()
                removeLeftRightPadding()
                dismissProgressOverlay()
                loadSuccess()
                if (forPrint && showPdfImmediately) {
                    exportPdfIntent()
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                dismissProgressOverlay()
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
        }

        if (!initialUrl.isNullOrEmpty()) {
            if (!isNetworkAvailable()) {
                val listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                    override fun onDialogFragmentNegativeButtonClicked() {}

                    override fun onDialogFragmentPositiveButtonClicked() {
                        findNavController().popBackStack()
                    }

                    override fun onDialogCancelled() {
                        findNavController().popBackStack()
                    }

                }
                showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!,
                        message = getString(R.string.alert_error_message_no_internet_connection),
                        listener = listener))
            } else {
                showProgressOverlay()

                if (forPrint) {
                    Thread(Runnable {
                        val result = extractOutHeadersAndProxyJS()

                        activity?.let { currentActivity ->
                            if (!currentActivity.isFinishing) {
                                currentActivity.runOnUiThread {
                                    result?.let { url ->
                                        loadWebView(url)
                                    } ?: run {
                                        showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!))
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

        return view
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
        //TODO: color paris?
//        MenuTint.colorIcons(getActivity(), menu, ContextCompat.getColor(getContext(), R.color.themeNavigationTint))
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
                showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!, message = getString(R.string.WEBVIEW_NO_EMAIL_CLIENT_FOUND)))
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

    fun exportPdfIntent() {
        WebPrinter(context!!).createPdfPrint(webView, title!!.replace(" ", "_"), object : DisposableObserver<File>() {
            override fun onNext(file: File) {
                var fileUri: Uri? = null
                try {
                    fileUri = FileProvider.getUriForFile(
                            context!!,
                            context!!.applicationContext.packageName + ".file_provider",
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
                    // if (intent.resolveActivity(activity!!.packageManager) != null) {
                    intent.resolveActivity(context!!.packageManager)?.let {
                        startActivity(intent)
//                        startActivityForResult(intent, 100)
                    } ?: run {
                        openPlayStoreForPdfApps()
                    }
                }
            }

            override fun onError(e: Throwable) {
                handleGenericThrowable(e)
                showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!))
            }

            override fun onComplete() {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.e(TAG, "requestCodpaye $requestCode")
        if (requestCode == 0 && showPdfImmediately) {
            findNavController().popBackStack()
        }
//        super.onActivityResult(requestCode, resultCode, data)
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
        showDialog(infoDialogYesNoNewInstance(context!!, title = getString(R.string.NO_PDF_APP_FOUND_TITLE),
                message = getString(R.string.NO_PDF_APP_FOUND_MESSAGE), listener = listener))
    }

    // TODO(aHashimi): can we change this to LiveData's?
    interface OnWebViewFragmentListener {
        fun onWebViewLoadSuccess()
        fun onWebViewLoadFailure(message: String)
    }

    companion object {

        val TAG = "WebViewFragment"

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
            handleGenericThrowable(e)
            showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!))
            return null
        }
    }

    private fun loadWebView(result: String) {
        try {
            val url = URL(initialUrl)
            val protocolAndHost = url.protocol + "://" + url.host
            webView.loadDataWithBaseURL(protocolAndHost, result, "text/html", "utf-8", null)
        } catch (e: MalformedURLException) {
            handleGenericThrowable(e)
            showDialog(infoDialogGenericErrorTitleMessageNewInstance(context!!))
        }
    }
}
