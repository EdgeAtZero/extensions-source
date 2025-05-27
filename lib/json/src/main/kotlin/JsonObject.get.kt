@file:Suppress("NOTHING_TO_INLINE")

package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.*

inline fun JsonObject.getJsonArray(key: String): JsonArray? = get(key) as? JsonArray
inline fun JsonObject.getJsonObject(key: String): JsonObject? = get(key) as? JsonObject
inline fun JsonObject.getBoolean(key: String): Boolean? = get(key)?.jsonPrimitive?.booleanOrNull
inline fun JsonObject.getInt(key: String): Int? = get(key)?.jsonPrimitive?.intOrNull
inline fun JsonObject.getDouble(key: String): Double? = get(key)?.jsonPrimitive?.doubleOrNull
inline fun JsonObject.getLong(key: String): Long? = get(key)?.jsonPrimitive?.longOrNull
inline fun JsonObject.getFloat(key: String): Float? = get(key)?.jsonPrimitive?.floatOrNull
inline fun JsonObject.getString(key: String): String? = get(key)?.jsonPrimitive?.contentOrNull
