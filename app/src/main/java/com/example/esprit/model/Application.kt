package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class Application(
    @SerializedName("_id")
    val id: String? = null,

    // Backend stocke maintenant l’identifiant étudiant (ex: "HT12345")
    @SerializedName("userId")
    val userId: String? = null,

    // populate("internshipId") côté backend → on récupère l’offre complète ici
    @SerializedName("internshipId")
    val internshipId: InternshipOffer? = null,

    val cvUrl: String,
    val coverLetter: String? = null,
    val aiScore: Int? = null,
    val status: String? = null          // "pending" | "accepted" | "rejected"
)
