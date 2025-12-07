package com.example.esprit.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * Custom deserializer for Location to handle cases where backend sends
 * location as a JSON string instead of a proper object
 */
class LocationDeserializer : JsonDeserializer<Location?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Location? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return try {
            when {
                // Case 1: Location is a proper JSON object
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    Location(
                        address = obj.get("address")?.takeIf { !it.isJsonNull }?.asString,
                        latitude = obj.get("latitude")?.takeIf { !it.isJsonNull }?.asDouble,
                        longitude = obj.get("longitude")?.takeIf { !it.isJsonNull }?.asDouble
                    )
                }
                // Case 2: Location is a JSON string (needs to be parsed)
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    val jsonString = json.asString
                    // Try to parse the string as JSON
                    try {
                        val obj = com.google.gson.JsonParser.parseString(jsonString).asJsonObject
                        Location(
                            address = obj.get("address")?.takeIf { !it.isJsonNull }?.asString,
                            latitude = obj.get("latitude")?.takeIf { !it.isJsonNull }?.asDouble,
                            longitude = obj.get("longitude")?.takeIf { !it.isJsonNull }?.asDouble
                        )
                    } catch (e: Exception) {
                        // If parsing fails, treat the string as an address
                        Location(address = jsonString, latitude = null, longitude = null)
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
