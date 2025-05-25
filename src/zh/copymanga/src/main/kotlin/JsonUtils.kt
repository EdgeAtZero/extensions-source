package eu.kanade.tachiyomi.extension.zh.copymanga

import com.github.liuyueyi.quick.transfer.ChineseUtils
import eu.kanade.tachiyomi.lib.json.getInt
import eu.kanade.tachiyomi.lib.json.getJsonArray
import eu.kanade.tachiyomi.lib.json.getJsonObject
import eu.kanade.tachiyomi.lib.json.getString
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.text.SimpleDateFormat
import java.util.*


object JsonUtils {

    fun parseComic(source: JsonObject): SManga =
        SManga.create().apply {
            url = "${Constants.MANGA_URL_PREFIX}${source.getString("path_word")}"
            title = source.getString("name")!!.let(ChineseUtils::t2s)
            author = source.getJsonArray("author")!!
                .joinToString(separator = ", ") { it.jsonObject.getString("name")!!.let(ChineseUtils::t2s) }
            thumbnail_url = source.getString("cover")
        }

    fun parseComicDetail(source: JsonObject): SManga =
        parseComic(source).apply {
            description = source.getString("brief")?.let(ChineseUtils::t2s)
            genre = source.getJsonArray("theme")!!
                .joinToString(separator = ", ") { it.jsonObject.getString("name")!!.let(ChineseUtils::t2s) }
            status = when (source.getJsonObject("status")!!.getInt("value")) {
                in 1..2 -> SManga.COMPLETED
                0 -> SManga.ONGOING
                else -> SManga.UNKNOWN
            }
            initialized = true
        }

    private val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    fun parseChapter(source: JsonObject): SChapter =
        SChapter.create().apply {
            val comic = "${Constants.MANGA_URL_PREFIX}${source.getString("comic_path_word")}"
            url = "${comic}${Constants.CHAPTER_URL_PREFIX}${source.getString("uuid")}"
            name = source.getString("name")!!.let(ChineseUtils::t2s)
            date_upload = source.getString("datetime_created")!!.let { date.parse(it)?.time ?: 0L }
        }

}
