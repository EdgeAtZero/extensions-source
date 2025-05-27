@file:Suppress("NOTHING_TO_INLINE")

package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.*

class ParsingScope private constructor() {

    inline fun JsonArray.jsonArray(index: Int): JsonArray = (getOrNull(index) as? JsonArray)!!
    inline fun JsonArray.jsonObject(index: Int): JsonObject = (getOrNull(index) as? JsonObject)!!
    inline fun JsonArray.boolean(index: Int): Boolean = getOrNull(index)?.jsonPrimitive?.booleanOrNull!!
    inline fun JsonArray.int(index: Int): Int = getOrNull(index)?.jsonPrimitive?.intOrNull!!
    inline fun JsonArray.double(index: Int): Double = getOrNull(index)?.jsonPrimitive?.doubleOrNull!!
    inline fun JsonArray.long(index: Int): Long = getOrNull(index)?.jsonPrimitive?.longOrNull!!
    inline fun JsonArray.float(index: Int): Float = getOrNull(index)?.jsonPrimitive?.floatOrNull!!
    inline fun JsonArray.string(index: Int): String = getOrNull(index)?.jsonPrimitive?.contentOrNull!!

    inline fun JsonObject.jsonArray(key: String): JsonArray = (get(key) as? JsonArray)!!
    inline fun JsonObject.jsonObject(key: String): JsonObject = (get(key) as? JsonObject)!!
    inline fun JsonObject.boolean(key: String): Boolean = get(key)?.jsonPrimitive?.booleanOrNull!!
    inline fun JsonObject.int(key: String): Int = get(key)?.jsonPrimitive?.intOrNull!!
    inline fun JsonObject.double(key: String): Double = get(key)?.jsonPrimitive?.doubleOrNull!!
    inline fun JsonObject.long(key: String): Long = get(key)?.jsonPrimitive?.longOrNull!!
    inline fun JsonObject.float(key: String): Float = get(key)?.jsonPrimitive?.floatOrNull!!
    inline fun JsonObject.string(key: String): String = get(key)?.jsonPrimitive?.contentOrNull!!

    companion object {

        val INSTANCE = ParsingScope()

    }

}
