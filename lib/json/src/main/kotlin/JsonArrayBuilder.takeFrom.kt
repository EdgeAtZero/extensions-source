@file:Suppress("UNCHECKED_CAST")

package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

private val arrayContent = JsonArray::class.declaredMemberProperties
    .first { it.name == "content" }
    .also { it.isAccessible = true }

private val builderContent = JsonArrayBuilder::class.declaredMemberProperties
    .first { it.name == "content" }
    .also { it.isAccessible = true }

fun JsonArrayBuilder.takeFrom(jsonArray: JsonArray) {
    val builderContent = builderContent.get(this) as MutableMap<String, JsonElement>
    val arrayContent = arrayContent.get(jsonArray) as MutableMap<String, JsonElement>
    builderContent.putAll(arrayContent)
}
