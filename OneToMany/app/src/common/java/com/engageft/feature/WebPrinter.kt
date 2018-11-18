package com.engageft.feature
import android.content.Context
import android.print.*
import android.webkit.WebView

import com.engageft.onetomany.R

import java.io.File
import java.util.ArrayList

import io.reactivex.observers.DisposableObserver

/**
 * WebPrinter
 *
 * Based on Optum WebPrinter
 *
 * Created by Kurt Mueller on 7/26/17.
 * Imported, converted to Kotlin by Atia Hashimi on 11/16/2018.
 * Copyright (c) 2017 Engage FT. All rights reserved.
 */
class WebPrinter(private val context: Context) {
    private val mPrintJobs = ArrayList<PrintJob>()

    private val printAttributes: PrintAttributes
        get() = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

    private fun createWebPrint(printAdapter: PrintDocumentAdapter) {
        // Get a PrintManager instance
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        // Create a print job with name and adapter instance
        val jobName = context.getString(R.string.app_name) + " Document"
        val printJob = printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())

        // Save the job object for later status checking
        mPrintJobs.add(printJob)
    }

    private fun createPdfPrint(printAdapter: PrintDocumentAdapter?, docName: String, fileObserver: DisposableObserver<File>) {
        val path = File(context.cacheDir.toString() + "/pdf/")
        val pdfPrint = PdfPrint(printAttributes)
        pdfPrint.print(printAdapter, path, "$docName.pdf", fileObserver)
    }

    fun createPdfPrint(webView: WebView, docName: String, fileObserver: DisposableObserver<File>) {
        // Get a print adapter instance
        var printAdapter: PrintDocumentAdapter? = null
        printAdapter = webView.createPrintDocumentAdapter(docName)
        createPdfPrint(printAdapter, docName, fileObserver)
    }
}
