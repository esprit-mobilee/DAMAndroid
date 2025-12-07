package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class DocumentField(
    val name: String,
    val type: String,
    val label: String,
    val required: Boolean
)

data class DocumentFormFieldsResponse(
    val fields: List<DocumentField> = emptyList()
)

data class DocumentRequestUser(
    @SerializedName("_id")
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val studentId: String? = null,
    val inscriptionPaid: Boolean = false // Statut de paiement des frais d'inscription
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}

data class DocumentRequestItem(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val annee: String,
    @SerializedName("userId")
    val userIdRaw: Any? = null, // Peut être String ou Object
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val details: Map<String, Any>? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String? = null
) {
    // Propriété calculée pour obtenir l'objet User
    val user: DocumentRequestUser?
        get() = when (userIdRaw) {
            is Map<*, *> -> {
                // Si c'est un objet, on le convertit
                try {
                    DocumentRequestUser(
                        id = (userIdRaw["_id"] as? String) ?: (userIdRaw["id"] as? String),
                        firstName = userIdRaw["firstName"] as? String,
                        lastName = userIdRaw["lastName"] as? String,
                        email = userIdRaw["email"] as? String,
                        studentId = userIdRaw["studentId"] as? String
                    )
                } catch (e: Exception) {
                    null
                }
            }
            is String -> {
                // Si c'est juste un ID, on crée un objet minimal
                DocumentRequestUser(id = userIdRaw)
            }
            else -> null
        }
}

data class DocumentFileItem(
    @SerializedName("_id")
    val id: String,
    val nomFichier: String? = null,
    val url: String? = null,
    val documentRequestId: String? = null,
    val type: String? = null,
    val annee: String? = null,
    @SerializedName("userId")
    val user: DocumentRequestUser? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class DocumentRequestStats(
    val totalRequests: Int = 0,
    val totalFiles: Int = 0,
    val byType: Map<String, Int> = emptyMap()
)

data class CreateDocumentRequestPayload(
    val type: String,
    val annee: String,
    val fileUrl: String? = null,
    val details: Map<String, String>? = null
)

data class DocumentRequestCreateResponse(
    val documentRequest: DocumentRequestItem,
    val fileUrl: String? = null,
    val fileName: String? = null
)

