package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.*

fun JsonObject.getJsonArray(key: String): JsonArray? = get(key)?.jsonArray
fun JsonObject.getJsonObject(key: String): JsonObject? = get(key)?.jsonObject
fun JsonObject.getBoolean(key: String): Boolean? = get(key)?.jsonPrimitive?.booleanOrNull
fun JsonObject.getInt(key: String): Int? = get(key)?.jsonPrimitive?.intOrNull
fun JsonObject.getDouble(key: String): Double? = get(key)?.jsonPrimitive?.doubleOrNull
fun JsonObject.getLong(key: String): Long? = get(key)?.jsonPrimitive?.longOrNull
fun JsonObject.getFloat(key: String): Float? = get(key)?.jsonPrimitive?.floatOrNull
fun JsonObject.getString(key: String): String? = get(key)?.jsonPrimitive?.contentOrNull
