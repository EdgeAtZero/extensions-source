@file:Suppress("UNCHECKED_CAST")

package eu.kanade.tachiyomi.lib.json

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

private val objectContent = JsonObject::class.declaredMemberProperties
    .first { it.name == "content" }
    .also { it.isAccessible = true }

private val builderContent = JsonObjectBuilder::class.declaredMemberProperties
    .first { it.name == "content" }
    .also { it.isAccessible = true }

fun JsonObjectBuilder.takeFrom(jsonObject: JsonObject) {
    val builderContent = builderContent.get(this) as MutableMap<String, JsonElement>
    val objectContent = objectContent.get(jsonObject) as MutableMap<String, JsonElement>
    builderContent.putAll(objectContent)
}
