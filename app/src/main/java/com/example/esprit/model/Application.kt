<<<<<<< HEAD
package com.example.esprit.model

import com.google.gson.annotations.SerializedName
import com.google.gson.*
import java.lang.reflect.Type

data class Application(
    @SerializedName("_id")
    val id: String? = null,

    // Backend stocke maintenant l'identifiant étudiant (ex: "HT12345")
    @SerializedName("userId")
    val userId: String? = null,

    // populate("internshipId") côté backend → on récupère l'offre complète ici
    // Peut être soit un String (ID) soit un Object (InternshipOffer populé)
    @SerializedName("internshipId")
    val internshipId: InternshipOffer? = null,
    
    // ID de l'internship si non populé
    @SerializedName("internshipId")
    val internshipIdString: String? = null,

    val cvUrl: String,
    val coverLetter: String? = null,
    val aiScore: Int? = null,
    val status: String? = null,          // "pending" | "accepted" | "rejected"
    
    // Interview scheduling fields (mock calendar integration)
    @SerializedName("interviewScheduledAt")
    val interviewScheduledAt: String? = null,  // ISO date string
    
    @SerializedName("interviewDuration")
    val interviewDuration: Int? = null,        // Duration in minutes
    
    @SerializedName("interviewMeetingLink")
    val interviewMeetingLink: String? = null,  // Google Meet link (mock or real)
    
    @SerializedName("interviewGoogleEventId")
    val interviewGoogleEventId: String? = null, // Calendar event ID
    
    @SerializedName("interviewNotes")
    val interviewNotes: String? = null         // Additional notes from admin
)

// Custom deserializer pour gérer internshipId flexible
class ApplicationDeserializer : JsonDeserializer<Application> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Application {
        val jsonObject = json.asJsonObject
        
        // Gérer internshipId qui peut être String ou Object
        var internshipOffer: InternshipOffer? = null
        var internshipIdStr: String? = null
        
        val internshipIdElement = jsonObject.get("internshipId")
        if (internshipIdElement != null && !internshipIdElement.isJsonNull) {
            if (internshipIdElement.isJsonObject) {
                // C'est un objet InternshipOffer
                internshipOffer = context.deserialize(internshipIdElement, InternshipOffer::class.java)
            } else if (internshipIdElement.isJsonPrimitive) {
                // C'est juste l'ID en String
                internshipIdStr = internshipIdElement.asString
            }
        }
        
        return Application(
            id = jsonObject.get("_id")?.asString,
            userId = jsonObject.get("userId")?.asString,
            internshipId = internshipOffer,
            internshipIdString = internshipIdStr,
            cvUrl = jsonObject.get("cvUrl")?.asString ?: "",
            coverLetter = jsonObject.get("coverLetter")?.asString,
            aiScore = jsonObject.get("aiScore")?.asInt,
            status = jsonObject.get("status")?.asString,
            // Interview fields
            interviewScheduledAt = jsonObject.get("interviewScheduledAt")?.asString,
            interviewDuration = jsonObject.get("interviewDuration")?.asInt,
            interviewMeetingLink = jsonObject.get("interviewMeetingLink")?.asString,
            interviewGoogleEventId = jsonObject.get("interviewGoogleEventId")?.asString,
            interviewNotes = jsonObject.get("interviewNotes")?.asString
        )
    }
}
=======
package com.example.esprit.model

import com.google.gson.annotations.SerializedName
import com.google.gson.*
import java.lang.reflect.Type

data class Application(
    @SerializedName("_id")
    val id: String? = null,

    // Backend stocke maintenant l'identifiant étudiant (ex: "HT12345")
    @SerializedName("userId")
    val userId: String? = null,

    // populate("internshipId") côté backend → on récupère l'offre complète ici
    // Peut être soit un String (ID) soit un Object (InternshipOffer populé)
    @SerializedName("internshipId")
    val internshipId: InternshipOffer? = null,
    
    // ID de l'internship si non populé
    @SerializedName("internshipId")
    val internshipIdString: String? = null,

    val cvUrl: String,
    val coverLetter: String? = null,
    val aiScore: Int? = null,
    val status: String? = null,          // "pending" | "accepted" | "rejected"
    
    // Interview scheduling fields (mock calendar integration)
    @SerializedName("interviewScheduledAt")
    val interviewScheduledAt: String? = null,  // ISO date string
    
    @SerializedName("interviewDuration")
    val interviewDuration: Int? = null,        // Duration in minutes
    
    @SerializedName("interviewMeetingLink")
    val interviewMeetingLink: String? = null,  // Google Meet link (mock or real)
    
    @SerializedName("interviewGoogleEventId")
    val interviewGoogleEventId: String? = null, // Calendar event ID
    
    @SerializedName("interviewNotes")
    val interviewNotes: String? = null         // Additional notes from admin
)

// Custom deserializer pour gérer internshipId flexible
class ApplicationDeserializer : JsonDeserializer<Application> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Application {
        val jsonObject = json.asJsonObject
        
        // Gérer internshipId qui peut être String ou Object
        var internshipOffer: InternshipOffer? = null
        var internshipIdStr: String? = null
        
        val internshipIdElement = jsonObject.get("internshipId")
        if (internshipIdElement != null && !internshipIdElement.isJsonNull) {
            if (internshipIdElement.isJsonObject) {
                // C'est un objet InternshipOffer
                internshipOffer = context.deserialize(internshipIdElement, InternshipOffer::class.java)
            } else if (internshipIdElement.isJsonPrimitive) {
                // C'est juste l'ID en String
                internshipIdStr = internshipIdElement.asString
            }
        }
        
        return Application(
            id = jsonObject.get("_id")?.asString,
            userId = jsonObject.get("userId")?.asString,
            internshipId = internshipOffer,
            internshipIdString = internshipIdStr,
            cvUrl = jsonObject.get("cvUrl")?.asString ?: "",
            coverLetter = jsonObject.get("coverLetter")?.asString,
            aiScore = jsonObject.get("aiScore")?.asInt,
            status = jsonObject.get("status")?.asString,
            // Interview fields
            interviewScheduledAt = jsonObject.get("interviewScheduledAt")?.asString,
            interviewDuration = jsonObject.get("interviewDuration")?.asInt,
            interviewMeetingLink = jsonObject.get("interviewMeetingLink")?.asString,
            interviewGoogleEventId = jsonObject.get("interviewGoogleEventId")?.asString,
            interviewNotes = jsonObject.get("interviewNotes")?.asString
        )
    }
}
>>>>>>> origin/messaging-announcement
