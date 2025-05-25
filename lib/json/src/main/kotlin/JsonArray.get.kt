package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.*

fun JsonArray.getJsonArray(index: Int): JsonArray? = getOrNull(index)?.jsonArray
fun JsonArray.getJsonObject(index: Int): JsonObject? = getOrNull(index)?.jsonObject
fun JsonArray.getBoolean(index: Int): Boolean? = getOrNull(index)?.jsonPrimitive?.booleanOrNull
fun JsonArray.getInt(index: Int): Int? = getOrNull(index)?.jsonPrimitive?.intOrNull
fun JsonArray.getDouble(index: Int): Double? = getOrNull(index)?.jsonPrimitive?.doubleOrNull
fun JsonArray.getLong(index: Int): Long? = getOrNull(index)?.jsonPrimitive?.longOrNull
fun JsonArray.getFloat(index: Int): Float? = getOrNull(index)?.jsonPrimitive?.floatOrNull
fun JsonArray.getString(index: Int): String? = getOrNull(index)?.jsonPrimitive?.contentOrNull
