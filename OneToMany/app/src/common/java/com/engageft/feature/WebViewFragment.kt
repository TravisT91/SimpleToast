package com.engageft.feature

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.apptoolbox.ViewUtils.newLotusInstance
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
//    private var webView: AgreementsWebView? = null
    private lateinit var webView: ScrollToBottomWebView
//    private lateinit var webViewViewModel: WebViewViewModel
    private var title: String? = null
    private var initialUrl: String? = null

    private var baseUrl: String? = null
    private var urlsToMerge: List<String>? = null
    private var titlesToMerge: List<String>? = null

    private var forPrint: Boolean = false
    private var showPdfImmediately: Boolean = false

    private var listener: OnWebViewFragmentListener? = null

    override fun createViewModel(): BaseViewModel? {
//        webViewViewModel = ViewModelProviders.of(this).get(WebViewViewModel::class.java)
        return null
    }

    private var onBottomReachedListener: ScrollToBottomWebView.OnBottomReachedListener? = null
//    private var minDistance: Int = 0

//    fun setOnBottomReachedListener(listener: AgreementsWebView.OnBottomReachedListener, minDistance: Int) {
//        onBottomReachedListener = listener
//        this.minDistance = minDistance
//        if (webView != null) {
//            webView!!.setOnBottomReachedListener(listener, minDistance)
//        }
//    }
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
        // show progress until network is checked and url is loaded
        showProgressOverlay()

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

        return view
    }

    override fun onResume() {
        super.onResume()
        // todo does it need to be done here?
        registerNetworkCallback()
    }

    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            Log.e(TAG, "network = onAvailable")
            loadUrl()
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Log.e(TAG, "network = onUnavailable")
        }
        // todo: check lost/losing etc with nexus 5 phone
    }

    private fun registerNetworkCallback() {
        val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build(), networkCallback)
    }

    private fun loadUrl() {
        if (forPrint) {
            Thread(Runnable {
                val result = extractOut()
                (context as? Activity)?.runOnUiThread { result?.let { updateWebView(it) } }
            }).start()
        } else {
            webView.loadUrl(initialUrl)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(networkCallback)
    }

    //
//        return view
//    }

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
//                showErrorDialog(getString(R.string.WEBVIEW_NO_EMAIL_CLIENT_FOUND))
            }

            view.reload()
            handled = true
        }
        return handled
    }



//
//    fun onAttach(context: Context) {
//        super.onAttach(context)
//
//        try {
//            listener = context as OnWebViewFragmentListener
//        } catch (e: ClassCastException) {
//            // okay if no listener here. Will use EventBus instead. See loadSuccess() and loadFailure(String errorMessage).
//            // Because this fragment is used both in an Activity fragment container and in a fragment's child fragment container,
//            // support both listener and EventBus. Otherwise for child fragment, activity would have to implement listener
//            // interface and then find the parent fragment and send messages to it. EventBus is cleaner in this case.
//        }
//    }

//    fun onResume() {
//        super.onResume()
//
//        if (!TextUtils.isEmpty(title)) {
//            setTitle(title)
//        } else {
//            clearTitleAndSubtitle()
//        }
//    }

    fun goBack(): Boolean {
        if (webView != null && webView!!.canGoBack()) {
            webView!!.goBack()

            return true
        }

        return false
    }

//    private fun htmlObservable(url: String): Observable<String> {
//        return Observable.defer {
//            try {
//                return@Observable.defer Observable . just getHtml(url)
//            } catch (e: IOException) {
//                return@Observable.defer Observable . error e
//            }
//        }
//    }

//    @Throws(IOException::class)
//    private fun getHtml(urlString: String): String {
//        val url = URL(urlString)
//        val connection = url.openConnection() as HttpURLConnection
//        connection.requestMethod = "GET"
//        val responseCode = connection.responseCode
//        if (responseCode != 200) {
//            throw IOException("Non-200 response code getting: $urlString")
//        } else {
//            val output = StringBuilder()
//            val br = BufferedReader(InputStreamReader(connection.inputStream))
//            var line: String
//            while ((line = br.readLine()) != null) {
//                output.append(line)
//            }
//            br.close()
//            return output.toString()
//        }
//    }

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
        Log.e(TAG, "loadSuccess")
        if (listener != null) {
            listener!!.onWebViewLoadSuccess()
        }

    }

    private fun loadFailure(errorMessage: String) {
        Log.e(TAG, "errorMessage $errorMessage")
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
//                handleThrowable(e)
                e.printStackTrace()
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
            return newInstance(title, initialUrl, false, false)
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

    //todo what happens when we don't this?
    private fun extractOut() : String? {
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
            //TODO

//                loadFailure(getString(R.string.ERROR_GENERIC))
            return null
        }
    }

    private fun updateWebView(result: String) {
        try {
            val url = URL(initialUrl)
            val protocolAndHost = url.protocol + "://" + url.host
            webView.loadDataWithBaseURL(protocolAndHost, result, "text/html", "utf-8", null)
        } catch (e: MalformedURLException) {
//                loadFailure(getString(R.string.ERROR_GENERIC))
        }
    }
}
