
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.print.*
import android.webkit.WebView

import com.engageft.onetomany.R

import java.io.File
import java.util.ArrayList

import io.reactivex.observers.DisposableObserver

/**
 * TODO: CLASS NAME
 *
 * Based on Optum WebPrinter
 *
 * Created by Kurt Mueller on 7/26/17.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
class WebPrinter(private val context: Context) {
    private val mPrintJobs = ArrayList<PrintJob>()

    private val printAttributes: PrintAttributes
        @TargetApi(19)
        get() = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

    @TargetApi(19)
    private fun createWebPrint(printAdapter: PrintDocumentAdapter) {
        // Get a PrintManager instance
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        // Create a print job with name and adapter instance
        val jobName = context.getString(R.string.app_name) + " Document"
        val printJob = printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())

        // Save the job object for later status checking
        mPrintJobs.add(printJob)
    }

    @TargetApi(16)
    private fun createLegacyWebPrint(url: String, docTitle: String) {
        // TODO: implement this without Optum PrintDialogActivity?
        //        final Intent printIntent = new Intent(context, PrintDialogActivity.class);
        //        printIntent.putExtra("url", url);
        //        printIntent.putExtra("title", docTitle);
        //        context.startActivity(printIntent);
    }

    //    @TargetApi(19)
    //    private void createPdfPrint(PrintDocumentAdapter printAdapter, @NonNull String docName, PdfPrint.Listener listener) {
    //        final File path = new File(context.getCacheDir() + "/pdf/");
    //        final PdfPrint pdfPrint = new PdfPrint(getPrintAttributes());
    ////        pdfPrint.print(printAdapter, path, "output_" + System.currentTimeMillis() + ".pdf");
    //        pdfPrint.print(printAdapter, path, docName + ".pdf", listener);
    //    }

    @TargetApi(19)
    private fun createPdfPrint(printAdapter: PrintDocumentAdapter?, docName: String, fileObserver: DisposableObserver<File>) {
        val path = File(context.cacheDir.toString() + "/pdf/")
        val pdfPrint = PdfPrint(printAttributes)
        pdfPrint.print(printAdapter, path, "$docName.pdf", fileObserver)
    }

    //    public void createWebPrint(@NonNull WebView webView, @NonNull String docName) {
    //        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //            // Get a print adapter instance
    //            final PrintDocumentAdapter printAdapter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
    //                    webView.createPrintDocumentAdapter(docName) : webView.createPrintDocumentAdapter();
    //            createWebPrint(printAdapter);
    //        } else {
    //            createLegacyWebPrint(webView.getUrl(), docName);
    //        }
    //    }

    //    public void createPdfPrint(@NonNull WebView webView, @NonNull String docName, PdfPrint.Listener listener) {
    //        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //            // Get a print adapter instance
    //            final PrintDocumentAdapter printAdapter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
    //                    webView.createPrintDocumentAdapter(docName) : webView.createPrintDocumentAdapter();
    //            createPdfPrint(printAdapter, docName, listener);
    //        } else {
    //            createLegacyWebPrint(webView.getUrl(), docName);
    //        }
    //    }


    fun createPdfPrint(webView: WebView, docName: String, fileObserver: DisposableObserver<File>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Get a print adapter instance
            var printAdapter: PrintDocumentAdapter? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                printAdapter = webView.createPrintDocumentAdapter(docName)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                printAdapter = webView.createPrintDocumentAdapter(docName)
            }
            createPdfPrint(printAdapter, docName, fileObserver)
        } else {
            createLegacyWebPrint(webView.url, docName)
        }
    }
}
