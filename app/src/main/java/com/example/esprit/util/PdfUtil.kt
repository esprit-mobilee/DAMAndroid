package com.example.esprit.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class PdfUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun extractTextFromPdf(uri: Uri): String {
        var document: PDDocument? = null
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            return stripper.getText(document)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            try {
                document?.close()
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun extractTextFromPdf(file: java.io.File): String {
        var document: PDDocument? = null
        try {
            document = PDDocument.load(file)
            val stripper = PDFTextStripper()
            return stripper.getText(document)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            try {
                document?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
