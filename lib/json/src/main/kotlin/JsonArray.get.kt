@file:Suppress("NOTHING_TO_INLINE")

package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.*

inline fun JsonArray.getJsonArray(index: Int): JsonArray? = getOrNull(index) as? JsonArray
inline fun JsonArray.getJsonObject(index: Int): JsonObject? = getOrNull(index) as? JsonObject
inline fun JsonArray.getBoolean(index: Int): Boolean? = getOrNull(index)?.jsonPrimitive?.booleanOrNull
inline fun JsonArray.getInt(index: Int): Int? = getOrNull(index)?.jsonPrimitive?.intOrNull
inline fun JsonArray.getDouble(index: Int): Double? = getOrNull(index)?.jsonPrimitive?.doubleOrNull
inline fun JsonArray.getLong(index: Int): Long? = getOrNull(index)?.jsonPrimitive?.longOrNull
inline fun JsonArray.getFloat(index: Int): Float? = getOrNull(index)?.jsonPrimitive?.floatOrNull
inline fun JsonArray.getString(index: Int): String? = getOrNull(index)?.jsonPrimitive?.contentOrNull
