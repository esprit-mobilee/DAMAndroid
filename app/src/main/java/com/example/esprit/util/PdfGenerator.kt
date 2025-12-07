package com.example.esprit.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.esprit.model.DocumentRequestItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {
    
    // A4 page dimensions in points (1 inch = 72 points)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    
    // Margins
    private const val MARGIN_LEFT = 50f
    private const val MARGIN_RIGHT = 50f
    private const val MARGIN_TOP = 50f
    
    fun generateAttestationPdf(request: DocumentRequestItem, outputFile: File): Boolean {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yPosition = MARGIN_TOP
            
            // Draw header
            yPosition = drawHeader(canvas, yPosition)
            
            // Draw title
            yPosition = drawTitle(canvas, yPosition)
            
            // Draw body
            yPosition = drawBody(canvas, yPosition, request)
            
            // Draw footer
            drawFooter(canvas, request)
            
            pdfDocument.finishPage(page)
            
            // Write to file
            FileOutputStream(outputFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun drawHeader(canvas: Canvas, startY: Float): Float {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        // School name
        val schoolName = "▶ ÉCOLE SUPÉRIEURE PRIVÉE D'INGÉNIERIE ET DE TECHNOLOGIES"
        canvas.drawText(schoolName, MARGIN_LEFT, startY, paint)
        
        // ESPRIT logo text (simplified - in production you'd draw an actual logo)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val logoText = "esprit"
        val logoWidth = paint.measureText(logoText)
        canvas.drawText(logoText, PAGE_WIDTH - MARGIN_RIGHT - logoWidth, startY + 20, paint)
        
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subtitle = "Se former autrement"
        val subtitleWidth = paint.measureText(subtitle)
        canvas.drawText(subtitle, PAGE_WIDTH - MARGIN_RIGHT - subtitleWidth, startY + 35, paint)
        
        return startY + 80
    }
    
    private fun drawTitle(canvas: Canvas, startY: Float): Float {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        // Draw border rectangle
        val rectPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        val titleText = "ATTESTATION DE PRÉSENCE"
        val rectWidth = 400f
        val rectHeight = 50f
        val rectLeft = (PAGE_WIDTH - rectWidth) / 2
        val rectTop = startY
        
        canvas.drawRect(rectLeft, rectTop, rectLeft + rectWidth, rectTop + rectHeight, rectPaint)
        
        // Draw title text centered in rectangle
        val textY = rectTop + (rectHeight / 2) + (paint.textSize / 3)
        canvas.drawText(titleText, PAGE_WIDTH / 2f, textY, paint)
        
        return startY + rectHeight + 60
    }
    
    private fun drawBody(canvas: Canvas, startY: Float, request: DocumentRequestItem): Float {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        var yPos = startY
        
        // Introduction text
        val intro = "Je soussigné M.Mohamed Ali BOUAKLINE, Chef Département de la scolarité de l'Ecole Supérieure"
        canvas.drawText(intro, MARGIN_LEFT, yPos, paint)
        yPos += 20
        
        val intro2 = "Privée d'Ingénierie et de Technologies, atteste par la présente que Monsieur"
        canvas.drawText(intro2, MARGIN_LEFT, yPos, paint)
        yPos += 30
        
        // Student name (bold and larger)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val studentName = "${request.user?.firstName?.uppercase() ?: ""} ${request.user?.lastName ?: ""}"
        val nameWidth = paint.measureText(studentName)
        canvas.drawText(studentName, (PAGE_WIDTH - nameWidth) / 2, yPos, paint)
        yPos += 40
        
        // Reset paint
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        // Class and year information
        val classInfo = request.details?.get("classe")?.toString() ?: request.details?.get("class")?.toString()
        val yearInfo = request.annee
        
        val enrollmentText = "est inscrit dans notre institution en 1 ère année du cycle d'Ingénieur en Informatique"
        canvas.drawText(enrollmentText, MARGIN_LEFT, yPos, paint)
        yPos += 20
        
        val classText = if (classInfo != null) {
            "(classe : $classInfo) pour l'année universitaire $yearInfo sous le numéro : ${request.user?.studentId ?: ""} et qu'il suit"
        } else {
            "pour l'année universitaire $yearInfo sous le numéro : ${request.user?.studentId ?: ""} et qu'il suit"
        }
        canvas.drawText(classText, MARGIN_LEFT, yPos, paint)
        yPos += 20
        
        val regularText = "régulièrement les cours."
        canvas.drawText(regularText, MARGIN_LEFT, yPos, paint)
        yPos += 40
        
        // Purpose statement
        val purposeText = "Cette attestation est délivrée à l'intéressé(e) à sa demande pour servir et valoir ce que de droit."
        canvas.drawText(purposeText, MARGIN_LEFT, yPos, paint)
        yPos += 40
        
        return yPos
    }
    
    private fun drawFooter(canvas: Canvas, request: DocumentRequestItem) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        val footerY = PAGE_HEIGHT - 200f
        
        // Date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
        val currentDate = dateFormat.format(Date())
        val dateText = "Fait le $currentDate"
        val dateWidth = paint.measureText(dateText)
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN_RIGHT - dateWidth, footerY, paint)
        
        // Department head title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val titleText = "Chef Département de la scolarité"
        val titleWidth = paint.measureText(titleText)
        canvas.drawText(titleText, PAGE_WIDTH - MARGIN_RIGHT - titleWidth, footerY + 20, paint)
        
        // Name
        val nameText = "M.Mohamed Ali BOUAKLINE"
        val nameWidth = paint.measureText(nameText)
        canvas.drawText(nameText, PAGE_WIDTH - MARGIN_RIGHT - nameWidth, footerY + 40, paint)
        
        // Bottom info
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val bottomY = PAGE_HEIGHT - 80f
        canvas.drawText("Agrément du Ministère de l'enseignement supérieur sous le N° 03/2003", MARGIN_LEFT, bottomY, paint)
        canvas.drawText("www.esprit.tn - E-mail : contact@esprit.tn", MARGIN_LEFT, bottomY + 15, paint)
        
        val addressText = "Z.I Ariana Aéroport - 1053 - Pôle Technologique - El Ghazala"
        canvas.drawText(addressText, MARGIN_LEFT, bottomY + 30, paint)
        
        val contactText = "Tél: 71 947 641 - Fax: 71 941 889"
        canvas.drawText(contactText, MARGIN_LEFT, bottomY + 45, paint)
    }
}
