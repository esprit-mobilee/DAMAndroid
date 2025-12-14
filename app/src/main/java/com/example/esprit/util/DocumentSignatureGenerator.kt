package com.example.esprit.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for generating document signatures and verification data
 */
object DocumentSignatureGenerator {
    
    private const val VERIFICATION_BASE_URL = "https://esprit-dam.onrender.com/api/verify/doc/"
    
    /**
     * Generate a unique document reference
     * Format: ESPRIT-DOC-YYYY-XXXXXX
     */
    fun generateDocumentReference(): String {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val randomPart = (100000..999999).random()
        return "ESPRIT-DOC-$year-$randomPart"
    }
    
    /**
     * Generate a verification hash for the document
     * This hash can be stored in the database and used to verify authenticity
     */
    fun generateVerificationHash(
        documentReference: String,
        studentId: String,
        approvalDate: Date,
        adminName: String
    ): String {
        val data = "$documentReference|$studentId|${approvalDate.time}|$adminName"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }
    
    /**
     * Generate verification URL for QR code
     */
    fun generateVerificationUrl(documentReference: String): String {
        return "$VERIFICATION_BASE_URL$documentReference"
    }
    
    /**
     * Generate QR code bitmap
     * @param content The content to encode in the QR code
     * @param size The size of the QR code bitmap (width and height)
     * @return Bitmap of the QR code or null if generation fails
     */
    fun generateQRCode(content: String, size: Int = 200): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Data class to hold signature information
     */
    data class SignatureData(
        val documentReference: String,
        val verificationHash: String,
        val verificationUrl: String,
        val approvalDate: Date,
        val adminName: String,
        val adminTitle: String = "Chef Département de la scolarité"
    )
    
    /**
     * Generate complete signature data for a document
     */
    fun generateSignatureData(
        studentId: String,
        adminName: String = "M.Mohamed Ali BOUAKLINE",
        existingReference: String? = null,
        existingHash: String? = null
    ): SignatureData {
        val approvalDate = Date()
        val documentReference = existingReference ?: generateDocumentReference()
        val verificationHash = existingHash ?: generateVerificationHash(
            documentReference,
            studentId,
            approvalDate,
            adminName
        )
        val verificationUrl = generateVerificationUrl(documentReference)
        
        return SignatureData(
            documentReference = documentReference,
            verificationHash = verificationHash,
            verificationUrl = verificationUrl,
            approvalDate = approvalDate,
            adminName = adminName
        )
    }
}
